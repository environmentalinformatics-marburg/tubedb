package tsdb.loader.bale;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.opencsv.CSVReader;

import tsdb.util.Table;

public class TOA5Table extends Table {
	//
	
	public final String[] metaHeader;
	public final String[] columnsHeader;
	public final String[] unitsHaeder;
	public final String[] aggregationsHeader;
	
	public final String recordingName;	

	public TOA5Table(String filename) throws FileNotFoundException, IOException {
		try(CSVReader reader = new CSVReader(new FileReader(filename))) {
			
			this.metaHeader = reader.readNext();
			
			if(!metaHeader[0].equals("TOA5")) {
				throw new RuntimeException("missing TOA5 marker");
			}
			
			this.recordingName = metaHeader[1];
			
			
			this.columnsHeader = reader.readNext();
			this.unitsHaeder = reader.readNext();
			this.aggregationsHeader = reader.readNext();

			//Logger.info("meta ("+metaHeader.length+") "+Arrays.toString(metaHeader));
			//Logger.info("columns ("+columnsHeader.length+") "+Arrays.toString(columnsHeader));
			//Logger.info("units ("+unitsHaeder.length+") "+Arrays.toString(unitsHaeder));
			//Logger.info("aggregations ("+aggregationsHeader.length+") "+Arrays.toString(aggregationsHeader));
			
			this.updateNames(columnsHeader);
			
			//List<String[]> rows = reader.readAll();  // very slow because of linkedlist for indexed access			
			ArrayList<String[]> dataRowList = new ArrayList<String[]>();
			String[] curRow = reader.readNext();
			while(curRow != null){
				dataRowList.add(curRow);
				curRow = reader.readNext();
			}				
			String[][] tabeRows = dataRowList.toArray(new String[0][]);			
			this.rows = tabeRows;
		}
	}
	
	public boolean metaHeaderContains(String s) {
		for(String h:metaHeader) {
			if(h.equals(s)) {
				return true;
			}
		}
		return false;
	}

}
