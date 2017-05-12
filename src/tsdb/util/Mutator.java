package tsdb.util;

public abstract class Mutator {
	
	public abstract void apply(long timestamp, float[] data);

}
