package tsdb.web.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.eclipse.jetty.server.Request;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.tinylog.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tsdb.remote.RemoteTsDB;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TimestampSeries;

public class Handler_query_js extends MethodHandler {	


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
		boolean interpolation = jsonSettings.optBoolean("interpolation", false);

		AggregationInterval agg = AggregationInterval.parse(timeAggregation);
		DataQuality dataQuality = DataQuality.parse(quality);

		Long startTime = null;
		Long endTime = null;

		boolean limitTime = false;
		long limitStart = Long.MIN_VALUE;
		long limitEnd = Long.MAX_VALUE;
		if(jsonSettings.has("view_time_limit_start")) {
			limitStart = jsonSettings.getLong("view_time_limit_start");
			limitTime = true;
		}
		if(jsonSettings.has("view_time_limit_end")) {
			limitEnd = jsonSettings.getLong("view_time_limit_end");
			limitTime = true;
		}


		TimestampSeries resultTs = null;
		int resultSchemaCount = -1;

		JSONArray jsonTimeseries = jsonReq.getJSONArray("timeseries");
		Logger.info(jsonTimeseries);
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
			if(limitTime && resultTs != null && resultTs.size() > 0 && (resultTs.getFirstTimestamp() < limitStart || resultTs.getLastTimestamp() > limitEnd)) {
				resultTs = resultTs.limitTime(limitStart, limitEnd);
			}
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
				if(limitTime && tss[i] != null && tss[i].size() > 0 && (tss[i].getFirstTimestamp() < limitStart || tss[i].getLastTimestamp() > limitEnd)) {
					tss[i] = tss[i].limitTime(limitStart, limitEnd);
				}
				//Logger.info(tss[i].toString());
			}
			resultTs = TimestampSeries.castMerge(tss);
			resultSchemaCount = resultTs.sensorNames.length;
		}
		}

		//Logger.info(Arrays.toString(resultTs.sensorNames));

		//Logger.info(resultTs.toString());

		String[] schema = resultTs.sensorNames;

		List<TsEntry> entries = resultTs.entryList;
		int entryCount = entries.size();

		/*if(entryCount > 1) {
			TsEntry a = entries.get(0);
			TsEntry b = entries.get(entryCount - 1);
			Logger.info(entryCount + " entries  " + TimeUtil.oleMinutesToText(a.timestamp, b.timestamp) + "   " + a.timestamp + " - " + b.timestamp);
		}*/

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
					//Logger.info("put " + value);
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
