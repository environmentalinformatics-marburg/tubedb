package tsdb.testing;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import tsdb.component.labeledproperty.Computation;
import tsdb.component.labeledproperty.Formula;
import tsdb.component.labeledproperty.PropertyComputation;
import tsdb.util.Timer;

public class TestingFormula {
	private static final Logger log = LogManager.getLogger("tsdb");

	static final int REPEATS = 10;
	static final int LOOPS = 10_000_000;

	public interface EvaluatorA {
		public float eval (float[] data);
	}

	public static void main(String[] args) throws Exception {
		String[] sensorNames = new String[] {"a", "b", "PAR_300_U", "PAR_300", "c", "p_QNH", "Ta_200"};
		//String formulaText = "PAR_300_U*1000/5.59";
		//String formulaText = "PAR_300_U";	
		//String formulaText = "42e7";
		//String formulaText = "2^3";
		//String formulaText = "2*3";
		//String formulaText = "2+3";
		//String formulaText = "a + b + c";
		//String formulaText = "a * b * c";
		//String formulaText = "((PAR_300_U*a/b+c*p_QNH/PAR_300 * Ta_200)^1.7-27)*c";
		String formulaText = "(((a + b + c + Ta_200) * (PAR_300_U^2.7 + p_QNH^1.2 + b/a)) / PAR_300^17) ^ (b/(27+a)) + (a-1)*(b-(2*PAR_300^(c/123)))*(c-3)";
		Formula formula = PropertyComputation.parseFormula(formulaText);
		Computation computation = formula.compile(sensorNames);

		float[] params = new float[]{1f, 2f, 3f, 4f, 5f, 6f, 7f};
		float v = computation.eval(params);
		log.info(v);

		String javaText = formula.compileToString(sensorNames);
		log.info(javaText);


		ClassPool pool = ClassPool.getDefault();

		CtClass evalAClass = pool.makeClass("EvalA", pool.get(Computation.class.getName()));
		evalAClass.addMethod(CtNewMethod.make("public float eval (float[] data) { return "+javaText+"; }",evalAClass));
		@SuppressWarnings("unchecked")
		Class<? extends Computation> clazzA = evalAClass.toClass();
		Computation objA = clazzA.newInstance();
		log.info(objA.eval(params));


		CtClass evalIClass = pool.makeClass("EvalI");
		evalIClass.addMethod(CtNewMethod.make("public float eval (float[] data) { return "+javaText+"; }",evalIClass));
		evalIClass.setInterfaces(new CtClass[] { pool.get(EvaluatorA.class.getName()) });
		@SuppressWarnings("unchecked")
		Class<? extends EvaluatorA> clazzI = evalIClass.toClass();
		EvaluatorA objI = clazzI.newInstance();
		log.info(objI.eval(params));

		Map<String, Function<float[], Float>> map = new HashMap<>();
		map.put("direct", data -> (((float) Math.pow((double)(((((data[0]+data[1])+data[4])+data[6])*((((float) Math.pow((double)data[2],(double)2.7f))+((float) Math.pow((double)data[5],(double)1.2f)))+(data[1]/data[0])))/((float) Math.pow((double)data[3],(double)17.0f))),(double)(data[1]/(27.0f+data[0]))))+(((data[0]-1.0f)*(data[1]-(2.0f*((float) Math.pow((double)data[3],(double)(data[4]/123.0f))))))*(data[4]-3.0f))) );
		map.put("tree", computation::eval);
		map.put("genI", objI::eval);
		map.put("genA", objA::eval);
		map.put("ID", data -> Float.MIN_VALUE);


		for(int repeat=0; repeat<REPEATS; repeat++) {
			
			for(Entry<String, Function<float[], Float>> e:map.entrySet()) {
				bench(e.getKey(), e.getValue(), params);
			}

		}
	}

	public static void bench(String name, Function<float[], Float> func, float[] params) {
		Timer.start(name);
		float sum = 0f;
		for(int loop=0; loop<LOOPS; loop++) {
			sum += func.apply(params);
		}
		log.info(Timer.stop(name)+" "+sum);
	}

}
