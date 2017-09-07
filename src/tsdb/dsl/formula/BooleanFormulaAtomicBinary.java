package tsdb.dsl.formula;

public abstract class BooleanFormulaAtomicBinary extends BooleanFormula {	
	public final Formula a;
	public final Formula b;
	public BooleanFormulaAtomicBinary(Formula a, Formula b) {
		this.a = a;
		this.b = b;
	}	
}
