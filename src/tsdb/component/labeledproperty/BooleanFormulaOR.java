package tsdb.component.labeledproperty;

import java.util.Map;

public class BooleanFormulaOR extends BooleanFormulaBinary {
	public BooleanFormulaOR(BooleanFormula a, BooleanFormula b) {
		super(a, b);
	}
	@Override
	public BooleanComputation compile(Map<String, Integer> sensorMap) {
		return new BooleanComputation() {
			BooleanComputation x = a.compile(sensorMap);
			BooleanComputation y = b.compile(sensorMap);
			@Override
			public boolean eval(float[] data) {
				return x.eval(data) || y.eval(data);
			}
		};
	}
	@Override
	public String compileToString(Map<String, Integer> sensorMap) {
		String ja = a.compileToString(sensorMap);
		String jb = b.compileToString(sensorMap);
		return "("+ja+"||"+jb+")";
	}
	@Override
	public BooleanFormula not() {
		return new BooleanFormulaAND(a.not(), b.not());
	}
}
