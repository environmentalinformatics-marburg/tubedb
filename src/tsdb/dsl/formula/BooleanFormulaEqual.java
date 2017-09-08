package tsdb.dsl.formula;

import tsdb.dsl.BooleanFormulaVisitor1;

public class BooleanFormulaEqual extends BooleanFormulaAtomicBinary {
	public BooleanFormulaEqual(Formula a, Formula b) {
		super(a, b);
	}

	@Override
	public BooleanFormula not() {
		return new BooleanFormulaNotEqual(a, b);
	}
	@Override
	public <T> T accept(BooleanFormulaVisitor1<T> visitor) {
		return visitor.visitEqual(this);
	}
}
