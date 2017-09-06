package tsdb.dsl.formula;

import java.util.Set;

import tsdb.dsl.Environment;
import tsdb.dsl.FormulaVisitor1;
import tsdb.dsl.computation.Computation;
import tsdb.dsl.computation.ComputationOfTime;

public class FormulaNoDataVar extends Formula {

	public final String name;
	public final boolean positive;

	public FormulaNoDataVar(String name, boolean positive) {
		this.name = name;
		this.positive = positive;
	}
	
	@Override
	public void collectDataVariables(Set<String> collector, Environment env) {
		if(!ComputationOfTime.NON_DATA_VARIABLES_SET.contains(name) && !env.containsResolver(name)) {
			collector.add(name);		
		}
	}

	@Override
	public Formula negative() {
		return new FormulaNoDataVar(name, !positive);
	}
	
	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitNoDataVar(this);
	}
}
