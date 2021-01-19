package tsdb.dsl.printformula;

public class PrintPredFormulaConst extends PrintPredFormula {
	public final boolean value;
	
	public PrintPredFormulaConst(boolean value) {
		super(0);
		this.value = value;
	}

	@Override
	public <T> T accept(PrintFormulaVisitor<T> visitor) {
		return visitor.visitPredConst(this);
	}
}
