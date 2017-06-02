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
	public void collectVariables(Set<String> collector) {
		a.collectVariables(collector);
		b.collectVariables(collector);
	}	
}
