package tsdb.dsl.formula;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.dsl.FormulaVisitor1;

public class FormulaVar extends Formula {
	private static final Logger log = LogManager.getLogger();	

	public final String name;
	public final boolean positive;

	public FormulaVar(String name, boolean positive) {
		this.name = name;
		this.positive = positive;
	}	

	@Override
	public Formula negative() {
		return new FormulaVar(name, !positive);
	}
	
	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitVar(this);
	}
}
