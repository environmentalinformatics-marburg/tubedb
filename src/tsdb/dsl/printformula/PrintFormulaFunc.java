package tsdb.dsl.printformula;


import org.tinylog.Logger;

import tsdb.dsl.FormulaVisitor1;

public class PrintFormulaFunc extends PrintFormula {
	

	public final String name;
	public final PrintFormula param;
	public final boolean positive;

	public PrintFormulaFunc(String name, PrintFormula param, boolean positive) {
		super(param.depth + 1);
		this.name = name;
		this.param = param;
		this.positive = positive;
	}

	@Override
	public <T> T accept(PrintFormulaVisitor<T> visitor) {
		return visitor.visitFunc(this);
	}
}
