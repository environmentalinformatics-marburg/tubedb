package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationVar2Neg extends Computation {	
	@Override
	public float eval(long timestamp, float[] data) {
		return - data[2];				
	}
	@Override
	public String toString() {
		return "Var2neg";
	}
}
