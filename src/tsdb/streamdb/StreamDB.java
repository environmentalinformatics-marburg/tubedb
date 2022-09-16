package tsdb.streamdb;
import static tsdb.util.AssumptionCheck.throwNull;
import static tsdb.util.AssumptionCheck.throwNullArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.tinylog.Logger;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import tsdb.util.DataEntry;
import tsdb.util.TimeUtil;
import tsdb.util.TimeSeriesMask;
import tsdb.util.iterator.TsIterator;

/**
 * Central class of StreamDB
 * @author woellauer
 *
 */
public class StreamDB {

	

	private DB db;

	private static final String DB_NAME_STATION_MAP = "stationMap";

	private BTreeMap<String,StationMeta> stationMetaMap;

	public StreamDB(String streamdbPathPrefix) {
		throwNull(streamdbPathPrefix);
		String pathName = streamdbPathPrefix;

		try {
			File dir = new File(streamdbPathPrefix);			
			dir.getParentFile().mkdirs();
		} catch(Exception e) {
			Logger.error(e);
		}

		db = DBMaker.newFileDB(new File(pathName))
				//.checksumEnable()
				//.compressionEnable() //in new db disabled!
				//.transactionDisable()
				//.mmapFileEnable() //slow commit and close!!!
				.mmapFileEnablePartial()
				.asyncWriteEnable()
				.asyncWriteFlushDelay(500)
				.cacheWeakRefEnable()
				.cacheSize(1000000)
				.closeOnJvmShutdown()
				.make();

		stationMetaMap = db.createTreeMap(DB_NAME_STATION_MAP)
				.keySerializer(BTreeKeySerializer.STRING)
				.valueSerializer(StationMeta.SERIALIZER)
				.makeOrGet();		
	}

	/**
	 * write all data to disk
	 */
	public void commit() {
		synchronized (db) {
			if(!db.isClosed()) {
				db.commit();		
			}
		}
	}

	/**
	 * write all data to disk and close db
	 */
	public void close() {
		synchronized (db) {
			if(!db.isClosed()) {
				Logger.info("commit...");
				db.commit();
				Logger.info("close...");
				db.close();
				Logger.trace("closed");
			}
		}		
	}

	/**
	 * delete all content in db
	 */
	public void clear() {
		for(StationMeta stationMeta:stationMetaMap.values()) {
			BTreeMap<String, SensorMeta> sensorMap = getSensorMap(stationMeta);
			for(SensorMeta sensorMeta:sensorMap.values()) {
				/*BTreeMap<Integer, ChunkMeta> chunkMetaMap = getSensorChunkMetaMap(sensorMeta);
				chunkMetaMap.clear();
				BTreeMap<Integer, Chunk> chunkMap = getSensorChunkMap(sensorMeta);
				chunkMap.clear();*/
				db.delete(sensorMeta.db_name_sensor_chunkmeta_map);
				db.delete(sensorMeta.db_name_sensor_chunk_map);
			}
			//sensorMap.clear();
			db.delete(stationMeta.db_name_sensor_map);

			db.delete(stationMeta.db_name_sensor_time_series_mask_map);			
		}
		stationMetaMap.clear();		
		commit();
		//compact(); // 'compact' not usable because of bug in MapDB.
	}	

	@Override
	protected void finalize() throws Throwable {
		close();
	}

	/**
	 * Check if station exists in StreamDB. Station exists only if it contains time series data.
	 * @param stationID
	 * @return
	 */
	public boolean existStation(String stationID) {
		return stationMetaMap.containsKey(stationID);
	}

	/**
	 * Check if data of sensor exits in station. If station does not exist return false. 
	 * @param stationID
	 * @param sensorName
	 * @return
	 */
	public boolean existSensor(String stationID, String sensorName) {
		StationMeta stationMeta = stationMetaMap.get(stationID);
		if(stationMeta==null) {
			return false;
		}
		return getSensorMap(stationMeta).containsKey(sensorName);
	}

