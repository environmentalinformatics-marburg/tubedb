package tsdb.dsl;

import java.util.Map;

public class FormulaPow extends FormulaBinary {
	public FormulaPow(Formula a, Formula b) {
		super(a, b);
	}
	@Override
	public Computation compile(Map<String, Integer> sensorMap) {
		return new Computation() {
			Computation x = a.compile(sensorMap);
			Computation y = b.compile(sensorMap);
			@Override
			public float eval(long timestamp, float[] data) {
				return (float) Math.pow(x.eval(timestamp, data), y.eval(timestamp, data));
			}
		};
	}
	@Override
	public String compileToString(Map<String, Integer> sensorMap) {
		String ja = a.compileToString(sensorMap);
		String jb = b.compileToString(sensorMap);
		return "((float) Math.pow((double)"+ja+",(double)"+jb+"))";
	}
}