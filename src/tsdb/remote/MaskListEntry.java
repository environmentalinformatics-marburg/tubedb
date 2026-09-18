package tsdb.remote;

import org.json.JSONWriter;

public class MaskListEntry {
	public final String station;
	public final String sensor;
	public final String start;
	public final String end;
	
	public MaskListEntry(String station, String sensor, String start, String end) {
		this.station = station;
		this.sensor = sensor;
		this.start = start;
		this.end = end;
	}

	public void writeJSON(JSONWriter json) {
		json.object();
		json.key("station");
		json.value(station);
		json.key("sensor");
		json.value(sensor);
		json.key("start");
		json.value(start);
		json.key("end");
		json.value(end);
		json.endObject();		
	}
}