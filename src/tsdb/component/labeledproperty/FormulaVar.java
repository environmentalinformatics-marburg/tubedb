package tsdb.component.labeledproperty;

import java.util.Map;

public class FormulaVar extends Formula {
	public final String name;

	public FormulaVar(String name) {
		this.name = name;
	}
	
	@Override
	public Computation compile(Map<String,Integer> sensorMap) {
		Integer p = sensorMap.get(name);
		if(p == null) {
			throw new RuntimeException("senso not found: "+name+"  in  "+sensorMap);
		}
		return new Computation(){
			int pos = sensorMap.get(name);
			@Override
			public float eval(float[] data) {
				return data[pos];				
			}
		};
		
	}
}
