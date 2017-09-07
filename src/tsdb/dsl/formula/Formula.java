package tsdb.dsl.formula;

import tsdb.dsl.FormulaVisitor1;

public abstract class Formula {
	
	public abstract <T> T accept(FormulaVisitor1<T> visitor);
	
	public Formula negative() {
		throw new RuntimeException("negative not implemented in "+this.getClass());
	}
	
	public Formula withSign(boolean positive) {
		return positive ? this : this.negative();
	}
}
