package tsdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import tsdb.component.Region;
import tsdb.util.Interval;

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

	public final Interval viewTimeRange; //nullable

	public List<String> assigned_plots; //nullable

	public GeneralStation(String name, Region region, String longName, String group, Interval viewTimeRange, List<String> assigned_plots) {
		this.name = name;
		this.region = region;
		this.longName = longName;
		this.stationList = new ArrayList<Station>();
		this.sensorNameTranlationMap = new HashMap<String,String>();
		this.virtualPlots = new ArrayList<VirtualPlot>();
		this.group = group;
		this.viewTimeRange = viewTimeRange;
		this.assigned_plots = assigned_plots;
	}

	public Stream<String> getStationAndVirtualPlotNames() {
		Stream<String> stationStream = stationList.stream().map(s->s.stationID);
		Stream<String> virtualPlotStream = virtualPlots.stream().map(v->v.plotID);
		return Stream.concat(stationStream, virtualPlotStream);
	}

	@Override
	public String toString() {
		return "GeneralStation [name=" + name + ", region=" + region + ", longName=" + longName + ", assigned_plots " + assigned_plots + "]";
	}


	public static class GeneralStationBuilder {

		public String name;
		public Region region;
		public String longName;
		public String group;
		public Interval viewTimeRange; //nullable
		public List<String> assigned_plots;  //nullable

		public GeneralStationBuilder(String name) {
			this.name = name;
		}

		public GeneralStation build() {			                  
			if(longName==null) {
				longName = name;
			}
			if(group==null) {
				group = name;
			}
			return new GeneralStation(name, region, longName, group, viewTimeRange, assigned_plots);
		}

		public void addAssigned_plots(String[] plots) {			
			List<String> list = assigned_plots == null ? new ArrayList<String>() : assigned_plots;			
			for(String plot : plots) {
				plot = plot.trim();
				if(!plot.isEmpty()) {
					list.add(plot);
				}
			}			
			assigned_plots = list.isEmpty() ? null : list;	
		}
	}
}
