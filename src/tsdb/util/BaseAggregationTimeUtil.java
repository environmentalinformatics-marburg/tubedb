package tsdb.util;

/**
 * utility to convert raw timestamps and aggregated timestamps
 * @author woellauer
 *
 */
public class BaseAggregationTimeUtil {
	
	public final static int AGGREGATION_TIME_INTERVAL = 60; // time interval of aggregation in minutes: 1 hour
	//public final static int AGGREGATION_TIME_INTERVAL = 15; // time interval of aggregation in minutes:  15 Minutes !!!
	
	public static long calcBaseAggregationTimestamp(long timestamp) {
		/*
		//*** timeseriesdatabase base aggregation timestamps
		if(timestamp%AGGREGATION_TIME_INTERVAL==0) {
			return timestamp-AGGREGATION_TIME_INTERVAL;
		} else {
			return timestamp-timestamp%AGGREGATION_TIME_INTERVAL;
		}
		//***
		*/
		
		//*** julendat base aggregation timestamps
		return timestamp-timestamp%AGGREGATION_TIME_INTERVAL;
		//***
	}
	
	public static boolean isBaseAggregationTimestamp(long timestamp) {
		return timestamp%AGGREGATION_TIME_INTERVAL==0;
		
	}
	
	public static long alignQueryTimestampToBaseAggregationTime(long queryTimestamp) {
		if(isBaseAggregationTimestamp(queryTimestamp)) {
			return queryTimestamp;
		} else {
			return queryTimestamp-queryTimestamp%AGGREGATION_TIME_INTERVAL;
		}
	}
	
	public static long calcMinRawTimestampOfBaseAggregationTimestamp(long baseAggregationTimestamp) {
		//*** julendat base aggregation timestamps
		return alignQueryTimestampToBaseAggregationTime(baseAggregationTimestamp);
		//***
	}
	
	public static long calcMaxRawTimestampOfBaseAggregationTimestamp(long baseAggregationTimestamp) {
		//*** julendat base aggregation timestamps
		return alignQueryTimestampToBaseAggregationTime(baseAggregationTimestamp)+(AGGREGATION_TIME_INTERVAL-1);
		//***
	}
}