	private StationMeta getStationMeta(String stationName, boolean createIfNotExists) {
		throwNull(stationName);
		StationMeta stationMeta = stationMetaMap.get(stationName);		
		if(stationMeta==null&&createIfNotExists){
			stationMeta = new StationMeta(stationName);

			db.checkNameNotExists(stationMeta.db_name_sensor_map);
			db.createTreeMap(stationMeta.db_name_sensor_map)
			.keySerializer(BTreeKeySerializer.STRING)
			.valueSerializer(SensorMeta.SERIALIZER)
			.makeOrGet();

			db.checkNameNotExists(stationMeta.db_name_sensor_time_series_mask_map);
			db.createTreeMap(stationMeta.db_name_sensor_time_series_mask_map)
			.keySerializer(BTreeKeySerializer.STRING)
			.valueSerializer(TimeSeriesMask.SERIALIZER)
			.makeOrGet();

			stationMetaMap.put(stationName, stationMeta);			
		}
		if(stationMeta==null) {
			//new Throwable().printStackTrace();
			Logger.warn("no station: "+stationName);
		}
		return stationMeta;
	}

	private SensorMeta getSensorMeta(StationMeta stationMeta, String sensorName, boolean createIfNotExists) {
		throwNull(stationMeta);
		throwNull(sensorName);
		BTreeMap<String, SensorMeta> sensorMap = db.getTreeMap(stationMeta.db_name_sensor_map);
		SensorMeta sensorMeta = sensorMap.get(sensorName);
		if(sensorMeta==null&&createIfNotExists) {
			sensorMeta = new SensorMeta(stationMeta.stationName, sensorName);
			db.checkNameNotExists(sensorMeta.db_name_sensor_chunk_map);
			db.createTreeMap(sensorMeta.db_name_sensor_chunk_map)
			.keySerializer(BTreeKeySerializer.ZERO_OR_POSITIVE_INT)
			//.valueSerializer(Chunk.DELTA_TIME_DELTA_DELTA_VALUE_INT_QUANTIZED_SERIALIZER)
			//.valueSerializer(Chunk.SNAPPY_DELTA_TIME_DELTA_DELTA_VALUE_INT_QUANTIZED_SERIALIZER)
			.valueSerializer(ChunkSerializer.DEFAULT)
			//.valuesOutsideNodesEnable() // !!! does not work: growing database
			//.
			.makeOrGet();
			db.checkNameNotExists(sensorMeta.db_name_sensor_chunkmeta_map);
			db.createTreeMap(sensorMeta.db_name_sensor_chunkmeta_map)
			.keySerializer(BTreeKeySerializer.ZERO_OR_POSITIVE_INT)
			.valueSerializer(ChunkMeta.SERIALIZER)
			.makeOrGet();
			sensorMap.put(sensorName, sensorMeta);
		}
		if(sensorMeta==null) {
			//new Throwable().printStackTrace();
			Logger.trace("no sensor: "+sensorName+"  in station: "+stationMeta.stationName);
		}
		return sensorMeta;
	}

	public SensorMeta getSensorMeta(String stationName, String sensorName) {
		return getSensorMeta(stationName, sensorName, false);
	}

	private SensorMeta getSensorMeta(String stationName, String sensorName, boolean createIfNotExists) {
		throwNull(stationName);
		throwNull(sensorName);
		if(createIfNotExists) {
			return getSensorMeta(getStationMeta(stationName, true), sensorName,true);
		} else {
			StationMeta stationMeta = getStationMeta(stationName, false);
			if(stationMeta==null) {
				return null;
			}
			return getSensorMeta(stationMeta, sensorName, false);
		}
	}


