package tsdb.dsl.formula;

import tsdb.dsl.BooleanFormulaVisitor1;

public class BooleanFormulaOR extends BooleanFormulaBinary {
	public BooleanFormulaOR(BooleanFormula a, BooleanFormula b) {
		super(a, b);
	}

	@Override
	public BooleanFormula not() {
		return new BooleanFormulaAND(a.not(), b.not());
	}
	@Override
	public <T> T accept(BooleanFormulaVisitor1<T> visitor) {
		return visitor.visitOR(this);
	}
}
