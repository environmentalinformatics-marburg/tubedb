package tsdb.dsl.printformula;

public class PrintFormulaVar extends PrintFormula {

	public final boolean positive;
	public final String name;

	public PrintFormulaVar(boolean positive, String name) {
		super(0);
		this.positive = positive;
		this.name = name;
	}

	@Override
	public <T> T accept(PrintFormulaVisitor<T> visitor) {
		return visitor.visitVar(this);
	}
}
