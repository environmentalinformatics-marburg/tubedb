package tsdb.dsl.printformula;

public class PrintFormulaConst extends PrintFormula {

	public final float value;

	public PrintFormulaConst(float value) {
		super(0);
		this.value = value;
	}

	@Override
	public <T> T accept(PrintFormulaVisitor<T> visitor) {
		return visitor.visitConst(this);
	}
}
