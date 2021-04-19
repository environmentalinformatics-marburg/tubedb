package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationSubNumVar extends Computation {
	public final float a;
	public final int b;
	public ComputationSubNumVar(float a, int b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return a - data[b];
	}
	
	@Override
	public String toString() {
		return "SubNumVar(" + a + ", [" + b + "])";
	}
}
