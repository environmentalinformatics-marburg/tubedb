package tsdb.dsl.computation;

public class BooleanComputationTrue extends BooleanComputation {	
	public static final BooleanComputationTrue DEFAULT = new BooleanComputationTrue();

	@Override
	public boolean eval(long timestamp, float[] data) {
		return true;
	}
}
