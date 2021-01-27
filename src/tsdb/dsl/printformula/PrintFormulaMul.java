package tsdb.dsl.printformula;

public class PrintFormulaMul extends PrintFormula {
	
	public final PrintFormula[] factors;
	
	public PrintFormulaMul(PrintFormula[] factors) {
		super(PrintFormula.getDepth(factors) + 1);
		this.factors = factors;
	}

	@Override
	public <T> T accept(PrintFormulaVisitor<T> visitor) {
		return visitor.visitMul(this);	
	}
}
