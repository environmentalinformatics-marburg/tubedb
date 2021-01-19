package tsdb.dsl.printformula;

public class PrintFormulaPow extends PrintFormulaBinary {
	public PrintFormulaPow(PrintFormula a, PrintFormula b) {
		super(a, b);
	}

	@Override
	public <T> T accept(PrintFormulaVisitor<T> visitor) {
		return visitor.visitPow(this);
	}
}