	public TimeSeriesMask getSensorTimeSeriesMask(StationMeta stationMeta, String sensorName, boolean createIfNotExists) {
		throwNull(stationMeta);
		throwNull(sensorName);
		BTreeMap<String, TimeSeriesMask> maskMap = db.getTreeMap(stationMeta.db_name_sensor_time_series_mask_map);
		TimeSeriesMask mask = maskMap.get(sensorName);
		if(mask==null&&createIfNotExists) {
			mask = new TimeSeriesMask();
			maskMap.put(sensorName, mask);
		}
		if(mask==null) {
			//Logger.info("no time series mask: "+sensorName+"  in station: "+stationMeta.stationName);
		}
		return mask;		
	}	

	public TimeSeriesMask getSensorTimeSeriesMask(String stationName, String sensorName, boolean createIfNotExists) {
		throwNull(stationName);
		throwNull(sensorName);
		if(createIfNotExists) {
			return getSensorTimeSeriesMask(getStationMeta(stationName, true), sensorName,true);
		} else {
			StationMeta stationMeta = getStationMeta(stationName, false);
			if(stationMeta==null) {
				return null;
			}
			return getSensorTimeSeriesMask(stationMeta, sensorName, false);
		}		
	}

	public void setSensorTimeSeriesMask(String stationName, String sensorName, TimeSeriesMask timeSeriesMask) {
		throwNull(stationName);
		throwNull(sensorName);
		throwNull(timeSeriesMask);
		StationMeta stationMeta = getStationMeta(stationName, true);
		BTreeMap<String, TimeSeriesMask> maskMap = db.getTreeMap(stationMeta.db_name_sensor_time_series_mask_map);
		maskMap.put(sensorName, timeSeriesMask);
	}	

	private BTreeMap<Integer, Chunk> getSensorChunkMap(SensorMeta sensorMeta) {
		throwNull(sensorMeta);
		return db.getTreeMap(sensorMeta.db_name_sensor_chunk_map);
	}

	public BTreeMap<Integer, ChunkMeta> getSensorChunkMetaMap(SensorMeta sensorMeta) {
		throwNull(sensorMeta);
		return db.getTreeMap(sensorMeta.db_name_sensor_chunkmeta_map);
	}


	public void insertSensorData(String stationName, String sensorName, DataEntry[] data) {	
		//Logger.info("streamDB insert data "+stationName+" "+sensorName+" "+data.length);
		throwNull(stationName);
		throwNull(sensorName);
		throwNull(data);
		if(data.length==0) {
			Logger.warn("no data to insert");
			return;
		}
		SensorMeta sensorMeta = getSensorMeta(stationName,sensorName,true);
		BTreeMap<Integer, ChunkMeta> chunkMetaMap = getSensorChunkMetaMap(sensorMeta);
		BTreeMap<Integer, Chunk> chunkMap = getSensorChunkMap(sensorMeta);

		int timestamp_next_year = Integer.MIN_VALUE;
		ArrayList<DataEntry> entryList = new ArrayList<DataEntry>(data.length);
		int prevTimestamp = -1;
		for(DataEntry entry:data) {
			if(entry.timestamp<=prevTimestamp) {
				throw new RuntimeException("not ordered timestamps "+TimeUtil.oleMinutesToText(prevTimestamp)+"  "+TimeUtil.oleMinutesToText(entry.timestamp)+"   "+entry.value+"  "+stationName+"/"+sensorName);
			}
			if(entry.timestamp<timestamp_next_year) {
				entryList.add(entry);				
			} else {
				if(!entryList.isEmpty()) {
					insertIntoOneChunk(chunkMetaMap,chunkMap,entryList);
				}
				timestamp_next_year = TimeUtil.roundNextYear(entry.timestamp);
				entryList.clear();
				entryList.add(entry);
			}
			prevTimestamp = entry.timestamp;
		}
		if(!entryList.isEmpty()) {
			insertIntoOneChunk(chunkMetaMap,chunkMap,entryList);
		}
	}

