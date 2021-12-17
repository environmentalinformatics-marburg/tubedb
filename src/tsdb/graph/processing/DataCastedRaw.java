package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNulls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.tinylog.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Node;
import tsdb.iterator.DataCastRawIterator;
import tsdb.util.Util;
import tsdb.util.iterator.TsIterator;

public class DataCastedRaw extends Node.Abstract {
	

	private final String[] schema; //not null
	private final List<Node> sources; //not null
	private final boolean _constant_timestep;
	private final boolean _continuous;
	private final int[][] inputIndices;

	public DataCastedRaw(TsDB tsdb, String[] schema, List<Node> sources, int[][] inputIndices) {
		super(tsdb);
		throwNulls(sources, inputIndices);
		if(sources.isEmpty()) {
			throw new RuntimeException("no sources");	
		}
		this.inputIndices = inputIndices;
		this.schema = schema;
		this.sources = sources;
		this._constant_timestep = sources.get(0).isConstantTimestep();
		this._continuous = sources.get(0).isContinuous();
		for(Node source:sources) {
			if(source.isConstantTimestep() != _constant_timestep) {
				throw new RuntimeException("different source types");
			}
			if(source.isContinuous() != _continuous) {
				throw new RuntimeException("different source types");
			}
		}
	}

	public static DataCastedRaw of(TsDB tsdb, List<Node> sources, String[] outputSensorNames) {
		@SuppressWarnings("unchecked")
		Map<String, Integer>[] schemaMaps = new Map[sources.size()];
		int[][] inputIndices = new int[sources.size()][];
		for (int i = 0; i < sources.size(); i++) {
			Node source = sources.get(i);
			String[] schema = source.getSchema();
			Map<String, Integer> schemaMap = Util.stringArrayToMap(schema);
			schemaMaps[i] = schemaMap;
			int sourceSchemaLen = source.getSchema().length;
			inputIndices[i] = new int[sourceSchemaLen];
			for(int j = 0; j < sourceSchemaLen; j++) {
				inputIndices[i][j] = -1; // set all sensors to not include
			}
		}
		ArrayList<String> schemaList = new ArrayList<String>();
		int outputPos = 0;
		//Logger.info(schemaSet);
		for(String name : outputSensorNames) {
			//Logger.info("sensor: " + name);
			for (int i = 0; i < sources.size(); i++) {
				Node source = sources.get(i);
				String sourceName = source.getSourceName();
				if(schemaMaps[i].containsKey(name)) {
					int inputPos = schemaMaps[i].get(name);
					inputIndices[i][inputPos] = outputPos++;
					String outputName = sourceName + "." + name; 
					schemaList.add(outputName);
					//Logger.info("it " + i + ":   " + inputPos + " " + name +" -> " + inputIndices[i][inputPos] + " " + outputName + "  " + Arrays.toString(inputIndices[i]));
				}
			}
		}
		/*for (int i = 0; i < sources.size(); i++) {
			Logger.info("source " + i + "   " + Arrays.toString(inputIndices[i]) + " " + Arrays.toString(sources.get(i).getSchema()));
		}*/
		return new DataCastedRaw(tsdb, schemaList.toArray(new String[0]), sources, inputIndices);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		if(start==null||end==null) {
			long[] interval = new long[]{Long.MAX_VALUE,Long.MIN_VALUE};
			sources.stream().map(s->tsdb.getTimeInterval(s.getSourceStation().stationID)).forEach(i->{
				if(i[0]<interval[0]) {
					interval[0] = i[0];
				}
				if(i[1]>interval[1]) {
					interval[1] = i[1];
				}
			});
			/*if(interval==null) {
				return null;
			}*/
			if(start==null) {
				start = interval[0];
			}
			if(end==null) {
				end = interval[1];
			}
		}
		return getExactly(start,end);
	}

	public TsIterator getExactly(long start, long end) {		
		List<TsIterator> iteratorList = new ArrayList<>();		
		for(Node source:sources) {
			TsIterator it = source.get(start, end);
			if(it != null && it.hasNext()) {
				iteratorList.add(it);				
			}
		}
		return new DataCastRawIterator(schema, iteratorList.toArray(new TsIterator[0]), inputIndices);
	}

	@Override
	public Station getSourceStation() { // multiple source stations
		return null;
	}

	@Override
	public String[] getSchema() {
		return schema;
	}

	@Override
	public boolean isConstantTimestep() {
		return _constant_timestep;
	}

	@Override
	public VirtualPlot getSourceVirtualPlot() { // multiple source stations
		return null;
	}

	@Override
	public long[] getTimestampInterval() {//maximum interval
		long[] interval = new long[]{Long.MAX_VALUE,Long.MIN_VALUE};
		sources.stream().map(s->s.getTimestampInterval()).forEach(i->{
			if(i[0]<interval[0]) {
				interval[0] = i[0];
			}
			if(i[1]>interval[1]) {
				interval[1] = i[1];
			}
		});		
		if(interval[0]==Long.MAX_VALUE || interval[1]==Long.MIN_VALUE) {
			return null;
		}
		return interval;
	}

	public String getSourceText() {
		String s="";
		for(Node source:sources) {
			s+=source.getSourceName()+" ";
		}
		return s;
	}

	@Override
	public boolean isContinuous() {
		return _continuous;
	}
}
