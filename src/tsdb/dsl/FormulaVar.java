package tsdb.dsl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.dsl.computation.Computation;

public class FormulaVar extends Formula {
	private static final Logger log = LogManager.getLogger();

	private static final HashSet<String> NON_DATA_VARIABLES_SET = new HashSet<String>(Arrays.asList(ComputationOfTime.NON_DATA_VARIABLES));

	public final String name;
	public final boolean positive;

	public FormulaVar(String name, boolean positive) {
		this.name = name;
		this.positive = positive;
	}
	
	@Override
	public Computation compile(Environment env) {
		Computation computationOfTime = ComputationOfTime.compileVar(name, positive);
		if(computationOfTime != null) {
			return computationOfTime;
		}
		
		if(env.containsResolver(name)) {
			Formula f = env.resolve(name);
			if(!positive) {
				f = f.negative();
			}
			return f.compile(env);			
		}		
		
		if(!env.containsSensor(name)) {
			throw new RuntimeException("sensor not found: "+name+"  in  "+env.sensorMap);
		}
		if(positive) {
			return new Computation(){
				int pos = env.getSensorIndex(name);
				@Override
				public float eval(long timestamp, float[] data) {
					return data[pos];				
				}
			};
		} else {
			return new Computation(){
				int pos = env.getSensorIndex(name);
				@Override
				public float eval(long timestamp, float[] data) {
					return - data[pos];				
				}
			};
		}
	}

	@Override
	public String compileToString(Environment env) {
		if(!env.containsSensor(name)) {
			throw new RuntimeException("sensor not found: "+name+"  in  "+env.sensorMap);
		}
		int pos = env.getSensorIndex(name);
		if(positive) {
			return "data["+pos+"]";
		} else {
			return " - data["+pos+"]";
		}
	}

	@Override
	public void collectDataVariables(Set<String> collector, Environment env) {
		if(!NON_DATA_VARIABLES_SET.contains(name) && !env.containsResolver(name)) {
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
