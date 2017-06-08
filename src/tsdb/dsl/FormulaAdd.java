package tsdb.dsl;

import tsdb.dsl.computation.Computation;

public class FormulaAdd extends FormulaBinary {
	public FormulaAdd(Formula a, Formula b) {
		super(a, b);
	}
	@Override
	public Computation compile(Environment env) {
		return new Computation() {
			Computation x = a.compile(env);
			Computation y = b.compile(env);
			@Override
			public float eval(long timestamp, float[] data) {
				return x.eval(timestamp, data) + y.eval(timestamp, data);
			}
		};
	}
	@Override
	public String compileToString(Environment env) {
		String ja = a.compileToString(env);
		String jb = b.compileToString(env);
		return "("+ja+"+"+jb+")";
	}
	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitAdd(this);		
	}	
}
