package tsdb.dsl.printformula;

public class PrintPredFormulaRel extends PrintPredFormula {
	public final String name;
	public final PrintFormula a;
	public final PrintFormula b;
	
	public PrintPredFormulaRel(String name, PrintFormula a, PrintFormula b) {
		super(PrintFormula.getDepth(a, b) + 1);
		this.name = name;
		this.a = a;
		this.b = b;
	}

	@Override
	public <T> T accept(PrintFormulaVisitor<T> visitor) {
		return visitor.visitPredRel(this);
	}
}
