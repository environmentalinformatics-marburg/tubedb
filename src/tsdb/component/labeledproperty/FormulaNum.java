package tsdb.component.labeledproperty;

import java.util.Map;

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
			public float eval(Float[] data) {
				return v;				
			}
		};
		
	}
}
