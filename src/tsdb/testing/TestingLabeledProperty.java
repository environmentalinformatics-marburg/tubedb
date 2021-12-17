package tsdb.testing;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.component.labeledproperty.LabeledProperty;
import tsdb.util.TimeUtil;

public class TestingLabeledProperty {
	

	public static void main(String[] args) {
		try(TsDB tsdb = TsDBFactory.createDefault()) {
			
			for(LabeledProperty p:tsdb.getStation("HEG19").labeledProperties.query("computation", Integer.MIN_VALUE, Integer.MAX_VALUE)) {
				Logger.info(p);
				
				Logger.info(TimeUtil.oleMinutesToText(p.start));
				Logger.info(TimeUtil.oleMinutesToText(p.end));
			}
			
		}

	}

}
