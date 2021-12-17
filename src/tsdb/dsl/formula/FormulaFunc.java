package tsdb.dsl.formula;


import org.tinylog.Logger;

import tsdb.dsl.FormulaVisitor1;

public class FormulaFunc extends Formula {
	

	public final String name;
	public final Formula parameter;
	public final boolean positive;

	public FormulaFunc(String name, Formula parameter, boolean positive) {
		this.name = name;
		this.parameter = parameter;
		this.positive = positive;
	}

	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitFunc(this);
	}
}
