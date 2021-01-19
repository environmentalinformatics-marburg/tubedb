package tsdb.dsl.printformula;

public class PrintPredFormulaOr extends PrintPredFormula {
	
	public final PrintPredFormula[] preds;
	
	public PrintPredFormulaOr(PrintPredFormula[] preds) {
		super(PrintPredFormula.getDepth(preds) + 1);
		this.preds = preds;
	}

	@Override
	public <T> T accept(PrintFormulaVisitor<T> visitor) {
		return visitor.visitPredOr(this);	
	}
}
