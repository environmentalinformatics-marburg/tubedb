package tsdb.dsl.formula;

import tsdb.dsl.FormulaVisitor1;

public class FormulaConditional extends Formula {
	public final BooleanFormula p;
	public final Formula a;
	public final Formula b;
	public FormulaConditional(BooleanFormula p, Formula a, Formula b) {
		this.p = p;
		this.a = a;
		this.b = b;
	}

	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitConditional(this);
	}
}
