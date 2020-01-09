package tsdb.dsl.computation;

public class BooleanComputationFalse extends BooleanComputation {	
	public static final BooleanComputationFalse DEFAULT = new BooleanComputationFalse();

	@Override
	public boolean eval(long timestamp, float[] data) {
		return false;
	}
}
