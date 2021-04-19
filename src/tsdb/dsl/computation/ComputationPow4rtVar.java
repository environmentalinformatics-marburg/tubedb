package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationPow4rtVar extends Computation {
	public final int a;
	public ComputationPow4rtVar(int a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = data[a];		
		return (float) Math.pow(x, (1d/4d));
	}
}
