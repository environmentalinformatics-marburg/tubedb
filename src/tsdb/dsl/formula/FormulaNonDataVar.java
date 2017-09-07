package tsdb.dsl.formula;

import tsdb.dsl.FormulaVisitor1;

public class FormulaNonDataVar extends Formula {

	public final String name;
	public final boolean positive;

	public FormulaNonDataVar(String name, boolean positive) {
		this.name = name;
		this.positive = positive;
	}
	
	@Override
	public Formula negative() {
		return new FormulaNonDataVar(name, !positive);
	}
	
	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitNonDataVar(this);
	}
}
