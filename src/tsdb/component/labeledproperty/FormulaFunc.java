package tsdb.component.labeledproperty;

import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	public Computation compile(Map<String,Integer> sensorMap) {
		log.info(parameter);
		Computation p = parameter.compile(sensorMap);
		switch(name) {
		case "exp":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(float[] data) {
						float a = p.eval(data); 
						return (float) Math.exp(a);				
					}
				};
			} else {
				return new Computation(){
					@Override
					public float eval(float[] data) {
						float a = p.eval(data); 
						return (float) ( - Math.exp(a));				
					}
				};				
			}
		case "ln":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(float[] data) {
						float a = p.eval(data); 
						return (float) Math.log(a);				
					}
				};
			} else {
				return new Computation(){
					@Override
					public float eval(float[] data) {
						float a = p.eval(data); 
						return (float) ( - Math.log(a));				
					}
				};				
			}
		default:
			throw new RuntimeException("function not found: "+name);
		}		
	}

	@Override
	public String compileToString(Map<String, Integer> sensorMap) {
		String p = parameter.compileToString(sensorMap);
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
	public void collectVariables(Set<String> collector) {
		parameter.collectVariables(collector);		
	}
}
