package tsdb.dsl.computation;

public abstract class BooleanComputation {
	public abstract boolean eval(long timestamp, float[] data);
}
