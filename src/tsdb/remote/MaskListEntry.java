package tsdb.remote;

import org.json.JSONObject;
import org.json.JSONWriter;

import tsdb.run.command.LoadMasks.MaskType;

public class MaskListEntry {
	@Override
	public String toString() {
		return "MaskListEntry [type=" + type + ", line=" + line + ", station=" + station + ", sensor=" + sensor
				+ ", start=" + start + ", end=" + end + ", user=" + user + ", date=" + date + ", comment=" + comment
				+ ", removed=" + removed + "]";
	}

	public final MaskType type;
	public final int line;
	public final String station;
	public final String sensor;
	public final String start;
	public final String end;
	public final String user;
	public final String date;
	public final String comment;
	public final String removed;
	
	public MaskListEntry(MaskType type, int line, String station, String sensor, String start, String end, String user, String date, String comment, String removed) {
		this.type = type;
		this.line = line;
		this.station = station;
		this.sensor = sensor;
		this.start = start;
		this.end = end;
		this.user = user;
		this.date = date;
		this.comment = comment;
		this.removed = removed;
	}
	
	public MaskListEntry asRemoved(String removed) {
	    return new MaskListEntry(type, line, station, sensor, start, end, user, date, comment, removed);
	}
	
	public boolean isRemoved() {
	    return !removed.isBlank();
	}
	
	public void writeJSON(JSONWriter jsonWriter) {
	    jsonWriter.object();
	    jsonWriter.key("type").value(type.getTypeText());
	    jsonWriter.key("line").value(line);
	    jsonWriter.key("station").value(station);
	    jsonWriter.key("sensor").value(sensor);
	    jsonWriter.key("start").value(start);
	    jsonWriter.key("end").value(end);
	    jsonWriter.key("user").value(user);
	    jsonWriter.key("date").value(date);
	    jsonWriter.key("comment").value(comment);
	    jsonWriter.key("removed").value(removed);
	    jsonWriter.endObject();
	}
	
	public static MaskListEntry fromJSON(JSONObject json) {
	    MaskType type = MaskType.fromText(json.getString("type"));
	    int line = json.getInt("line");
	    String station = json.getString("station");
	    String sensor = json.getString("sensor");
	    String start = json.getString("start");
	    String end = json.getString("end");
	    String user = json.optString("user", "");
	    String date = json.optString("date", "");
	    String comment = json.optString("comment", "");
	    String removed = json.optString("removed", "");
	    return new MaskListEntry(type, line, station, sensor, start, end, user, date, comment, removed);
	}
}