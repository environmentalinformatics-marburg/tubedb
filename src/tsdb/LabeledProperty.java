package tsdb;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TimeUtil;

public class LabeledProperty {
	private static final Logger log = LogManager.getLogger();

	public final String station;
	public final String label;
	public final int start;
	public final int end;
	public final YamlMap map;
	public final Object content;

	public LabeledProperty(String station, String label, int start, int end, YamlMap map) {
		this.station = station;
		this.start = start;
		this.end = end;
		this.label = label;
		this.map = map;
		this.content = parseContent(label, map);
	}

	private Object parseContent(String label, YamlMap map) {
		switch(label) {
		case "CNR4":
			return PropertyCNR4.parse(map);
		default:
			return map;
		}
	}

	public static List<LabeledProperty> parse(YamlMap entry) {
		String station = entry.getString("station");
		String startObject = entry.optString("start", "*");
		String endObject = entry.optString("end", "*");
		int start = TimeUtil.parseStartTimestamp(startObject);
		int end = TimeUtil.parseEndTimestamp(endObject);
		log.trace(startObject);
		log.trace(endObject);

		List<LabeledProperty> result = new ArrayList<LabeledProperty>();
		for(YamlMap map:entry.getList("content").asMaps()) {
			try {
			String label = map.getString("label");
			LabeledProperty labeledProperty = new LabeledProperty(station, label, start, end, map);
			result.add(labeledProperty);
			log.trace(labeledProperty);
			} catch (Exception e) {
				log.warn("could not parse entry "+entry+"  "+e);
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return "LabeledProperty [station=" + station + ", label=" + label + ", start=" + start + ", end=" + end
				+ ", content=" + content + "]";
	}

}
