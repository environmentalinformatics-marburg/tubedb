package tsdb.component.labeledproperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.function.Consumer;

import org.tinylog.Logger;

import tsdb.util.DataRow;
import tsdb.util.TimeUtil;
import tsdb.util.Util;

public class LabeledProperties {

	private static final List<LabeledProperty> EMPTY_LIST = new ArrayList<>(0);

	private Map<String, List<LabeledProperty>> labelMap = new HashMap<>(); // insert order preserved per label only
	private List<LabeledProperty> flatList = new ArrayList<>(); // insert order preserved


	public List<LabeledProperty> query(String label, int start, int end) {
		List<LabeledProperty> list = labelMap.get(label);
		return list==null?EMPTY_LIST:filter(list,start,end);
	}

	private static List<LabeledProperty> filter(List<LabeledProperty> list, int start, int end) {
		int count=0;
		for(LabeledProperty p:list) {
			if((start <= p.end) && (end >= p.start)) {
				count++;
			}
		}
		if(count==0) {
			return EMPTY_LIST;
		} 
		if(count==list.size()) {
			return list;
		}
		List<LabeledProperty> result = new ArrayList<>(count);
		for(LabeledProperty p:list) {
			if((start <= p.end) && (end >= p.start)) {
				result.add(p);
			}
		}
		return result;
	}

	public void forEach(int start, int end, Consumer<? super LabeledProperty> action) {
		for(LabeledProperty prop : flatList) {
			//Logger.info("check for " + TimeUtil.oleMinutesToText(start, end) + "  for  "  + prop);
			if((start <= prop.end) && (end >= prop.start)) {
				action.accept(prop);
			}
		}
	}

	public void insert(LabeledProperty property) {
		List<LabeledProperty> list = labelMap.get(property.label);
		if(list==null) {
			list = new ArrayList<>();
			labelMap.put(property.label, list);
		}
		list.add(property);
		flatList.add(property);
	}

	public Iterator<LabeledProperty> query_iterator(int start, int end) {
		return new IntervalIterator(flatList.iterator(), start, end);
	}

	private static class IntervalIterator implements Iterator<LabeledProperty> {

		private final Iterator<LabeledProperty> it;
		private final int start;
		private final int end;

		private LabeledProperty cur; 

		public IntervalIterator(Iterator<LabeledProperty> it, int start, int end) {
			this.it = it;
			this.start = start;
			this.end = end;
			cur = null;
		}

		@Override
		public boolean hasNext() {
			if(cur != null) {
				return true;
			}
			while(it.hasNext()) {
				LabeledProperty c = it.next();
				if((start <= c.end) && (end >= c.start)) {
					cur = c;
					return true;
				}
			}
			return false;
		}

		@Override
		public LabeledProperty next() {
			hasNext();
			LabeledProperty c = cur;
			cur = null;
			return c;
		}

	}

	public void applyProperties(int firstTimestamp, int lastTimestamp, NavigableMap<Long, DataRow> eventMap, String[] sensorNames, String stationID) {		
		forEach(firstTimestamp, lastTimestamp, prop -> {
			try {
				applyProperty(prop, eventMap, sensorNames);
			} catch(Exception e) {
				Logger.warn(stationID + "   " + e);
			}
		});	
	}

	public static void applyProperty(LabeledProperty prop, NavigableMap<Long, DataRow> eventMap, String[] sensorNames) {
		Collection<DataRow> rows = eventMap.subMap((long)prop.start, true, (long)prop.end, true).values();
		switch (prop.label) {
		case "computation": {
			PropertyComputation computation = (PropertyComputation) prop.content;
			if(Util.containsString(sensorNames, computation.target)) {
				Logger.info("PropertyComputation "+prop.station + "  " +  TimeUtil.oleMinutesToText(prop.start, prop.end) + "   " + computation.target +"    "+ computation.formula_org);
				computation.calculate(rows, sensorNames);
			}
			break;
		}
		case "CNR4": {
			((PropertyCNR4)prop.content).calculate(rows, sensorNames);
			break;
		}
		case "reassign": {
			((PropertyReassign)prop.content).calculate(rows, sensorNames);
			break;
		}
		case "clear": {
			((PropertyClear)prop.content).calculate(rows, sensorNames);
			break;
		}
		case "overwrite": {
			PropertyOverwrite overwrite = ((PropertyOverwrite)prop.content);
			Logger.info("PropertyOverwrite "+prop.station + "  " +  TimeUtil.oleMinutesToText(prop.start, prop.end) + "   " + Arrays.toString(overwrite.source) +"  ->  "+ Arrays.toString(overwrite.target));
			overwrite.calculate(rows, sensorNames);
			break;
		}
		default:
			Logger.warn("unknown property label: "+prop.label);
			break;
		}
	}

	public void applyProperties(int firstTimestamp, int lastTimestamp, Collection<DataRow> dataRows, String[] sensorNames, String stationID) {		
		forEach(firstTimestamp, lastTimestamp, prop -> {
			try {
				applyProperty(prop, dataRows, sensorNames);
			} catch(Exception e) {
				Logger.warn(stationID + "   " + e);
			}
		});	
	}

	public static void applyProperty(LabeledProperty prop, Collection<DataRow> dataRows, String[] sensorNames) {
		switch (prop.label) {
		case "computation": {
			PropertyComputation computation = ((PropertyComputation)prop.content);
			if(Util.containsString(sensorNames, computation.target)) {
				Logger.info("PropertyComputation "+prop.station + "  " +  TimeUtil.oleMinutesToText(prop.start, prop.end) + "   " + computation.target +"    "+ computation.formula_org);
				computation.calculate(dataRows, sensorNames, prop.start, prop.end);
			}
			break;
		}
		case "CNR4": {
			((PropertyCNR4)prop.content).calculate(dataRows, sensorNames, prop.start, prop.end);
			break;
		}
		case "reassign": {
			((PropertyReassign)prop.content).calculate(dataRows, sensorNames, prop.start, prop.end);
			break;
		}
		case "clear": {
			((PropertyClear)prop.content).calculate(dataRows, sensorNames, prop.start, prop.end);
			break;
		}
		case "overwrite": {
			PropertyOverwrite overwrite = ((PropertyOverwrite)prop.content);
			Logger.info("PropertyOverwrite "+prop.station + "  " +  TimeUtil.oleMinutesToText(prop.start, prop.end) + "   " + Arrays.toString(overwrite.source) +"  ->  "+ Arrays.toString(overwrite.target));
			overwrite.calculate(dataRows, sensorNames, prop.start, prop.end);
			break;
		}
		default:
			Logger.warn("unknown property label: "+prop.label);
			break;
		}
	}
}
