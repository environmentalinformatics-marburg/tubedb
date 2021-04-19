package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationNum0 extends Computation {
	public static final ComputationNum0 DEFUALT = new ComputationNum0();
	private ComputationNum0() { }
	@Override
	public float eval(long timestamp, float[] data) {
		return 0f;
	}
	@Override
	public String toString() {
		return "zero";
	}
}
