package tsdb.component.labeledproperty;

import java.util.Map;

public class FormulaMul extends Formula {
	public final Formula a;
	public final Formula b;
	public FormulaMul(Formula a, Formula b) {
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
				return x.eval(data) * y.eval(data);
			}
		};
	}
	@Override
	public String compileToString(Map<String, Integer> sensorMap) {
		String ja = a.compileToString(sensorMap);
		String jb = b.compileToString(sensorMap);
		return "("+ja+"*"+jb+")";
	}
}
