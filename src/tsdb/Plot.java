package tsdb;

import java.util.stream.Stream;
import tsdb.util.Util;

/**
 * Interface for common functionality of virtual Plots and stations that are plots.
 * @author woellauer
 *
 */
public interface Plot {
	
	Stream<Plot> getNearestPlots();
	String[] getSensorNames();
	String getPlotID();
	boolean existData(String sensorName);
	boolean existData();
	default String[] getValidSchemaEntries(String[] querySchema) {
		return Util.getValidEntries(querySchema, getSensorNames());
	}
	
	String[] getValidSchemaEntriesWithVirtualSensors(String[] querySchema);
	
	double[] getLatLon();
	double getElevation();
	
	public static Real of(Station station) {
		return new Real(station);
	}
	
	public static Virtual of(VirtualPlot virtualPlot) {
		return new Virtual(virtualPlot);
	}
	
	class Real implements Plot {		
		public final Station station;		
		public Real(Station station) {
			this.station = station;
		}		
		@Override
		public Stream<Plot> getNearestPlots() {
			return station.nearestStations.stream().map(s->new Real(s));			
		}
		@Override
		public String getPlotID() {
			return station.stationID;
		}
		@Override
		public String[] getSensorNames() {
			return station.getSensorNames();
		}
		@Override
		public boolean existData() {
			return station.existData();
		}
		@Override
		public boolean existData(String sensorName) {
			return station.existData(sensorName);
		}
		@Override
		public String[] getValidSchemaEntriesWithVirtualSensors(String[] querySchema) {
			return station.getValidSchemaEntriesWithVirtualSensors(querySchema);
		}
		@Override
		public String toString() {
			return getPlotID();
		}
		@Override
		public double[] getLatLon() {
			return new double[]{station.geoPosLatitude, station.geoPosLongitude};
		}
		@Override
		public double getElevation() {
			return station.elevation;
		}
	}
		
	class Virtual implements Plot {		
		public final VirtualPlot virtualPlot;		
		public Virtual(VirtualPlot virtualPlot) {
			this.virtualPlot = virtualPlot;
		}		
		@Override
		public Stream<Plot> getNearestPlots() {
			return virtualPlot.nearestVirtualPlots.stream().map(s->new Virtual(s));			
		}
		@Override
		public String getPlotID() {
			return virtualPlot.plotID;
		}
		@Override
		public String[] getSensorNames() {
			return virtualPlot.getSensorNames();
		}
		@Override
		public boolean existData() {
			return virtualPlot.existData();
		}
		@Override
		public boolean existData(String sensorName) {
			return virtualPlot.existData(sensorName);
		}
		@Override
		public String[] getValidSchemaEntriesWithVirtualSensors(String[] querySchema) {
			return virtualPlot.getValidSchemaEntriesWithVirtualSensors(querySchema);
		}
		@Override
		public String toString() {
			return getPlotID();
		}
		@Override
		public double[] getLatLon() {
			return new double[]{virtualPlot.geoPosLatitude, virtualPlot.geoPosLongitude};
		}
		@Override
		public double getElevation() {
			return virtualPlot.elevation;
		}
	}
}
