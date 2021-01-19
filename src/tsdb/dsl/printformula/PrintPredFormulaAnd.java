package tsdb.dsl.printformula;

public class PrintPredFormulaAnd extends PrintPredFormula {
	
	public final PrintPredFormula[] preds;
	
	public PrintPredFormulaAnd(PrintPredFormula[] preds) {
		super(PrintPredFormula.getDepth(preds) + 1);
		this.preds = preds;
	}

	@Override
	public <T> T accept(PrintFormulaVisitor<T> visitor) {
		return visitor.visitPredAnd(this);	
	}
}
