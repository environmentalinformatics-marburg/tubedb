package tsdb.util;

public abstract class Computation {
	
	public abstract float eval(long timestamp, float[] data);
	
}
