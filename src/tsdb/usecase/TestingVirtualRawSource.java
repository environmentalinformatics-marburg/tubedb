package tsdb.usecase;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.node.RawSource;
import tsdb.util.iterator.TsIterator;

public class TestingVirtualRawSource {
	public static void main(String[] args) {
		TsDB tsdb = TsDBFactory.createDefault();
		
		//VirtualRawSource raw = VirtualRawSource.create(tsdb,tsdb.getVirtualPlot("fer0"),new String[]{"Ta_200"});
		//RawSource raw = RawSource.of(tsdb, "fer0", new String[]{"Ta_200"});
		RawSource raw = RawSource.of(tsdb, "AEG01", new String[]{"Albedo"});
		TsIterator it = raw.get(null, null);
		
		while(it.hasNext()) {
			System.out.println(it.next());
		}
		
		
		tsdb.close();
	}
}
