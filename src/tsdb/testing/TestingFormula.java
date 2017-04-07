package tsdb.testing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.component.labeledproperty.Computation;
import tsdb.component.labeledproperty.Formula;
import tsdb.component.labeledproperty.PropertyComputation;

public class TestingFormula {
	private static final Logger log = LogManager.getLogger("tsdb");

	public static void main(String[] args) {
		String[] sensorNames = new String[] {"a", "b", "PAR_300_U", "PAR_300", "c"};
		String formulaText = "PAR_300_U*1000/5.59";
		//String formulaText = "PAR_300_U";	
		//String formulaText = "42e7";
		//String formulaText = "2^3";
		//String formulaText = "2*3";
		//String formulaText = "2+3";
		Formula formula = PropertyComputation.parseFormula(formulaText);
		Computation computation = formula.compile(sensorNames);
		
		float v = computation.eval(new Float[]{1f, 2f, 3f, 4f, 5f});
		log.info(v);
	}

}