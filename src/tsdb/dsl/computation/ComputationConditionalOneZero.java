package tsdb.dsl.computation;

public class ComputationConditionalOneZero extends Computation {
	private final BooleanComputation p;
	public ComputationConditionalOneZero(BooleanComputation p) {
		this.p = p;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return p.eval(timestamp, data) ? 1f : 0f;
	}
}
