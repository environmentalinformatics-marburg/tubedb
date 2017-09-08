package tsdb.dsl.formula;

import tsdb.dsl.BooleanFormulaVisitor1;

public class BooleanFormulaNotEqual extends BooleanFormulaAtomicBinary {
	public BooleanFormulaNotEqual(Formula a, Formula b) {
		super(a, b);
	}

	@Override
	public BooleanFormula not() {
		return new BooleanFormulaEqual(a, b);
	}
	@Override
	public <T> T accept(BooleanFormulaVisitor1<T> visitor) {
		return visitor.visitNotEqual(this);
	}
}
