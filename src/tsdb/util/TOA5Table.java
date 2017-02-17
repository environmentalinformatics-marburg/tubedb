package tsdb.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVReader;

public class TOA5Table extends Table {
	private static final Logger log = LogManager.getLogger();
	
	

	public TOA5Table(String filename) throws FileNotFoundException, IOException {
		try(CSVReader reader = new CSVReader(new FileReader(filename))) {
			
			String[] metaHeader = reader.readNext();
			String[] columnsHeader = reader.readNext();
			String[] unitsHaeder = reader.readNext();
			String[] aggregationsHeader = reader.readNext();

			//log.info("meta ("+metaHeader.length+") "+Arrays.toString(metaHeader));
			//log.info("columns ("+columnsHeader.length+") "+Arrays.toString(columnsHeader));
			//log.info("units ("+unitsHaeder.length+") "+Arrays.toString(unitsHaeder));
			//log.info("aggregations ("+aggregationsHeader.length+") "+Arrays.toString(aggregationsHeader));
			
			this.updateNames(columnsHeader);
			List<String[]> rows = reader.readAll();			
			this.rows = rows.toArray(new String[0][]);
			
			
			
			
			Y y = ColumnReaderSpaceTimestamp::new;

		}

	}

}
