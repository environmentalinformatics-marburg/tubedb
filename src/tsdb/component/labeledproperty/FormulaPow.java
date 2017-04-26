package tsdb.component.labeledproperty;

import java.util.Map;

public class FormulaPow extends Formula {
	public final Formula a;
	public final Formula b;
	public FormulaPow(Formula a, Formula b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public Computation compile(Map<String, Integer> sensorMap) {
		return new Computation() {
			Computation x = a.compile(sensorMap);
			Computation y = b.compile(sensorMap);
			@Override
			public float eval(float[] data) {
				return (float) Math.pow(x.eval(data), y.eval(data));
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
