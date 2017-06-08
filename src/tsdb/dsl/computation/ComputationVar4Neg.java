package tsdb.dsl.computation;

public class ComputationVar4Neg extends Computation {	
	@Override
	public float eval(long timestamp, float[] data) {
		return - data[4];				
	}
}
