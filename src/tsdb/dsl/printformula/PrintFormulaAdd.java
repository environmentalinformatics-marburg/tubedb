package tsdb.dsl.printformula;

public class PrintFormulaAdd extends PrintFormula {
	
	public final PrintFormulaAddOp[] terms;
	
	public PrintFormulaAdd(PrintFormulaAddOp[] terms) {
		super(PrintFormulaAddOp.getDepth(terms) + 1);
		this.terms = terms;
	}

	@Override
	public <T> T accept(PrintFormulaVisitor<T> visitor) {
		return visitor.visitAdd(this);	
	}
}
