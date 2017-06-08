package tsdb.dsl.computation;

public class ComputationVarNeg extends Computation {
	private final int pos;
	public ComputationVarNeg(int pos) {
		this.pos = pos;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return - data[pos];				
	}
}
