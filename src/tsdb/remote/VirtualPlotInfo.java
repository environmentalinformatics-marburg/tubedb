package tsdb.remote;

import java.io.Serializable;
import java.util.List;

import tsdb.StationProperties;
import tsdb.VirtualPlot;
import tsdb.util.TimestampInterval;
import tsdb.util.Util;

/**
 * Serializable virtual plot info
 * immutable (Fields should not be changed.)
 * @author woellauer
 *
 */
public class VirtualPlotInfo implements Serializable {
	
	private static final long serialVersionUID = -288110404460514296L;
	public final String plotID;
	public final GeneralStationInfo generalStationInfo;
	public final int geoPosEasting;
	public final int geoPosNorthing;
	public final double geoPosLatitude;
	public final double geoPosLongitude;	
	public final float elevation;
	public final float elevationTemperature;
	public final List<TimestampInterval<StationProperties>> intervalList;
	
	public VirtualPlotInfo(VirtualPlot virtualPlot) {		
		this.plotID = virtualPlot.plotID;
		this.generalStationInfo= Util.ifnull(virtualPlot.generalStation, gs->new GeneralStationInfo(gs));
		this.geoPosEasting = (int)virtualPlot.geoPosEasting;
		this.geoPosNorthing = (int)virtualPlot.geoPosNorthing;
		this.intervalList = virtualPlot.intervalList;
		this.elevation = virtualPlot.elevation;
		this.geoPosLatitude = virtualPlot.geoPosLatitude;
		this.geoPosLongitude = virtualPlot.geoPosLongitude;
		this.elevationTemperature = virtualPlot.elevationTemperature;
	}
}