	public void removeSensorData(String stationName, String sensorName, int start, int end) {
		SensorMeta sensorMeta = getSensorMeta(stationName,sensorName,false);
		if(sensorMeta==null) {
			Logger.info("no sensor: "+stationName+" "+sensorName+" ->  nothing removed");
			return;
		}
		BTreeMap<Integer, ChunkMeta> chunkMetaMap = getSensorChunkMetaMap(sensorMeta);
		BTreeMap<Integer, Chunk> chunkMap = getSensorChunkMap(sensorMeta);

		ChunkMeta[] allChunkMetas = chunkMetaMap.values().toArray(new ChunkMeta[0]); //proxy

		for(ChunkMeta chunkMeta:allChunkMetas) {
			if(start<=chunkMeta.firstTimestamp&&chunkMeta.lastTimestamp<=end) { //remove full chunk
				removeChunk(chunkMetaMap,chunkMap,chunkMeta);
			} else if(start<=chunkMeta.lastTimestamp&&chunkMeta.firstTimestamp<=end){ // partial data chunk remove
				Chunk oldChunk = chunkMap.get(chunkMeta.firstTimestamp);
				Chunk newChunk = removeIntervalInChunk(oldChunk,start, end);
				if(newChunk!=null) {
					removeChunk(chunkMetaMap,chunkMap,chunkMeta);
					insertChunk(chunkMetaMap,chunkMap,newChunk);
					Logger.trace("chunk part reinserted");
				} else {
					Logger.error("chunk not removed (internal error): "+chunkMeta);
				}

			}
		}
	}

	/**
	 * returns a new chunk without data in interval
	 * @param chunk
	 * @param start
	 * @param end
	 * @return
	 */
	private static Chunk removeIntervalInChunk(Chunk chunk, int start, int end) {
		ArrayList<DataEntry> result = new ArrayList<DataEntry>(chunk.data.length);
		for(DataEntry value:chunk.data) {
			if(value.timestamp<start || end<value.timestamp) {
				result.add(value);
			}
		}
		return Chunk.of(result);
	}

	/**
	 * get meta, that is correct target of timestamp if present
	 * @param timestamp
	 */
	private ChunkMeta getChunkMeta(BTreeMap<Integer, ChunkMeta> chunkMetaMap, int timestamp) {
		int timestamp_year = TimeUtil.roundLowerYear(timestamp);
		int timestamp_next_year = TimeUtil.roundNextYear(timestamp);
		Integer key = chunkMetaMap.ceilingKey(timestamp_year);
		if(key==null) {
			return null;
		}
		if(timestamp_next_year<=key) {
			return null;
		}
		ChunkMeta chunkMeta = chunkMetaMap.get(key);
		throwNull(chunkMeta);
		return chunkMeta;
	}

	private void insertIntoOneChunk(BTreeMap<Integer, ChunkMeta> chunkMetaMap, BTreeMap<Integer, Chunk> chunkMap, ArrayList<DataEntry> entryList) {
		//int timestamp_chunk = TimeConverter.roundLowerYear(entryList.get(0).timestamp);
		int timestamp_next_year = TimeUtil.roundNextYear(entryList.get(0).timestamp);
		if(timestamp_next_year<=entryList.get(entryList.size()-1).timestamp) {
			throw new RuntimeException("data of more than one chunk");
		}
		//ChunkMeta oldChunkMeta = chunkMetaMap.get(timestamp_chunk);
		ChunkMeta oldChunkMeta = getChunkMeta(chunkMetaMap, entryList.get(0).timestamp);
		if(oldChunkMeta==null) {
			insertChunk(chunkMetaMap, chunkMap, Chunk.of(entryList));
		} else {
			Chunk oldChunk = chunkMap.get(oldChunkMeta.firstTimestamp);
			Iterator<DataEntry> oldIt = Arrays.stream(oldChunk.data).iterator();
			Iterator<DataEntry> newIt = entryList.iterator();
			ArrayList<DataEntry> resultList = new ArrayList<DataEntry>();

			DataEntry old_curr = oldIt.hasNext()?oldIt.next():null;			
			DataEntry new_curr = newIt.hasNext()?newIt.next():null;

			while(old_curr!=null||new_curr!=null) {				
				if(old_curr!=null) {
					if(new_curr!=null) {
						if(old_curr.timestamp==new_curr.timestamp) {// overwrite old data with new data
							resultList.add(new_curr);
							old_curr = oldIt.hasNext()?oldIt.next():null;
							new_curr = newIt.hasNext()?newIt.next():null;
						} else if(old_curr.timestamp<new_curr.timestamp) {
							resultList.add(old_curr);
							old_curr = oldIt.hasNext()?oldIt.next():null;
						} else {
							resultList.add(new_curr);
							new_curr = newIt.hasNext()?newIt.next():null;
						}
					} else {
						resultList.add(old_curr);
						old_curr = oldIt.hasNext()?oldIt.next():null;
					}
				} else {
					resultList.add(new_curr);
					new_curr = newIt.hasNext()?newIt.next():null;
				}				
			}

			removeChunk(chunkMetaMap, chunkMap, oldChunkMeta);
			insertChunk(chunkMetaMap, chunkMap, Chunk.of(resultList));
		}
	}

