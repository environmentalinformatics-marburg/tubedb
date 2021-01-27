package tsdb.dsl.printformula;

public class PrintFormulaPow extends PrintFormulaBinary {
	public PrintFormulaPow(PrintFormula a, PrintFormula b) {
		super(a, b, PrintFormula.getDepth(a, b) > 0 ? PrintFormula.getDepth(a, b) + 1 : 0);
	}

	@Override
	public <T> T accept(PrintFormulaVisitor<T> visitor) {
		return visitor.visitPow(this);
	}
}
