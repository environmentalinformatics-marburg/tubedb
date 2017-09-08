package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationVar4 extends Computation {
	@Override
	public float eval(long timestamp, float[] data) {
		return data[4];				
	}
}
