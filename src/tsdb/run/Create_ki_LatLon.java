package tsdb.run;

import java.io.FileNotFoundException;

import org.gdal.gdal.gdal;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.VirtualPlot;

public class Create_ki_LatLon {	
	//private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) throws FileNotFoundException {

		TsDB tsdb = TsDBFactory.createDefault();

		gdal.AllRegister();

		SpatialReference src = new SpatialReference("");
		src.ImportFromProj4("+proj=utm +zone=37 +south +ellps=clrk80 +towgs84=-160,-6,-302,0,0,0,0 +units=m +no_defs");
		SpatialReference dst = new SpatialReference("");		
		dst.ImportFromProj4("+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs");
		CoordinateTransformation ct = CoordinateTransformation.CreateCoordinateTransformation(src, dst);

		System.out.println("PlotID"+","+"Lat"+","+"Lon");
		
		
		for(VirtualPlot virtualPlots:tsdb.getVirtualPlots()) {
			if(virtualPlots.generalStation.region.name.equals("KI")&&!Float.isNaN(virtualPlots.geoPosEasting)&&!Float.isNaN(virtualPlots.geoPosNorthing)) {
				//System.out.println(":["+virtualPlots.geoPosEasting+","+virtualPlots.geoPosNorthing+"]");
				double[] d = ct.TransformPoint(virtualPlots.geoPosEasting,virtualPlots.geoPosNorthing);			
				//System.out.println("['"+virtualPlots.plotID+"',"+d[1]+","+d[0]+"],");
				System.out.println(virtualPlots.plotID+","+d[1]+","+d[0]);
			}
		}
	}
}
