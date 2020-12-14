package tsdb.remote;

import static tsdb.util.AssumptionCheck.throwNulls;

import java.io.Serializable;

import tsdb.GeneralStation;
import tsdb.component.Region;
import tsdb.util.Interval;

/**
 * Serializable general station info
 * immutable
 * @author woellauer
 */
public class GeneralStationInfo implements Serializable, Comparable<GeneralStationInfo> {	
	private static final long serialVersionUID = -5021875538014695128L;
	
	public final String name;
	public final String longName;
	public final String group;
	public final Region region;
	public final Interval viewTimeRange; //nullable
	public final int stationCount;
	public final int virtualPlotCount;
	public final String[] assigned_plots;
	
	public GeneralStationInfo(GeneralStation generalStation) {
		this.name = generalStation.name;
		this.longName = generalStation.longName;
		this.group = generalStation.group;
		this.region = generalStation.region;
		this.viewTimeRange = generalStation.viewTimeRange;
		this.stationCount = generalStation.stationList.size();
		this.virtualPlotCount = generalStation.virtualPlots.size();
		this.assigned_plots = generalStation.assigned_plots == null ? null : generalStation.assigned_plots.toArray(new String[0]);
	}
	
	public GeneralStationInfo(String generalName, String regionName) {
		throwNulls(generalName, regionName);
		this.name = generalName;
		this.longName = generalName;
		this.group = generalName;
		this.region = new Region(regionName, regionName);
		this.viewTimeRange = null;
		this.stationCount = 0;
		this.virtualPlotCount = 0;
		this.assigned_plots = null;
	}
	
	public String getName() {
		return name;
	}
	
	public String getGroup() {
		return group;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeneralStationInfo other = (GeneralStationInfo) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int compareTo(GeneralStationInfo o) {
		return this.name.compareTo(o.name);
	}

}
