package tsdb.dsl;

import java.util.Map;

public class BooleanFormulaAND extends BooleanFormulaBinary {
	public BooleanFormulaAND(BooleanFormula a, BooleanFormula b) {
		super(a, b);
	}
	@Override
	public BooleanComputation compile(Map<String, Integer> sensorMap) {
		return new BooleanComputation() {
			BooleanComputation x = a.compile(sensorMap);
			BooleanComputation y = b.compile(sensorMap);
			@Override
			public boolean eval(long timestamp, float[] data) {
				return x.eval(timestamp, data) && y.eval(timestamp, data);
			}
		};
	}
	@Override
	public String compileToString(Map<String, Integer> sensorMap) {
		String ja = a.compileToString(sensorMap);
		String jb = b.compileToString(sensorMap);
		return "("+ja+"&&"+jb+")";
	}
	@Override
	public BooleanFormula not() {
		return new BooleanFormulaOR(a.not(), b.not());
	}
}
