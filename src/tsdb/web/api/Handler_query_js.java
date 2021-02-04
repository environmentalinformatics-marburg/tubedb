package tsdb.web.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

		JSONArray jsonTimeseries = jsonReq.getJSONArray("timeseries");
		log.info(jsonTimeseries);
		JSONObject jsonTimeseriesEntry = jsonTimeseries.getJSONObject(0);
		String jsonTimeseriesEntryPlot = jsonTimeseriesEntry.getString("plot");
		String jsonTimeseriesEntrySensor = jsonTimeseriesEntry.getString("sensor");

		String plot = jsonTimeseriesEntryPlot;
		String[] schema = new String[] {jsonTimeseriesEntrySensor};
		int schemaCount = schema.length;

		Long startTime = null;
		Long endTime = null;

		String[] supplementedSchema = tsdb.supplementSchema(schema, tsdb.getSensorNamesOfPlotWithVirtual(plot));			
		String[] validSchema =  tsdb.getValidSchemaWithVirtualSensors(plot, supplementedSchema);
		TimestampSeries ts = tsdb.plot(null, plot, validSchema, agg, dataQuality, interpolation, startTime, endTime);

		List<TsEntry> entries = ts.entryList;
		int entryCount = entries.size();

		int INT_SIZE = 4;
		int FLOAT_SIZE = 4;
		int bufferLen = INT_SIZE + INT_SIZE + entryCount * (INT_SIZE + schemaCount * FLOAT_SIZE);
		byte[] data = new byte[bufferLen];
		ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		byteBuffer.putInt(entryCount);
		byteBuffer.putInt(schemaCount);
		for(TsEntry entry : entries) {
			byteBuffer.putInt((int) entry.timestamp);
		}
		for(int i = 0; i < schemaCount; i++) {
			String sensorName = schema[i];
			int sensorNameIndex = ts.getIndexOfSensorName(sensorName);
			if(sensorNameIndex >= 0) {
				for(TsEntry entry : entries) {
					byteBuffer.putFloat(entry.data[i]);
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
