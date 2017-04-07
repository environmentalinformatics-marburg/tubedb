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
			public float eval(Float[] data) {
				return (float) Math.pow(x.eval(data), y.eval(data));
			}
		};
	}
}
