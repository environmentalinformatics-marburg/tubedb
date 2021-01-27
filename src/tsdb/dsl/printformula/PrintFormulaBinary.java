package tsdb.dsl.printformula;

public abstract class PrintFormulaBinary extends PrintFormula {
	public final PrintFormula a;
	public final PrintFormula b;
	
	public PrintFormulaBinary(PrintFormula a, PrintFormula b) {
		super(PrintFormula.getDepth(a, b) + 1);
		this.a = a;
		this.b = b;
	}
	
	public PrintFormulaBinary(PrintFormula a, PrintFormula b, int depth) {
		super(depth);
		this.a = a;
		this.b = b;
	}
}
