package tsdb.component;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


import org.tinylog.Logger;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * SourceCatalog contains info about imported data-files.
 * @author woellauer
 *
 */
public class SourceCatalog {
	
	

	private static final String DB_FILENAME_PREFIX = "SourceCatalog";
	private static final String DB_NAME_SOURCE_CATALOG = "SourceCatalog";

	private DB db;
	private BTreeMap<String, SourceEntry> catalogMap;

	public SourceCatalog(String databasePath) {
		
		try {
			File dir = new File(databasePath);
			dir.mkdirs();
		} catch(Exception e) {
			Logger.error(e);
		}
		
		this.db = DBMaker.newFileDB(new File(databasePath+DB_FILENAME_PREFIX))
						 .compressionEnable()
				         .closeOnJvmShutdown()
				         .make();

		if(db.getAll().containsKey(DB_NAME_SOURCE_CATALOG)) {
			Logger.trace("open existing SourceCatalog");
			this.catalogMap = db.getTreeMap(DB_NAME_SOURCE_CATALOG);
		} else {
			System.out.println("create new SourceCatalog");
			this.catalogMap =  db.createTreeMap(DB_NAME_SOURCE_CATALOG).makeStringMap();
		}		
	}

	public void clear() {
		catalogMap.clear();
	}

	public void insert(SourceEntry sourceEntry) {
		catalogMap.put(sourceEntry.path+'/'+sourceEntry.filename, sourceEntry);
	}

	public Collection<SourceEntry> getEntries() {
		return catalogMap.values();
	}

	public List<SourceEntry> getEntriesWithStationName(String stationName) {		
		return getEntries().stream().filter((SourceEntry x)->x.stationName.equals(stationName)).collect(Collectors.toList());		
	}
	
	public void commit() {
		db.commit();
	}
	
	public void close() {
		db.commit();
		db.close();
	}
}
