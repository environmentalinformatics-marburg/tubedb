package tsdb.dsl.formula;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.dsl.Environment;
import tsdb.dsl.FormulaVisitor1;
import tsdb.dsl.computation.Computation;
import tsdb.dsl.computation.ComputationOfTime;

public class FormulaVar extends Formula {
	private static final Logger log = LogManager.getLogger();	

	public final String name;
	public final boolean positive;

	public FormulaVar(String name, boolean positive) {
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
		return new FormulaVar(name, !positive);
	}
	
	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitVar(this);
	}
}
