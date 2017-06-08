package tsdb.dsl.computation;

public class ComputationVar extends Computation {
	private final int pos;
	public ComputationVar(int pos) {
		this.pos = pos;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return data[pos];				
	}
}
