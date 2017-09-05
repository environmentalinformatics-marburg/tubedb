package tsdb.component.labeledproperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

}
