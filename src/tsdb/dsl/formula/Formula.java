package tsdb.dsl.formula;

import java.util.LinkedHashSet;
import java.util.Set;

import tsdb.dsl.Environment;
import tsdb.dsl.FormulaVisitor1;

public abstract class Formula {
	
	public abstract <T> T accept(FormulaVisitor1<T> visitor);
	
	@Deprecated
	public abstract void collectDataVariables(Set<String> collector, Environment env);
	
	@Deprecated
	public final Set<String> getDataVariables(Environment env) {
		LinkedHashSet<String> collector = new LinkedHashSet<String>();
		collectDataVariables(collector, env);
		return collector;
	}
	
	@Deprecated
	public final int[] getDataVariableIndices(Environment env) {
		return getDataVariables(env).stream().mapToInt(name -> env.getSensorIndex(name)).toArray();
	}
	
	public Formula negative() {
		throw new RuntimeException("negative not implemented in "+this.getClass());
	}
	
	public Formula withSign(boolean positive) {
		return positive ? this : this.negative();
	}
}
