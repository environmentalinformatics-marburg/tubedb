package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationVar3Neg extends Computation {	
	@Override
	public float eval(long timestamp, float[] data) {
		return - data[3];				
	}
}