	private void removeChunk(BTreeMap<Integer, ChunkMeta> chunkMetaMap, BTreeMap<Integer, Chunk> chunkMap, ChunkMeta oldChunkMeta) {
		throwNull(chunkMetaMap);
		throwNull(chunkMap);
		throwNull(oldChunkMeta);
		//Logger.info("remove chunk "+oldChunkMeta);
		if(chunkMetaMap.remove(oldChunkMeta.firstTimestamp)==null) {
			Logger.error("could not remove oldChunkMeta");
		}
		if(chunkMap.remove(oldChunkMeta.firstTimestamp)==null) {
			Logger.error("could not remove old Chunk");
		}
	}

	private void insertChunk(BTreeMap<Integer, ChunkMeta> chunkMetaMap, BTreeMap<Integer, Chunk> chunkMap, Chunk chunk) {
		throwNull(chunkMetaMap);
		throwNull(chunkMap);
		throwNull(chunk);
		chunkMap.put(chunk.data[0].timestamp, chunk);
		chunkMetaMap.put(chunk.data[0].timestamp, new ChunkMeta(chunk.data[0].timestamp, chunk.data[chunk.data.length - 1].timestamp, chunk.data.length));
	}

	public NavigableSet<String> getStationNames() {
		return stationMetaMap.keySet();
	}

	public NavigableSet<String> getSensorNames(String stationName) {
		throwNull(stationName);
		StationMeta stationMeta = stationMetaMap.get(stationName);		
		if(stationMeta==null){
			Logger.warn("no station: "+stationName);
			return new TreeSet<String>();
		}
		BTreeMap<String, SensorMeta> sensorMap = db.getTreeMap(stationMeta.db_name_sensor_map);
		return sensorMap.keySet();
	}

	public StreamIterator getSensorIterator(String stationName, String sensorName, int minTimestamp, int maxTimestamp) {
		throwNull(stationName);
		throwNull(sensorName);
		SensorMeta sensorMeta = getSensorMeta(stationName,sensorName,false);
		if(sensorMeta==null) {
			return null;
		}
		return getSensorIterator(sensorMeta,minTimestamp,maxTimestamp);
	}

	public StreamIterator getSensorIterator(StationMeta stationMeta, String sensorName, int minTimestamp, int maxTimestamp) {
		throwNull(stationMeta);
		throwNull(sensorName);
		SensorMeta sensorMeta = getSensorMeta(stationMeta,sensorName,false);
		if(sensorMeta==null) {
			return null;
		}
		return getSensorIterator(sensorMeta,minTimestamp,maxTimestamp);
	}

	public StreamIterator getSensorIterator(SensorMeta sensorMeta, int minTimestamp, int maxTimestamp) {
		throwNull(sensorMeta);
		BTreeMap<Integer, Chunk> chunkMap = getSensorChunkMap(sensorMeta);
		BTreeMap<Integer, ChunkMeta> chunkMetaMap = getSensorChunkMetaMap(sensorMeta);
		return new StreamIterator(sensorMeta, chunkMetaMap, chunkMap, minTimestamp, maxTimestamp);	
	}
	
