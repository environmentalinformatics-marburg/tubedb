package tsdb.dsl.computation;

public class ComputationVar4 extends Computation {
	@Override
	public float eval(long timestamp, float[] data) {
		return data[4];				
	}
}
