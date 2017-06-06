package tsdb.dsl;

import java.util.Map;
import java.util.Set;

public class FormulaConditional extends Formula {
	public final BooleanFormula p;
	public final Formula a;
	public final Formula b;
	public FormulaConditional(BooleanFormula p, Formula a, Formula b) {
		this.p = p;
		this.a = a;
		this.b = b;
	}
	@Override
	public Computation compile(Map<String, Integer> sensorMap) {
		return new Computation() {
			BooleanComputation c = p.compile(sensorMap);
			Computation x = a.compile(sensorMap);
			Computation y = b.compile(sensorMap);
			@Override
			public float eval(long timestamp, float[] data) {
				return c.eval(timestamp, data) ? x.eval(timestamp, data) : y.eval(timestamp, data);
			}
		};
	}
	@Override
	public String compileToString(Map<String, Integer> sensorMap) {
		String jp = p.compileToString(sensorMap);
		String ja = a.compileToString(sensorMap);
		String jb = b.compileToString(sensorMap);
		return "("+jp+"?"+ja+":"+jb+")";
	}
	@Override
	public void collectDataVariables(Set<String> collector) {
		p.collectVariables(collector);
		a.collectDataVariables(collector);
		b.collectDataVariables(collector);
	}
}
