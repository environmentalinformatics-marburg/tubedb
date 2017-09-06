package tsdb.dsl.formula;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.dsl.Environment;
import tsdb.dsl.FormulaVisitor1;
import tsdb.dsl.computation.Computation;
import tsdb.util.TimeUtil;

public class FormulaFunc extends Formula {
	private static final Logger log = LogManager.getLogger();

	public final String name;
	public final Formula parameter;
	public final boolean positive;

	public FormulaFunc(String name, Formula parameter, boolean positive) {
		this.name = name;
		this.parameter = parameter;
		this.positive = positive;
	}

	@Override
	public void collectDataVariables(Set<String> collector, Environment env) {
		parameter.collectDataVariables(collector, env);		
	}
	
	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitFunc(this);
	}
}
