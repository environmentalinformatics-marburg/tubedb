package tsdb.dsl.formula;

import java.util.Set;

import tsdb.dsl.Environment;

public abstract class FormulaBinary extends Formula {
	public final Formula a;
	public final Formula b;
	public FormulaBinary(Formula a, Formula b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public void collectDataVariables(Set<String> collector, Environment env) {
		a.collectDataVariables(collector, env);
		b.collectDataVariables(collector, env);
	}	
}
