package tsdb.dsl.formula;

import tsdb.dsl.BooleanFormulaVisitor1;

public class BooleanFormulaLess extends BooleanFormulaAtomicBinary {
	public BooleanFormulaLess(Formula a, Formula b) {
		super(a, b);
	}

	@Override
	public BooleanFormula not() {
		return new BooleanFormulaLessEqual(b, a);
	}
	@Override
	public <T> T accept(BooleanFormulaVisitor1<T> visitor) {
		return visitor.visitLess(this);
	}
}
