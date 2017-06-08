package tsdb.dsl.computation;

public abstract class Computation {
	public abstract float eval(long timestamp, float[] data);
}
