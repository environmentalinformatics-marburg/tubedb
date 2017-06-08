package tsdb.dsl.computation;

public class ComputationVar2Neg extends Computation {	
	@Override
	public float eval(long timestamp, float[] data) {
		return - data[2];				
	}
}