	public int getSensorTimeMin(SensorMeta sensorMeta, int minTimestamp, int maxTimestamp) {
		throwNull(sensorMeta);
		BTreeMap<Integer, ChunkMeta> chunkMetaMap = getSensorChunkMetaMap(sensorMeta);
		BTreeMap<Integer, Chunk> chunkMap = getSensorChunkMap(sensorMeta);
		Iterator<ChunkMeta> chunkMetaIterator = ChunkMeta.createIterator(chunkMetaMap, minTimestamp, maxTimestamp);
		while(chunkMetaIterator.hasNext()) {
			ChunkMeta chunkMeta = chunkMetaIterator.next();
			if(minTimestamp <= chunkMeta.firstTimestamp && maxTimestamp >= chunkMeta.firstTimestamp) {
				return chunkMeta.firstTimestamp;
			}
			Chunk chunk = chunkMap.get(chunkMeta.firstTimestamp);
			for(DataEntry e : chunk.data) {
				if(minTimestamp <= e.timestamp) {
					if(maxTimestamp < e.timestamp) {
						return Integer.MAX_VALUE;
					}
					return e.timestamp;
				}
			}
		}
		return Integer.MAX_VALUE;		
	}
	
	public int getSensorTimeMax(SensorMeta sensorMeta, int minTimestamp, int maxTimestamp) {
		throwNull(sensorMeta);
		BTreeMap<Integer, ChunkMeta> chunkMetaMap = getSensorChunkMetaMap(sensorMeta);
		BTreeMap<Integer, Chunk> chunkMap = getSensorChunkMap(sensorMeta);
		Iterator<ChunkMeta> chunkMetaIterator = ChunkMeta.createIterator(chunkMetaMap, minTimestamp, maxTimestamp);
		List<ChunkMeta> chunkMetas = new ArrayList<ChunkMeta>();
		while(chunkMetaIterator.hasNext()) {
			ChunkMeta chunkMeta = chunkMetaIterator.next();
			chunkMetas.add(chunkMeta);
		}
		Collections.reverse(chunkMetas);
		for(ChunkMeta chunkMeta : chunkMetas) {
			if(minTimestamp <= chunkMeta.lastTimestamp && maxTimestamp >= chunkMeta.lastTimestamp) {
				return chunkMeta.lastTimestamp;
			}
			Chunk chunk = chunkMap.get(chunkMeta.firstTimestamp);
			DataEntry[] chunkData = chunk.data;
			for(int i = chunkData.length - 1; i >= 0; i--) {
				DataEntry e = chunkData[i];
				if(maxTimestamp >= e.timestamp) {
					if(minTimestamp > e.timestamp) {
						return Integer.MIN_VALUE;
					}
					return e.timestamp;
				}
			}
		}
		return Integer.MIN_VALUE;
	}


	public StreamTsIterator getSensorTsIterator(String stationName, String sensorName, int minTimestamp, int maxTimestamp) {
		throwNull(stationName);
		throwNull(sensorName);
		StreamIterator it = getSensorIterator(stationName, sensorName, minTimestamp, maxTimestamp);
		if(it==null) {
			return null;
		}
		return new StreamTsIterator(it);
	}

	public TsIterator getTsIterator(String stationName, String[] sensorNames, int minTimestamp, int maxTimestamp) {
		throwNull(stationName);
		throwNullArray(sensorNames);
		if(sensorNames==null||sensorNames.length<1) {
			Logger.error("no sensors");
			return null;
		}
		if(sensorNames.length==1) {
			return getSensorTsIterator(stationName, sensorNames[0], minTimestamp, maxTimestamp);
		}
		StationMeta stationMeta = getStationMeta(stationName, false);
		if(stationMeta==null) {
			return null;
		}
		ArrayList<StreamIterator> streamIteratorList = new ArrayList<StreamIterator>();
		for(String sensorName:sensorNames) {			
			StreamIterator it = getSensorIterator(stationMeta, sensorName, minTimestamp, maxTimestamp);
			if(it!=null&&it.hasNext()) {
				streamIteratorList.add(it);
			}
		}
		return new RelationalIterator(streamIteratorList,sensorNames);
	}

