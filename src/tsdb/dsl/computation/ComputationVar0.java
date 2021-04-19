package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationVar0 extends Computation {	
	@Override
	public float eval(long timestamp, float[] data) {
		return data[0];				
	}
	@Override
	public String toString() {
		return "Var0";
	}
}
