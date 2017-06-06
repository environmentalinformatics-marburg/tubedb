package tsdb.dsl;

import java.util.Set;

public abstract class FormulaBinary extends Formula {
	public final Formula a;
	public final Formula b;
	public FormulaBinary(Formula a, Formula b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public void collectDataVariables(Set<String> collector) {
		a.collectDataVariables(collector);
		b.collectDataVariables(collector);
	}	
}