	public BTreeMap<String, SensorMeta> getSensorMap(StationMeta stationMeta) {
		throwNull(stationMeta);
		return db.getTreeMap(stationMeta.db_name_sensor_map);
	}

	public BTreeMap<String, SensorMeta> getSensorMap(String stationName) {
		throwNull(stationName);
		StationMeta stationMeta = getStationMeta(stationName, false);
		if(stationMeta==null){
			return null;
		}		
		return getSensorMap(stationMeta);
	}

	public int[] getSensorTimeInterval(String stationName, String sensorName) {
		SensorMeta sensorMeta = getSensorMeta(stationName, sensorName);
		if(sensorMeta == null) {
			return null;
		}
		return getSensorTimeInterval(sensorMeta);
	}	

	public int[] getSensorTimeInterval(SensorMeta sensorMeta) {
		throwNull(sensorMeta);
		BTreeMap<Integer, ChunkMeta> chunkMetaMap = getSensorChunkMetaMap(sensorMeta);
		if(chunkMetaMap.isEmpty()) {
			return null;
		}
		int[] interval = new int[]{chunkMetaMap.firstKey(), chunkMetaMap.lastEntry().getValue().lastTimestamp};
		//Logger.info("interval " + TimeUtil.oleMinutesToText(interval[0]) + " - " + TimeUtil.oleMinutesToText(interval[1]) + "  " + sensorMeta.stationName + " " + sensorMeta.sensorName);
		return interval;
	}
	
	public int[] getSensorTimeInterval(String stationName, String sensorName, int min, int max) {
		SensorMeta sensorMeta = getSensorMeta(stationName, sensorName);
		if(sensorMeta == null) {
			return null;
		}
		return getSensorTimeInterval(sensorMeta, min, max);
	}
	
	public int[] getSensorTimeInterval(SensorMeta sensorMeta, int min, int max) {
		int imin = getSensorTimeMin(sensorMeta, min, max);
		int imax = getSensorTimeMax(sensorMeta, min, max);
		//Logger.info("interval " + imin + " " + imax);
		if(imin == Integer.MAX_VALUE || imax == Integer.MIN_VALUE) {
			return null;
		}
		int[] interval = new int[]{imin, imax};
		//Logger.info("interval " + TimeUtil.oleMinutesToText(interval[0]) + " - " + TimeUtil.oleMinutesToText(interval[1]) + "  " + sensorMeta.stationName + " " + sensorMeta.sensorName);
		return interval;
	}
	
	public int[] getSensorTimeInterval_OLD(SensorMeta sensorMeta, int min, int max) {
		throwNull(sensorMeta);
		BTreeMap<Integer, ChunkMeta> chunkMetaMap = getSensorChunkMetaMap(sensorMeta);
		if(chunkMetaMap.isEmpty()) {
			return null;
		}
		int imin = chunkMetaMap.firstKey();
		if(imin > max) {
			return null;
		}
		int imax = chunkMetaMap.lastEntry().getValue().lastTimestamp;
		if(imax < min) {
			return null;
		}
		if(imin < min) {
			StreamIterator it = getSensorIterator(sensorMeta, min, max);
			if(!it.hasNext()) {
				return null;
			}
			DataEntry e = it.next();
			imin = e.timestamp;
		}
		if(imax > max) {
			StreamIterator it = getSensorIterator(sensorMeta, min, max);
			DataEntry e = null;
			while(it.hasNext()) {
				e = it.next();
			}
			if(e == null) {
				return null;
			}
			imax = e.timestamp;
		}
		int[] interval = new int[]{imin, imax};
		Logger.info("interval " + TimeUtil.oleMinutesToText(interval[0]) + " - " + TimeUtil.oleMinutesToText(interval[1]) + "  " + sensorMeta.stationName + " " + sensorMeta.sensorName);
		return interval;
	}

