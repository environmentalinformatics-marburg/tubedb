package tsdb.remote;

import static tsdb.util.AssumptionCheck.throwFalse;
import static tsdb.util.AssumptionCheck.throwNull;

import java.io.Serializable;

import tsdb.Station;
import tsdb.VirtualPlot;

/**
 * Serializable plot info
 * immutable
 * @author woellauer
 */
public class PlotInfo implements Serializable {	
	private static final long serialVersionUID = -304301220496293360L;

	public final String name;

	public final GeneralStationInfo generalStationInfo;
	public final boolean isStation;
	public final boolean isVIP;
	public final double geoPosLatitude;
	public final double geoPosLongitude;
	public final float elevation;
	public final String loggerTypeName;
	
	public PlotInfo(VirtualPlot virtualPlot) {
		this.name = virtualPlot.plotID;
		this.generalStationInfo = new GeneralStationInfo(virtualPlot.generalStation);
		this.isStation = false;
		this.isVIP = virtualPlot.isFocalPlot;
		//this.geoPos = new double[]{virtualPlot.geoPosEasting,virtualPlot.geoPosNorthing,virtualPlot.elevation};
		this.geoPosLatitude = virtualPlot.geoPosLatitude;
		this.geoPosLongitude = virtualPlot.geoPosLongitude;
		this.elevation = virtualPlot.elevation;
		this.loggerTypeName = "virtual";
	}
	
	public PlotInfo(Station station) {
		throwFalse(station.isPlot);
		this.name = station.stationID;
		this.generalStationInfo = new GeneralStationInfo(station.generalStation);
		this.isStation = true;
		this.isVIP = station.isVIP();
		this.geoPosLatitude = station.geoPosLatitude;
		this.geoPosLongitude = station.geoPosLongitude;
		this.elevation = Float.NaN;
		if(station.loggerType!=null) {
			this.loggerTypeName = station.loggerType.typeName;
		} else {
			this.loggerTypeName = "unknown";
		}
	}
	
	public PlotInfo(String name, String generalName, String regionName) {
		throwNull(name);
		this.name = name;
		this.generalStationInfo = new GeneralStationInfo(generalName, regionName);
		this.isStation = false;
		this.isVIP = false;
		this.geoPosLatitude = Double.NaN;
		this.geoPosLongitude = Double.NaN;
		this.elevation = Float.NaN;
		this.loggerTypeName = "unknown";
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
		PlotInfo other = (PlotInfo) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
