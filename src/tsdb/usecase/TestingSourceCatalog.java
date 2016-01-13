package tsdb.usecase;

import tsdb.TsDBFactory;
import tsdb.TsDB;
import tsdb.component.SourceEntry;

public class TestingSourceCatalog {

	public static void main(String[] args) {
		System.out.println("start...");
		TsDB timeSeriesDatabase = TsDBFactory.createDefault();
		
		//timeSeriesDatabase.sourceCatalog.insert(new SourceEntry(Paths.get("c:/filename/file6"), "stationName", 0, 0, 0, new String[]{"h"}, new String[]{"h"}, 0));
		//timeSeriesDatabase.sourceCatalog.insert(new SourceEntry(Paths.get("c:/filename/file7"), "stationName", 0, 0, 0, new String[]{"h"}, new String[]{"h"}, 0));
		
		
		System.out.println(timeSeriesDatabase.sourceCatalog.getEntries().size());
		
		
		for(SourceEntry entry:timeSeriesDatabase.sourceCatalog.getEntries()) {
			System.out.println(entry);
		}
		
		timeSeriesDatabase.close();


	}

}
