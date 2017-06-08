package tsdb.dsl.computation;

public class ComputationAdd extends Computation {
	public final Computation a;
	public final Computation b;
	public ComputationAdd(Computation a, Computation b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return a.eval(timestamp, data) + b.eval(timestamp, data);
	}
}
