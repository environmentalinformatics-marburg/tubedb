package tsdb.dsl.printformula;

public class PrintFormulaDiv extends PrintFormulaBinary {
	public PrintFormulaDiv(PrintFormula a, PrintFormula b) {
		super(a, b);
	}

	@Override
	public <T> T accept(PrintFormulaVisitor<T> visitor) {
		return visitor.visitDiv(this);
	}
}
