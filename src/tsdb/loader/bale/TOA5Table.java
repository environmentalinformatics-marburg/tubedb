package tsdb.loader.bale;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVReader;

import tsdb.util.Table;

public class TOA5Table extends Table {
	//private static final Logger log = LogManager.getLogger();
	
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

			//log.info("meta ("+metaHeader.length+") "+Arrays.toString(metaHeader));
			//log.info("columns ("+columnsHeader.length+") "+Arrays.toString(columnsHeader));
			//log.info("units ("+unitsHaeder.length+") "+Arrays.toString(unitsHaeder));
			//log.info("aggregations ("+aggregationsHeader.length+") "+Arrays.toString(aggregationsHeader));
			
			this.updateNames(columnsHeader);
			List<String[]> rows = reader.readAll();			
			this.rows = rows.toArray(new String[0][]);
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
