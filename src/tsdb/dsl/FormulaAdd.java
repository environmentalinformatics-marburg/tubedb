package tsdb.dsl;

import java.util.Map;
import java.util.Set;

public class FormulaAdd extends FormulaBinary {
	public FormulaAdd(Formula a, Formula b) {
		super(a, b);
	}
	@Override
	public Computation compile(Map<String, Integer> sensorMap) {
		return new Computation() {
			Computation x = a.compile(sensorMap);
			Computation y = b.compile(sensorMap);
			@Override
			public float eval(long timestamp, float[] data) {
				return x.eval(timestamp, data) + y.eval(timestamp, data);
			}
		};
	}
	@Override
	public String compileToString(Map<String, Integer> sensorMap) {
		String ja = a.compileToString(sensorMap);
		String jb = b.compileToString(sensorMap);
		return "("+ja+"+"+jb+")";
	}	
}
