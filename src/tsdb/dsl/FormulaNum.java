package tsdb.dsl;

import java.util.Map;
import java.util.Set;

public class FormulaNum extends Formula {
	public final float value;
	
	public FormulaNum(String s) {
		this(Float.parseFloat(s));
	}

	public FormulaNum(float value) {
		this.value = value;
	}
	
	@Override
	public Computation compile(Map<String,Integer> sensorMap) {		
		return new Computation(){
			float v = value;
			@Override
			public float eval(long timestamp, float[] data) {
				return v;				
			}
		};
		
	}

	@Override
	public String compileToString(Map<String, Integer> sensorMap) {
		return Float.toString(value)+"f";
	}

	@Override
	public void collectDataVariables(Set<String> collector) {
		// nothing		
	}
}