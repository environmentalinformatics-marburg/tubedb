package tsdb.util;

import java.io.Serializable;

import static tsdb.util.AssumptionCheck.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Schema of time series (TsEntries)
 * immutable (Field values should not be changed.)
 * @author woellauer
 */
public class TsSchema implements Serializable {
	private static final long serialVersionUID = 755267163534504899L;
	
	public static final int NO_CONSTANT_TIMESTEP = -999999;

	public enum Aggregation{
		NO,
		CONSTANT_STEP,
		MONTH,
		YEAR;
		
		public void throwNotVariableStepAggregation() {
			throwFalse(this==Aggregation.MONTH||this==Aggregation.YEAR,"NotVariableStepAggregation: "+this);
		}
	}

	public final String[] names;
	public final int length;

	public final Aggregation aggregation;
	public final int timeStep;
	public final boolean isContinuous;

	//special meta data
	public final boolean hasQualityFlags;
	public final boolean hasInterpolatedFlags;
	public final boolean hasQualityCounters;

	public TsSchema(String[] names, Aggregation aggregation,int timeStep,boolean isContinuous, boolean hasQualityFlags,boolean hasInterpolatedFlags,boolean hasQualityCounters) {
		this.names = Arrays.copyOf(names, names.length);
		this.length = names.length;
		this.aggregation = aggregation;
		this.timeStep = timeStep;
		this.isContinuous = isContinuous;
		this.hasQualityFlags = hasQualityFlags;
		this.hasInterpolatedFlags = hasInterpolatedFlags;
		this.hasQualityCounters = hasQualityCounters;

		String ret = checkConsistency();
		if(ret!=null) {
			throw new RuntimeException("schema not consistent: "+ret);
		}
	}

	public TsSchema(String[] names, Aggregation aggregation,int timeStep,boolean isContinuous, boolean hasQualityFlags,boolean hasInterpolatedFlags) {
		this(names,aggregation,timeStep,isContinuous,hasQualityFlags,hasInterpolatedFlags,false);
	}

	public TsSchema(String[] names, Aggregation aggregation,int timeStep,boolean isContinuous, boolean hasQualityFlags) {
		this(names,aggregation,timeStep,isContinuous,hasQualityFlags,false,false);
	}

	public TsSchema(String[] names, Aggregation aggregation,int timeStep,boolean isContinuous) {
		this(names,aggregation,timeStep,isContinuous,false,false,false);
	}

	public TsSchema(String[] names, Aggregation aggregation,int timeStep) {
		this(names,aggregation,timeStep,false,false,false,false);
	}

	public TsSchema(String[] names, Aggregation aggregation) {
		this(names,aggregation,NO_CONSTANT_TIMESTEP,false,false,false,false);
	}

	public TsSchema(String[] names) {
		this(names,Aggregation.NO,NO_CONSTANT_TIMESTEP,false,false,false,false);
	}

	private String checkConsistency() {
		if(names==null) {
			return "schema==null";
		}		
		if(names.length==0) {
			return "schema with no attributes";
		}
		Set<String> set = new HashSet<String>();
		for(int i=0;i<names.length;i++) {
			if(names[i]==null || names[i].isEmpty()) {
				return "empty attribute: "+i;
			}
			if(set.contains(names[i])) {
				return names[i]+" duplicate attributes";
			}
			set.add(names[i]);
		}
		if(aggregation==null) {
			return "aggregation==null";
		}
		if(aggregation==Aggregation.CONSTANT_STEP) {
			if(timeStep==NO_CONSTANT_TIMESTEP) {
				return "timeStep==NO_CONSTANT_TIMESTEP";
			}
		} else {
			if(timeStep!=NO_CONSTANT_TIMESTEP) {
				return "timeStep!=NO_CONSTANT_TIMESTEP";
			}
		}
		return null;
	}

	public TsSchema copy() {
		return new TsSchema(names, aggregation,timeStep,isContinuous,hasQualityFlags,hasInterpolatedFlags,hasQualityCounters);
	}

	public void throwNoAggregation() {
		throwTrue(aggregation==Aggregation.NO,"input is not pre aggregated");
	}
	
	public void throwNotAggregation(Aggregation aggregation) {
		throwFalse(this.aggregation==aggregation,"not aggregation: "+this.aggregation+"  "+aggregation);
	}
	
	public void throwNotStep(int timeStep) {
		throwFalse(this.timeStep==timeStep,"not timeStep: "+this.timeStep+"  "+timeStep);
	}

	public void throwNotContinuous() {
		throwFalse(isContinuous,"input is not pre aggregated");
	}

	public static void throwDifferentContinuous(TsSchema[] schemas) {
		boolean cont = schemas[0].isContinuous;
		for(TsSchema schema:schemas) {
			throwFalse(schema.isContinuous==cont,"different continuous");
		}
	}
	
	public static void throwDifferentQualityFlags(TsSchema[] schemas) {
		boolean qf = schemas[0].hasQualityFlags;
		for(TsSchema schema:schemas) {
			throwFalse(schema.hasQualityFlags==qf,"different quality flags");
		}
	}
	
	public static void throwDifferentTimeStep(TsSchema... schemas) {
		int step = schemas[0].timeStep;
		for(TsSchema schema:schemas) {
			throwFalse(schema.timeStep==step,"different timestep");
		}
	}
	
	public static void throwDifferentAggregation(TsSchema... schemas) {
		Aggregation agg = schemas[0].aggregation;
		for(TsSchema schema:schemas) {
			throwFalse(schema.aggregation==agg,"different aggregation");
		}
	}
	
	public void throwNoQualityFlags() {
		throwFalse(hasQualityFlags,"input has no qualityflags");
	}
	
	public void throwNoInterpolatedFlags() {
		throwFalse(hasInterpolatedFlags,"input has no interpolated flags");
	}
	
	public static boolean isSameNames(TsSchema schema1, TsSchema schema2) {
		throwNulls(schema1, schema2);
		if(schema1.length!=schema2.length) {
			return false;
		}
		if(schema1.length==0) {
			throw new RuntimeException("empty schema");
		}
		for(int i=0;i<schema1.length;i++) {
			if(!schema1.names[i].equals(schema2.names[i])) {
				return false;
			}
		}
		return true;
	}
	
	public static void throwDifferentNames(TsSchema schema1, TsSchema schema2) {
		throwFalse(isSameNames(schema1, schema2),"not same names");
	}
	
	public void throwNoConstantTimeStep() {
		throwFalse(aggregation==Aggregation.CONSTANT_STEP,"no constant timestep");
	}
	
	public void throwNoBaseAggregation() {
		throwFalse(timeStep==BaseAggregationTimeUtil.AGGREGATION_TIME_INTERVAL,"no base aggregation");
	}
	
	public void throwNotVariableStepAggregation() {
		aggregation.throwNotVariableStepAggregation();
	}
	
	public boolean contains(String[] names) {
		return Util.isContained(names, this.names);
	}
	
	@Override
	public String toString() {
		return "TsSchema [names=" + Arrays.toString(names) + ", length="
				+ length + ", aggregation=" + aggregation + ", timeStep="
				+ timeStep + ", isContinuous=" + isContinuous
				+ ", hasQualityFlags=" + hasQualityFlags
				+ ", hasInterpolatedFlags=" + hasInterpolatedFlags
				+ ", hasQualityCounters=" + hasQualityCounters + "]";
	}
}
