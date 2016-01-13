package tsdb.remote;

import java.io.Serializable;
import java.util.List;

import tsdb.Station;
import tsdb.StationProperties;
import tsdb.component.LoggerType;
import tsdb.util.TimestampInterval;
import tsdb.util.Util;

/**
 * Serializable station info
 * immutable (Fields should not be changed.)
 * @author woellauer
 *
 */
public class StationInfo implements Serializable {

	private static final long serialVersionUID = -5759969271465519328L;
	public final String stationID;
	public final LoggerType loggerType;
	public final double geoPoslongitude;
	public final double geoPosLatitude;
	public final GeneralStationInfo generalStationInfo;
	public final String alternativeID;
	public final List<TimestampInterval<StationProperties>> intervalList;

	public StationInfo(Station station) {
		stationID = station.stationID;
		loggerType = station.loggerType;
		geoPoslongitude = station.geoPosLongitude;
		geoPosLatitude = station.geoPosLatitude;
		generalStationInfo = Util.ifnull(station.generalStation, gs->new GeneralStationInfo(gs));
		alternativeID = station.alternativeID;
		intervalList = station.propertiesList;
	}		
}