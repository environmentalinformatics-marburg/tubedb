package tsdb.dsl.formula;

import tsdb.dsl.BooleanFormulaVisitor1;

public class BooleanFormulaAND extends BooleanFormulaBinary {
	public BooleanFormulaAND(BooleanFormula a, BooleanFormula b) {
		super(a, b);
	}

	@Override
	public BooleanFormula not() {
		return new BooleanFormulaOR(a.not(), b.not());
	}
	@Override
	public <T> T accept(BooleanFormulaVisitor1<T> visitor) {
		return visitor.visitAND(this);
	}
}
