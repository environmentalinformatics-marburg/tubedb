package tsdb.dsl;

import java.util.Map;

import tsdb.dsl.computation.BooleanComputation;
import tsdb.dsl.computation.Computation;

public class BooleanFormulaLessEqual extends BooleanFormulaAtomicBinary {
	public BooleanFormulaLessEqual(Formula a, Formula b) {
		super(a, b);
	}
	@Override
	public BooleanComputation compile(Environment env) {
		return new BooleanComputation() {
			Computation x = a.compile(env);
			Computation y = b.compile(env);
			@Override
			public boolean eval(long timestamp, float[] data) {
				return x.eval(timestamp, data) <= y.eval(timestamp, data);
			}
		};
	}
	@Override
	public String compileToString(Environment env) {
		String ja = a.compileToString(env);
		String jb = b.compileToString(env);
		return "("+ja+"<="+jb+")";
	}
	@Override
	public BooleanFormula not() {
		return new BooleanFormulaLess(b, a);
	}
	@Override
	public <T> T accept(BooleanFormulaVisitor1<T> visitor) {
		return visitor.visitLessEqual(this);
	}
}
