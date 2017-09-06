package tsdb.dsl.formula;

import java.util.Map;
import java.util.Set;

import tsdb.dsl.Environment;
import tsdb.dsl.FormulaVisitor1;
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
