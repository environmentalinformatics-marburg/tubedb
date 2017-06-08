package tsdb.dsl;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	public Computation compile(Environment env) {
		log.info(parameter);
		Computation p = parameter.compile(env);
		switch(name) {
		case "exp":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						float a = p.eval(timestamp, data); 
						return (float) Math.exp(a);				
					}
				};
			} else {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						float a = p.eval(timestamp, data); 
						return (float) ( - Math.exp(a));				
					}
				};				
			}
		case "ln":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						float a = p.eval(timestamp, data); 
						return (float) Math.log(a);				
					}
				};
			} else {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						float a = p.eval(timestamp, data); 
						return (float) ( - Math.log(a));				
					}
				};				
			}
		case "cumsum_by_year":
			if(positive) {
				return new Computation(){
					long timestampMin = 0;
					long timestampMax = 0;
					float acc = 0f;
					@Override
					public float eval(long timestamp, float[] data) {
						if(timestamp<timestampMin || timestampMax<timestamp) {
							int currentYear = TimeUtil.oleMinutesToLocalDateTime(timestamp).getYear();
							timestampMin = TimeUtil.ofDateStartMinute(currentYear);
							timestampMax = TimeUtil.ofDateEndMinute(currentYear);
							acc = 0f;
						}
						float a = p.eval(timestamp, data);
						acc += a;
						return acc;				
					}
				};
			} else {
				return new Computation(){
					long timestampMin = 0;
					long timestampMax = 0;
					float acc = 0f;
					@Override
					public float eval(long timestamp, float[] data) {
						if(timestamp<timestampMin || timestampMax<timestamp) {
							int currentYear = TimeUtil.oleMinutesToLocalDateTime(timestamp).getYear();
							timestampMin = TimeUtil.ofDateStartMinute(currentYear);
							timestampMax = TimeUtil.ofDateEndMinute(currentYear);
							acc = 0f;
						}
						float a = p.eval(timestamp, data);
						acc += a;
						return - acc;				
					}
				};		
			}			
		default:
			throw new RuntimeException("function not found: "+name);
		}		
	}

	@Override
	public String compileToString(Environment env) {
		String p = parameter.compileToString(env);
		switch(name) {
		case "exp":
			if(positive) {
				return "(float) Math.exp((double)"+ p +")";
			} else {
				return "(float) ( - Math.exp((double)"+ p +") )";
			}
		case "ln":
			if(positive) {
				return "(float) Math.log((double)"+ p +")";
			} else {
				return "(float) ( - Math.log((double)"+ p +") )";
			}
		default:
			throw new RuntimeException("function not found: "+name);
		}
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
