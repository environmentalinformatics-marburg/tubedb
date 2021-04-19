package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulPowNum_ extends Computation {
	public final Computation a;
	public final Computation exponent;
	public final float factor;
	public ComputationMulPowNum_(Computation a, Computation exponent, float factor) {
		this.a = a;
		this.exponent = exponent;
		this.factor = factor;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return  ((float) (Math.pow(a.eval(timestamp, data), exponent.eval(timestamp, data)))) * factor;
	}
	@Override
	public String toString() {
		return "MulPowNum(" + a  + ", " + exponent + ", " + factor + ")";
	}
}
