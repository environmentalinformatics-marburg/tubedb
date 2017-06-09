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
