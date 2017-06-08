package tsdb.dsl;

import java.util.Map;
import java.util.Set;

import tsdb.dsl.computation.BooleanComputation;
import tsdb.dsl.computation.Computation;

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
	public Computation compile(Environment env) {
		return new Computation() {
			BooleanComputation c = p.compile(env);
			Computation x = a.compile(env);
			Computation y = b.compile(env);
			@Override
			public float eval(long timestamp, float[] data) {
				return c.eval(timestamp, data) ? x.eval(timestamp, data) : y.eval(timestamp, data);
			}
		};
	}
	@Override
	public String compileToString(Environment env) {
		String jp = p.compileToString(env);
		String ja = a.compileToString(env);
		String jb = b.compileToString(env);
		return "("+jp+"?"+ja+":"+jb+")";
	}
	@Override
	public void collectDataVariables(Set<String> collector, Environment env) {
		p.collectVariables(collector, env);
		a.collectDataVariables(collector, env);
		b.collectDataVariables(collector, env);
	}
	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitConditional(this);
	}
}
