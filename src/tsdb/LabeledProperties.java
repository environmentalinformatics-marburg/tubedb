package tsdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabeledProperties {

	private static final List<LabeledProperty> EMPTY_LIST = new ArrayList<>(0);

	private Map<String, List<LabeledProperty>> labelMap = new HashMap<>();


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

	public void insert(LabeledProperty property) {
		List<LabeledProperty> list = labelMap.get(property.label);
		if(list==null) {
			list = new ArrayList<>();
			labelMap.put(property.label, list);
		}
		list.add(property);		
	}

}
