package tsdb.dsl.printformula;

public class PrintFormulaIf extends PrintFormulaBinary {
	public final PrintPredFormula p;

	public PrintFormulaIf(PrintPredFormula p, PrintFormula a, PrintFormula b) {
		super(a, b);
		this.p = p;		
	}

	@Override
	public <T> T accept(PrintFormulaVisitor<T> visitor) {
		return visitor.visitIf(this);
	}
}
