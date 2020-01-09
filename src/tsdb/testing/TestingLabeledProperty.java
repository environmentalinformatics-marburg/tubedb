package tsdb.testing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.component.labeledproperty.LabeledProperty;
import tsdb.util.TimeUtil;

public class TestingLabeledProperty {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		try(TsDB tsdb = TsDBFactory.createDefault()) {
			
			for(LabeledProperty p:tsdb.getStation("HEG19").labeledProperties.query("computation", Integer.MIN_VALUE, Integer.MAX_VALUE)) {
				log.info(p);
				
				log.info(TimeUtil.oleMinutesToText(p.start));
				log.info(TimeUtil.oleMinutesToText(p.end));
			}
			
		}

	}

}
