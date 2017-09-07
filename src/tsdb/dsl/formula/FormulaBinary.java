package tsdb.dsl.formula;

public abstract class FormulaBinary extends Formula {
	public final Formula a;
	public final Formula b;
	
	public FormulaBinary(Formula a, Formula b) {
		this.a = a;
		this.b = b;
	}
}
