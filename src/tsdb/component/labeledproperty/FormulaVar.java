package tsdb.component.labeledproperty;

import java.util.Map;

public class FormulaVar extends Formula {
	public final String name;

	public FormulaVar(String name) {
		this.name = name;
	}
	
	@Override
	public Computation compile(Map<String,Integer> sensorMap) {		
		return new Computation(){
			int pos = sensorMap.get(name);
			@Override
			public float eval(Float[] data) {
				return data[pos];				
			}
		};
		
	}
}
