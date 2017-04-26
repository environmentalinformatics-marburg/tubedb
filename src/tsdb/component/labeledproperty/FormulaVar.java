package tsdb.component.labeledproperty;

import java.util.Map;

public class FormulaVar extends Formula {
	//private static final Logger log = LogManager.getLogger();
	
	public final String name;

	public FormulaVar(String name) {
		this.name = name;
	}
	
	@Override
	public Computation compile(Map<String,Integer> sensorMap) {
		Integer p = sensorMap.get(name);
		if(p == null) {
			throw new RuntimeException("sensor not found: "+name+"  in  "+sensorMap);
		}
		return new Computation(){
			int pos = sensorMap.get(name);
			@Override
			public float eval(float[] data) {
				return data[pos];				
			}
		};
		
	}

	@Override
	public String compileToString(Map<String, Integer> sensorMap) {
		Integer p = sensorMap.get(name);
		if(p == null) {
			throw new RuntimeException("sensor not found: "+name+"  in  "+sensorMap);
		}
		int pos = p.intValue();
		//log.info("pos "+pos+"   "+name);
		return "data["+pos+"]";
	}
}
