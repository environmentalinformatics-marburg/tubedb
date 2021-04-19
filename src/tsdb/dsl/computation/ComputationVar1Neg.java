package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationVar1Neg extends Computation {	
	@Override
	public float eval(long timestamp, float[] data) {
		return - data[1];				
	}
	@Override
	public String toString() {
		return "Var1neg";
	}
}
