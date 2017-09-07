package tsdb.dsl.formula;

public abstract class BooleanFormulaBinary extends BooleanFormula {	
	public final BooleanFormula a;
	public final BooleanFormula b;
	public BooleanFormulaBinary(BooleanFormula a, BooleanFormula b) {
		this.a = a;
		this.b = b;
	}	
}
