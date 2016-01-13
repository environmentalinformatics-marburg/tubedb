package tsdb.util.iterator;

/**
 * time columns in CSV output files
 * @author woellauer
 *
 */
public enum CSVTimeType {
	TIMESTAMP,	// timestamp in minutes		
	DATETIME,	// date and time in standard format
	NONE,		// no time column
	TIMESTAMP_AND_DATETIME	//both columns: timestamp and date-time
};

