package tsdb.remote;

import static tsdb.util.AssumptionCheck.throwNulls;

import java.io.Serializable;

import tsdb.GeneralStation;
import tsdb.component.Region;

/**
 * Serializable general station info
 * immutable
 * @author woellauer
 */
public class GeneralStationInfo implements Serializable {	
	private static final long serialVersionUID = -5021875538014695128L;
	
	public final String name;
	public final String longName;
	public final String group;
	public final Region region;
	public final int stationCount;
	public final int virtualPlotCount;
	
	public GeneralStationInfo(GeneralStation generalStation) {
		this.name = generalStation.name;
		this.longName = generalStation.longName;
		this.group = generalStation.group;
		this.region = generalStation.region;
		this.stationCount = generalStation.stationList.size();
		this.virtualPlotCount = generalStation.virtualPlots.size();
	}
	
	public GeneralStationInfo(String generalName, String regionName) {
		throwNulls(generalName, regionName);
		this.name = generalName;
		this.longName = generalName;
		this.group = generalName;
		this.region = new Region(regionName, regionName);
		this.stationCount = 0;
		this.virtualPlotCount = 0;
	}
	
	public String getName() {
		return name;
	}
	
	public String getGroup() {
		return group;
	}

}
