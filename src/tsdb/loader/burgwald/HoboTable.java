package tsdb.loader.burgwald;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


import org.tinylog.Logger;

import com.opencsv.CSVReader;

import tsdb.util.Table;

public class HoboTable extends Table {
	
	
	public final String plotID;
	public final String[] columnsHeader;


	public HoboTable(String filename) throws FileNotFoundException, IOException {
		try(CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {			
			String[] firstLine = reader.readNext();			
			if(firstLine.length != 1) {
				throw new RuntimeException("invalid HOBO header: "+Arrays.toString(firstLine));
			}			
			String metaHeader = firstLine[0].startsWith("\uFEFF") ? firstLine[0].substring(1) : firstLine[0];
			if(!metaHeader.startsWith("Plot-Titel: ")) {
				throw new RuntimeException("invalid HOBO header: |"+metaHeader+"|");
			}
			this.plotID = metaHeader.substring(12).trim();
			//Logger.info("|" + plotID + "|");			
			
			this.columnsHeader = reader.readNext();
			
			//Logger.info(Arrays.toString(columnsHeader));
			String[] names = new String[columnsHeader.length];
			for (int i = 0; i < names.length; i++) {
				String name = columnsHeader[i];
				int sep1 = name.indexOf(',');
				int sep2 = name.indexOf('(');
				if(sep1 < 0) {
					if(sep2 < 0) {
						names[i] = name.trim();
					} else {
						names[i] = name.substring(0, sep2).trim();
					}
				} else {
					if(sep2 < 0) {
						names[i] = name.substring(0, sep1).trim();
					} else {
						if(sep1 < sep2) {
							names[i] = name.substring(0, sep1).trim();
						} else {
							names[i] = name.substring(0, sep2).trim();
						}
					}
				}				
			}
			//Logger.info(Arrays.toString(names));			
			this.updateNames(names);
			
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
	


}
