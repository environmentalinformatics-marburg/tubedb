package tsdb.web.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import tsdb.remote.RemoteTsDB;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TimestampSeries;

public class Handler_query_js extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_query_js(RemoteTsDB tsdb) {
		super(tsdb, "query_js");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("application/octet-stream");

		JSONObject jsonReq = new JSONObject(new JSONTokener(request.getReader()));
		JSONObject jsonSettings = jsonReq.getJSONObject("settings");
		String timeAggregation = jsonSettings.getString("timeAggregation");
		String quality = jsonSettings.getString("quality");
		boolean interpolation = jsonSettings.getBoolean("interpolation");

		AggregationInterval agg = AggregationInterval.parse(timeAggregation);
		DataQuality dataQuality = DataQuality.parse(quality);

		Long startTime = null;
		Long endTime = null;

		TimestampSeries resultTs = null;
		int resultSchemaCount = -1;

		JSONArray jsonTimeseries = jsonReq.getJSONArray("timeseries");
		log.info(jsonTimeseries);
		int jsonTimeseriesLen = jsonTimeseries.length();

		switch(jsonTimeseriesLen) {
		case 0: {
			return;
		}
		case 1: {
			JSONObject jsonTimeseriesEntry = jsonTimeseries.getJSONObject(0);
			String jsonTimeseriesEntryPlot = jsonTimeseriesEntry.getString("plot");
			String jsonTimeseriesEntrySensor = jsonTimeseriesEntry.getString("sensor");

			String plot = jsonTimeseriesEntryPlot;
			String[] schema = new String[] {jsonTimeseriesEntrySensor};
			resultSchemaCount = schema.length;

			String[] supplementedSchema = tsdb.supplementSchema(schema, tsdb.getSensorNamesOfPlotWithVirtual(plot));			
			String[] validSchema =  tsdb.getValidSchemaWithVirtualSensors(plot, supplementedSchema);
			resultTs = tsdb.plot(null, plot, validSchema, agg, dataQuality, interpolation, startTime, endTime);
			break;
		}
		default: {
			TimestampSeries[] tss = new TimestampSeries[jsonTimeseriesLen];
			for (int i = 0; i < jsonTimeseriesLen; i++) {
				JSONObject jsonTimeseriesEntry = jsonTimeseries.getJSONObject(i);
				String jsonTimeseriesEntryPlot = jsonTimeseriesEntry.getString("plot");
				String jsonTimeseriesEntrySensor = jsonTimeseriesEntry.getString("sensor");

				String plot = jsonTimeseriesEntryPlot;
				String[] schema = new String[] {jsonTimeseriesEntrySensor};

				String[] supplementedSchema = tsdb.supplementSchema(schema, tsdb.getSensorNamesOfPlotWithVirtual(plot));			
				String[] validSchema =  tsdb.getValidSchemaWithVirtualSensors(plot, supplementedSchema);
				tss[i] = tsdb.plot(null, plot, validSchema, agg, dataQuality, interpolation, startTime, endTime);
				log.info(tss[i].toString());
			}
			resultTs = TimestampSeries.castMerge(tss);
			resultSchemaCount = resultTs.sensorNames.length;
		}
		}
		
		log.info(Arrays.toString(resultTs.sensorNames));
		
		log.info(resultTs.toString());
		
		String[] schema = resultTs.sensorNames;

		List<TsEntry> entries = resultTs.entryList;
		int entryCount = entries.size();

		if(entryCount > 1) {
			TsEntry a = entries.get(0);
			TsEntry b = entries.get(entryCount - 1);
			log.info(entryCount + " entries  " + TimeUtil.oleMinutesToText(a.timestamp, b.timestamp) + "   " + a.timestamp + " - " + b.timestamp);
		}

		int INT_SIZE = 4;
		int FLOAT_SIZE = 4;
		int bufferLen = INT_SIZE + INT_SIZE + entryCount * (INT_SIZE + resultSchemaCount * FLOAT_SIZE);
		byte[] data = new byte[bufferLen];
		ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		byteBuffer.putInt(entryCount);
		byteBuffer.putInt(resultSchemaCount);
		for(TsEntry entry : entries) {
			byteBuffer.putInt((int) entry.timestamp);
		}
		for(int i = 0; i < resultSchemaCount; i++) {
			String sensorName = schema[i];
			int sensorNameIndex = resultTs.getIndexOfSensorName(sensorName);
			if(sensorNameIndex >= 0) {
				for(TsEntry entry : entries) {
					float value = entry.data[sensorNameIndex];
					//log.info("put " + value);
					byteBuffer.putFloat(value);
				}
			} else {
				for (int j = 0; j < entryCount; j++) {
					byteBuffer.putFloat(Float.NaN);
				}
			}
		}
		response.getOutputStream().write(data);		
	}
}