	public int[] getStationTimeInterval(String stationName) {
		throwNull(stationName);
		BTreeMap<String, SensorMeta> sensorMap = getSensorMap(stationName);
		if(sensorMap==null||sensorMap.isEmpty()) {
			return null;
		}
		int minTimestamp = Integer.MAX_VALUE;
		int maxTimestamp = Integer.MIN_VALUE;
		for(SensorMeta sensorMeta:sensorMap.values()) {
			int[] interval = getSensorTimeInterval(sensorMeta);
			if(interval!=null) {
				if(interval[0]<minTimestamp) {
					minTimestamp = interval[0];
				}
				if(maxTimestamp<interval[1]) {
					maxTimestamp = interval[1];
				}
			}
		}
		if(minTimestamp == Integer.MAX_VALUE || maxTimestamp == Integer.MIN_VALUE) {
			return null;
		}
		return new int[]{minTimestamp, maxTimestamp};	
	}
	
	public int[] getStationTimeInterval(String stationName, int min, int max) {
		throwNull(stationName);
		BTreeMap<String, SensorMeta> sensorMap = getSensorMap(stationName);
		if(sensorMap == null||sensorMap.isEmpty()) {
			return null;
		}
		int minTimestamp = Integer.MAX_VALUE;
		int maxTimestamp = Integer.MIN_VALUE;
		for(SensorMeta sensorMeta:sensorMap.values()) {
			int[] interval = getSensorTimeInterval(sensorMeta, min, max);
			if(interval != null) {
				if(interval[0] < minTimestamp) {
					minTimestamp = interval[0];
				}
				if(maxTimestamp < interval[1]) {
					maxTimestamp = interval[1];
				}
			}
		}
		if(minTimestamp == Integer.MAX_VALUE || maxTimestamp == Integer.MIN_VALUE) {
			return null;
		}
		return new int[]{minTimestamp, maxTimestamp};	
	}

	public void printStatistics() {
		for(StationMeta stationMeta:stationMetaMap.values()) {
			System.out.println(stationMeta.stationName);
			for(SensorMeta sensorMeta:getSensorMap(stationMeta).values()) {
				BTreeMap<Integer, ChunkMeta> sensorChunkMetaMap = getSensorChunkMetaMap(sensorMeta);
				int entryCount = 0;
				for(ChunkMeta chunkMeta: sensorChunkMetaMap.values()) {
					entryCount += chunkMeta.entryCount;
				}
				BTreeMap<Integer, Chunk> sensorChunkMap = getSensorChunkMap(sensorMeta);
				System.out.print(sensorMeta.sensorName+" "+sensorChunkMetaMap.size()+";"+sensorChunkMap.size()+":"+entryCount+"   ");
			}
			System.out.println();
		}

		for(String key:db.getAll().keySet()) {
			System.out.println(key);
		}

	}

	public void compact() {
		Logger.warn("ignore db compact: unfixed bug in compact");
		//db.compact();
	}

	public void removeInterval(String stationName, int start, int end) {//TODO remove empty streams and stations
		NavigableSet<String> sensorNames = getSensorNames(stationName);
		if(sensorNames.isEmpty()) {
			Logger.info("no sensors in station -> nothing removed: "+stationName);
			return;
		}
		for(String sensorName:sensorNames) {
			Logger.trace("remove "+stationName+"/"+sensorName+"  "+start+"  "+end);
			removeSensorData(stationName, sensorName, start, end);
		}
	}

	public void clearMaskOfStation(String stationName) {
		StationMeta stationMeta = getStationMeta(stationName, false);
		if(stationMeta==null) {
			//Logger.warn("station not found "+stationName);
			return;
		}
		BTreeMap<String, TimeSeriesMask> maskMap = db.getTreeMap(stationMeta.db_name_sensor_time_series_mask_map);
		maskMap.clear();		
	}
}
