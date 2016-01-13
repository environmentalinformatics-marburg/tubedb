package tsdb.run;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.VirtualPlot;
import static tsdb.util.AssumptionCheck.*;

/**
 * create elevation from plot geo positions
 * needs path set to gdal DLLs
 * @author woellauer
 *
 */
public class Create_ki_elevation {
	
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) throws FileNotFoundException {

		//open database
		TsDB tsdb = TsDBFactory.createDefault();
		//cof1 Easting:305573 Northing:9641686 Elevation:1303.08

		//load geoTiff library
		gdal.AllRegister();

		//read geoTiff
		String filename = TsDBFactory.get_CSV_output_directory()+"DEM_ARC1960_30m_Hemp.tif";
		Dataset dataset = gdal.Open(filename);
		System.out.println("Description: "+dataset.GetDescription());
		System.out.println("Metadata: "+dataset.GetMetadata_Dict());
		System.out.println("Projection: "+dataset.GetProjection());
		System.out.println("Projection: "+dataset.GetGCPProjection());
		System.out.println("RasterCount: "+dataset.GetRasterCount());
		System.out.println("RasterXSize: "+dataset.getRasterXSize());
		System.out.println("RasterYSize: "+dataset.getRasterYSize());

		//get transform coefficients:  pixel pos -> geo pos 
		double[] GT = dataset.GetGeoTransform();
		for(int i=0;i<GT.length;i++) {
			System.out.println(i+". geo: "+GT[i]);
		}
		
		//get transform coefficients:  geo pos -> pixel pos
		double[] invGT = gdal.InvGeoTransform(GT);

		Band band = dataset.GetRasterBand(1);

		System.out.println("BlockXSize: "+band.GetBlockXSize());
		System.out.println("BlockYSize: "+band.GetBlockYSize());
		System.out.println("DataType: "+band.getDataType());
		System.out.println("GDT_Float32: "+gdalconst.GDT_Float32);
		throwFalse(band.getDataType()==gdalconst.GDT_Float32, ()->"unexpected data type: "+band.getDataType());


		//source geo pos projection
		SpatialReference src = new SpatialReference("");
		src.ImportFromProj4("+proj=utm +zone=37 +south +ellps=clrk80 +towgs84=-160,-6,-302,0,0,0,0 +units=m +no_defs");
		SpatialReference dst = new SpatialReference("");
		
		//destination geo pos projection of geoTiff
		dst.ImportFromWkt(dataset.GetProjection());
		log.info(dataset.GetProjection());
		CoordinateTransformation ct = CoordinateTransformation.CreateCoordinateTransformation(src, dst);		
		
		//create csv output file
		PrintStream out = new PrintStream(new FileOutputStream(TsDBFactory.get_CSV_output_directory()+"ki_elevation.csv"));
		
		//csv header
		out.println("PlotID,Elevation");

		for(VirtualPlot virtualPlot:tsdb.getVirtualPlots()) {
			double[] tranformed = ct.TransformPoint(virtualPlot.geoPosEasting, virtualPlot.geoPosNorthing);
			double[] resultX = new double[1];
			double[] resultY = new double[1];
			gdal.ApplyGeoTransform(invGT, tranformed[0], tranformed[1], resultX, resultY);
			int xoff = (int)resultX[0];
			int yoff = (int)resultY[0];
			int xsize = 1;
			int ysize = 1;
			int buf_xsize = 1;
			int buf_ysize = 1;
			int buf_type = gdalconstConstants.GDT_Float32;
			float[] array = new float[1];
			try {
				if(band.ReadRaster(xoff, yoff, xsize, ysize, buf_xsize, buf_ysize, buf_type, array)==0) {
					System.out.println(virtualPlot.plotID+": "+virtualPlot.geoPosEasting+","+virtualPlot.geoPosNorthing+" -> "+xoff+","+yoff+" -> "+array[0]);
					out.println(virtualPlot.plotID+","+array[0]);
				} else {
					log.warn("error in get elevation: "+virtualPlot.plotID+"  "+virtualPlot.geoPosEasting+","+virtualPlot.geoPosNorthing);
				}
			} catch(Exception e) {
				log.warn("error in get elevation: "+virtualPlot.plotID+"    "+e);
			}
		}
		
		out.close();
	}
}
