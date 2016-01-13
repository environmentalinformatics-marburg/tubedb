package tsdb.component;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun.Tuple2;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.iterator.TsIterator;

/**
 * Storage for pre calculated time series
 * @author woellauer
 *
 */
public class CacheStorage {
	
	private static final Logger log = LogManager.getLogger();

	private static final String DB_NAME_METADATA_SCHEMA = "schema";
	private static final String DB_NAME_STREAM_PREFIX = "stream_";

	/*
	 * Layout:
	 * 
	 * "stream_[name]": 
	 * map timestamp -> TimeSeriesEntry
	 * 
	 * 
	 * "schema":
	 * map "stream_[name]" -> TimeSeriesSchema 
	 * 
	 * 
	 */

	private DB db;
	private BTreeMap<String, TsSchema> schemaMap;



	//private Map<String,ConcurrentNavigableMap<Long,TimeSeriesEntry>> streamMap;

	public CacheStorage(String cachePath) {
		
		try {
			File dir = new File(cachePath);
			dir.mkdirs();
		} catch(Exception e) {
			log.error(e);
		}
		
		this.db = DBMaker.newFileDB(new File(cachePath+"cachedb"))
				.compressionEnable()
				//.transactionDisable() //not safe
				//.mmapFileEnable() //slow commit and close!!!
				.mmapFileEnablePartial()
				.asyncWriteEnable()
				.asyncWriteFlushDelay(500)
				//.cacheSize(100000)
				.cacheSize(1000000)
				.closeOnJvmShutdown()
				.make();

		
		
		
		if(db.getAll().containsKey(DB_NAME_METADATA_SCHEMA)) {
			this.schemaMap = db.getTreeMap(DB_NAME_METADATA_SCHEMA);
		} else {
			this.schemaMap =  db.createTreeMap(DB_NAME_METADATA_SCHEMA).makeStringMap();
		}
	}

	public void createNew(String streamName, TsSchema schema) {
		String dbName = DB_NAME_STREAM_PREFIX+streamName;
		db.delete(dbName);
		//ConcurrentNavigableMap<Long,TimeSeriesEntry> map = db.getTreeMap(streamName);
		//streamMap.put(streamName, map);
		schemaMap.remove(streamName);

		//db.getTreeMap(dbName);
		db.createTreeMap(dbName).makeLongMap();
		schemaMap.put(streamName, schema);
		
		

	}

	public ConcurrentNavigableMap<Long, TsEntry> queryMap(String streamName, Long begin, Long end) {
		String dbName = DB_NAME_STREAM_PREFIX+streamName;
		if(schemaMap.containsKey(streamName)) {
			ConcurrentNavigableMap<Long, TsEntry> map = db.getTreeMap(dbName);
			if(begin!=null) {
				Long fromKey = begin;
				if(end!=null) {
					Long toKey = end;
					return map.subMap(fromKey, true, toKey, true);
				} else {
					return map.tailMap(fromKey, true);
				}
			} else {
				if(end!=null) {
					Long toKey = end;
					return map.headMap(toKey, true);
				} else {
					return map;
				}
			}
		} else {
			log.error("stream not in database: "+streamName);
			return null;
		}
	}

	public  TsIterator query(String streamName, Long begin, Long end) {		
		TsSchema schema = schemaMap.get(streamName);
		if(schema!=null) {
			Iterator<TsEntry> it = queryMap(streamName,begin,end).values().iterator();			
			TsIterator it2 = new TsIterator(schema) {
				@Override
				public boolean hasNext() {
					return it.hasNext();
				}
				@Override
				public TsEntry next() {
					return it.next();
				}
				@Override
				public String getProcessingTitle() {
					return "cache query iterator";
				}				
			};
			//System.out.println(it2.getOutputTimeSeriesSchema());
			return it2;
		} else {
			log.error("stream not in database: "+streamName);
			return null;
		}
	}

	public void writeNew(String streamName,TsIterator input_iterator) {
		
		String dbName = DB_NAME_STREAM_PREFIX+streamName;
		db.delete(dbName);
		schemaMap.remove(streamName);

		schemaMap.put(streamName, input_iterator.getSchema());
		
		Iterator<Tuple2<Long, TsEntry>> bulk_iterator = new Iterator<Tuple2<Long,TsEntry>>() {
			@Override
			public boolean hasNext() {
				return input_iterator.hasNext();
			}
			@Override
			public Tuple2<Long, TsEntry> next() {
				TsEntry e = input_iterator.next();
				return new Tuple2<Long, TsEntry>(e.timestamp, e);
			}
		};
		
		db.createTreeMap(dbName).nodeSize(126).pumpIgnoreDuplicates().pumpSource(bulk_iterator).pumpPresort(1000000).make();//.makeLongMap();
		db.commit();
		//db.compact();
				
		
		
		/*createNew(streamName, input_iterator.getOutputTimeSeriesSchema());		
		String dbName = DB_NAME_STREAM_PREFIX+streamName;
		if(schemaMap.containsKey(streamName)) {
			ConcurrentNavigableMap<Long, TimeSeriesEntry> map = db.getTreeMap(dbName);
			while(input_iterator.hasNext()) {
				TimeSeriesEntry element = input_iterator.next();
				map.put(element.timestamp, element);
			}
		} else {
			log.error("stream not in database: "+streamName);
		}
		db.commit();*/
		/*ConcurrentNavigableMap<Long, TimeSeriesEntry> map = streamMap.get(streamName);
		if(map==null) {
			map = db.getTreeMap(streamName);
		}
		while(input_iterator.hasNext()) {
			TimeSeriesEntry element = input_iterator.next();
			map.put(element.timestamp, element);
		}
		db.commit();*/
	}
	
	public void printInfo() {
		System.out.println("*** cache info ***");
		//Map<String, Object> map = db.getAll();
		Map<String, Object> map = db.getCatalog();
		for(Map.Entry<String,Object> entry:map.entrySet()) {
			System.out.println(entry.getKey()+"   "+entry.getValue().getClass()+"  "+entry.getValue().toString());
		}
		System.out.println("******************");
	}
	
	public void clear() {		
		for(String key:schemaMap.keySet()){
			db.getTreeMap(key).clear();
		}
		schemaMap.clear();
		commit_and_compact();
	}
	
	public void close() {
		db.commit();
		db.compact();
		db.close();
	}
	
	public void commit_and_compact() {
		db.commit();
		db.compact();
	}
	
	public Stream<String> getStreamNames() {
		return db.getAll().keySet().stream().filter(x->x.startsWith(DB_NAME_STREAM_PREFIX)).map(x->x.substring(DB_NAME_STREAM_PREFIX.length()));
	}
	
	public TsSchema getSchema(String streamName) {
		return schemaMap.get(streamName);		
	}

}
