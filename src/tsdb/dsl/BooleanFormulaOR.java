package tsdb.dsl;

import java.util.Map;

import tsdb.dsl.computation.BooleanComputation;

public class BooleanFormulaOR extends BooleanFormulaBinary {
	public BooleanFormulaOR(BooleanFormula a, BooleanFormula b) {
		super(a, b);
	}
	@Override
	public BooleanComputation compile(Environment env) {
		return new BooleanComputation() {
			BooleanComputation x = a.compile(env);
			BooleanComputation y = b.compile(env);
			@Override
			public boolean eval(long timestamp, float[] data) {
				return x.eval(timestamp, data) || y.eval(timestamp, data);
			}
		};
	}
	@Override
	public String compileToString(Environment env) {
		String ja = a.compileToString(env);
		String jb = b.compileToString(env);
		return "("+ja+"||"+jb+")";
	}
	@Override
	public BooleanFormula not() {
		return new BooleanFormulaAND(a.not(), b.not());
	}
	@Override
	public <T> T accept(BooleanFormulaVisitor1<T> visitor) {
		return visitor.visitOR(this);
	}
}
