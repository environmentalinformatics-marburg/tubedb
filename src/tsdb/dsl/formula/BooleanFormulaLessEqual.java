package tsdb.dsl.formula;

import tsdb.dsl.BooleanFormulaVisitor1;

public class BooleanFormulaLessEqual extends BooleanFormulaAtomicBinary {
	public BooleanFormulaLessEqual(Formula a, Formula b) {
		super(a, b);
	}

	@Override
	public BooleanFormula not() {
		return new BooleanFormulaLess(b, a);
	}
	@Override
	public <T> T accept(BooleanFormulaVisitor1<T> visitor) {
		return visitor.visitLessEqual(this);
	}
}
