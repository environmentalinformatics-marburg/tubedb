package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationVar3 extends Computation {
	@Override
	public float eval(long timestamp, float[] data) {
		return data[3];				
	}
	@Override
	public String toString() {
		return "Var3";
	}
}
