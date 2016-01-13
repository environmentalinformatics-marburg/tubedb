package tsdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import tsdb.component.Region;

/**
 * This class contains metadata that is associated with a group of stations like HEG or HEW.
 * @author woellauer
 *
 */
public class GeneralStation {
	
	public String name;
	
	public final Region region;
	
	public final String longName;
	
	public Map<String,String> sensorNameTranlationMap;
	
	public List<Station> stationList;
	
	public List<VirtualPlot> virtualPlots;
	
	public final String group;//not null //  if no group: name of general station
	
	public GeneralStation(String name, Region region, String longName, String group) {
		this.name = name;
		this.region = region;
		this.longName = longName;
		this.stationList = new ArrayList<Station>();
		this.sensorNameTranlationMap = new HashMap<String,String>();
		this.virtualPlots = new ArrayList<VirtualPlot>();
		this.group = group;
	}
	
	public Stream<String> getStationAndVirtualPlotNames() {
		Stream<String> stationStream = stationList.stream().map(s->s.stationID);
		Stream<String> virtualPlotStream = virtualPlots.stream().map(v->v.plotID);
		return Stream.concat(stationStream, virtualPlotStream);
	}

}
