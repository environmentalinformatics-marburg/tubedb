package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationCubeVar extends Computation {
	public final int a;
	public ComputationCubeVar(int a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = data[a];
		return x*x*x;
	}
	@Override
	public String toString() {
		return "CubeVar[" + a + "]";
	}
}
