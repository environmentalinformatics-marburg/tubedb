package tsdb.dsl.formula;

import java.util.Set;

import tsdb.dsl.Environment;

public abstract class BooleanFormulaBinary extends BooleanFormula {	
	public final BooleanFormula a;
	public final BooleanFormula b;
	public BooleanFormulaBinary(BooleanFormula a, BooleanFormula b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public void collectVariables(Set<String> collector, Environment env) {
		a.collectVariables(collector, env);
		b.collectVariables(collector, env);
	}	
}
