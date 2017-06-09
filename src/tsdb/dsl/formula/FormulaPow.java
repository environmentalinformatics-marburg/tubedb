package tsdb.dsl.formula;

import tsdb.dsl.Environment;
import tsdb.dsl.FormulaVisitor1;
import tsdb.dsl.computation.Computation;

public class FormulaPow extends FormulaBinary {
	public FormulaPow(Formula a, Formula b) {
		super(a, b);
	}
	@Override
	public Computation compile(Environment env) {
		return new Computation() {
			Computation x = a.compile(env);
			Computation y = b.compile(env);
			@Override
			public float eval(long timestamp, float[] data) {
				return (float) Math.pow(x.eval(timestamp, data), y.eval(timestamp, data));
			}
		};
	}
	@Override
	public String compileToString(Environment env) {
		String ja = a.compileToString(env);
		String jb = b.compileToString(env);
		return "((float) Math.pow((double)"+ja+",(double)"+jb+"))";
	}
	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitPow(this);
	}
}
