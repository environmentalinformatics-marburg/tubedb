package tsdb.iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.BaseAggregationTimeUtil;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.InputProcessingIterator;
import tsdb.util.iterator.TsIterator;

/**
 * This iterator fills gaps in input_iterator with NaN rows.
 * start and end of resulting iterator are set to the given parameters, and if needed filled with NaN rows.
 * input iterator needs aggregated data of constant time steps (may contain time gaps in it)
 * @author woellauer
 *
 */
public class NanGapIterator extends InputProcessingIterator {	
	private static final Logger log = LogManager.getLogger();

	/**
	 * timestamp of next Element to output
	 */
	long currTimestamp;
	TsEntry nextElement;

	Long endTimestamp;

	public static TsSchema createSchema(TsSchema tsSchema) {
		String[] names = tsSchema.names;
		tsSchema.throwNoConstantTimeStep();
		Aggregation aggregation = tsSchema.aggregation;
		int timeStep = tsSchema.timeStep;
		boolean isContinuous = true;
		boolean hasQualityFlags = tsSchema.hasQualityFlags;
		boolean hasInterpolatedFlags = tsSchema.hasInterpolatedFlags;
		boolean hasQualityCounters = tsSchema.hasQualityCounters;
		return new TsSchema(names, aggregation, timeStep, isContinuous, hasQualityFlags, hasInterpolatedFlags, hasQualityCounters);
	}


	/**
	 * 
	 * @param input_iterator
	 * @param start if null first element is start
	 * @param end if null last element is end
	 */
	public NanGapIterator(TsIterator input_iterator, Long start, Long end) {
		super(input_iterator, createSchema(input_iterator.getSchema()));

		/*System.out.println("nan it start: "+TimeConverter.oleMinutesToLocalDateTime(start));
		System.out.println("nan it end: "+TimeConverter.oleMinutesToLocalDateTime(end));
		System.out.println("nan it timeStep: "+outputTimeSeriesSchema.timeStep);*/

		if(start!=null) {		
			if(!BaseAggregationTimeUtil.isBaseAggregationTimestamp(start)) {
				throw new RuntimeException("timestamp start not alligned: "+TimeUtil.oleMinutesToLocalDateTime(start));
			}
			if(start%schema.timeStep!=0) {
				throw new RuntimeException("timestamp start not alligned to timestep: "+TimeUtil.oleMinutesToLocalDateTime(start));
			}
		}

		if(end!=null) {
			if(!BaseAggregationTimeUtil.isBaseAggregationTimestamp(end)) {
				throw new RuntimeException("timestamp end not alligned: "+TimeUtil.oleMinutesToLocalDateTime(end));
			}		
			if(end%schema.timeStep!=0) {
				throw new RuntimeException("timestamp end not alligned to timestep: "+TimeUtil.oleMinutesToLocalDateTime(end));
			}
		}


		this.endTimestamp = end;
		if(input_iterator.hasNext()) { //****************input iterator is not empty***************
			this.nextElement = input_iterator.next();
			if(start!=null) { //********************** with start timestamp *********************
				if(start<=nextElement.timestamp) { //************* next element timestamp higher than start **************
					currTimestamp = start;
				} else { //********* next element timestamp lower than start *************************
					while(nextElement.timestamp<start) { // loop up to first element in start - end range
						if(input_iterator.hasNext()) {
							nextElement = input_iterator.next();
						} else {
							nextElement = null;
							break;
						}
					}					
					currTimestamp = start;
				}
			} else { //*************** no start timestamp ***************************
				this.currTimestamp = this.nextElement.timestamp;
			}
		} else { //****************input iterator is empty***************
			if(start!=null) {
				input_iterator = null;			
				currTimestamp = start;
				nextElement = null;
			} else {
				input_iterator = null;			
				currTimestamp = -100;
				endTimestamp = (long)-200;
				nextElement = null;	
			}
		}

	}

	@Override
	protected TsEntry getNext() {
		//System.out.println(this.getClass()+"  getNext()");
		if(nextElement==null) {// ******************  no elements left in input_iterator ********************************************
			if(endTimestamp==null) {
				return null;
			} else {
				if(currTimestamp<=endTimestamp) {
					TsEntry nanElement = TsEntry.createNaN(currTimestamp, input_iterator.getNames().length);
					currTimestamp += schema.timeStep;
					//System.out.println(this.getClass()+" "+currTimestamp+" "+nanElement);
					return nanElement;
				} else {
					return null;
				}
			}
		} else if(endTimestamp!=null&&nextElement.timestamp>endTimestamp) {
			nextElement = null;
			return null;
		} else if(currTimestamp<nextElement.timestamp) { // ************** next element higher than current timestamp ****************
			TsEntry nanElement = TsEntry.createNaN(currTimestamp, input_iterator.getNames().length);
			currTimestamp += schema.timeStep;
			return nanElement;
		} else if(currTimestamp==nextElement.timestamp) { // ************* current element timestamp equal to current timestamp ******
			currTimestamp += schema.timeStep;
			TsEntry currElement = nextElement;			
			if(input_iterator.hasNext()) {
				TsEntry temp = input_iterator.next();
				if(temp.timestamp<=nextElement.timestamp) {
					log.error("timestamp error in input iterator");
				}
				nextElement = temp;
			} else {
				nextElement=null;
			}
			//System.out.println(this.getClass()+" "+currTimestamp+" "+currElement);
			return currElement;
		} else {// ******************** current timestamp higher than next timestamp
			throw new RuntimeException("timestamp error in NanGapIterator: currTimestamp    nextElement.timestamp   "+currTimestamp+"\t\t"+nextElement.timestamp+"\t\t"+TimeUtil.oleMinutesToLocalDateTime(currTimestamp)+"\t-\t"+TimeUtil.oleMinutesToLocalDateTime(nextElement.timestamp));
			//log.error("timestamp error in NanGapIterator: currTimestamp    nextElement.timestamp   "+currTimestamp+"\t\t"+nextElement.timestamp);
			//return null;
		}
	}
}
