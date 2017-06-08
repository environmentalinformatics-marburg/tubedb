package tsdb.testing;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import tsdb.component.labeledproperty.PropertyComputation;
import tsdb.dsl.Environment;
import tsdb.dsl.Formula;
import tsdb.dsl.FormulaCompileVisitor;
import tsdb.dsl.FormulaResolveVisitor;
import tsdb.dsl.computation.BooleanComputation;
import tsdb.dsl.computation.Computation;
import tsdb.util.Timer;

public class TestingFormula {
	private static final Logger log = LogManager.getLogger();

	static final int REPEATS = 10;
	static final int LOOPS = 100_000_000;

	public interface EvaluatorA {
		public float eval (float[] data);
	}

	public static String computationToString(Computation c) {
		Class<? extends Computation> clazz = c.getClass();
		String name = clazz.getSimpleName();
		if(name.startsWith("Computation")) {
			name = name.substring("Computation".length());
		}
		String s = " ";
		s += name+"(";
		Field[] fields = clazz.getDeclaredFields();
		for(Field field:fields) {
			try {
				field.setAccessible(true);
				Object f = field.get(c);
				if (f instanceof Computation) {
					Computation cs = (Computation) f;
					s += computationToString(cs);					
				} else if (f instanceof BooleanComputation) {
					BooleanComputation bcs = (BooleanComputation) f;
					s += booleanComputationToString(bcs);					
				} else {
					s += f;
				}
			} catch (Exception e) {
				s += "[ERROR]";
			}
		}
		s += ")";
		return s;
	}
	
	public static String booleanComputationToString(BooleanComputation c) {
		Class<? extends BooleanComputation> clazz = c.getClass();
		String name = clazz.getSimpleName();
		if(name.startsWith("BooleanComputation")) {
			name = name.substring("BooleanComputation".length());
		}
		String s = " ";
		s += name+"(";
		Field[] fields = clazz.getDeclaredFields();
		for(Field field:fields) {
			try {
				field.setAccessible(true);
				Object f = field.get(c);
				if (f instanceof Computation) {
					Computation cs = (Computation) f;
					s += computationToString(cs);					
				} else if (f instanceof BooleanComputation) {
					BooleanComputation bcs = (BooleanComputation) f;
					s += booleanComputationToString(bcs);					
				} else {
					s += f;
				}
			} catch (Exception e) {
				s += "[ERROR]";
			}
		}
		s += ")";
		return s;
	}

	public static void main(String[] args) throws Exception {

		log.info("-1/-1  "+((-1f)/(-1f)));
		log.info("-1/0  "+((-1f)/0f));
		log.info("-1/1  "+((-1f)/1f));
		log.info("");
		log.info("0/-1  "+(0f/(-1f)));
		log.info("0/0  "+(0f/0f));
		log.info("0/1  "+(0f/1f));
		log.info("");
		log.info("1/-1  "+(1f/(-1f)));
		log.info("1/0  "+(1f/0f));
		log.info("1/1  "+(1f/1f));
		log.info("");

		String[] sensorNames = new String[] {"a", "b", "PAR_300_U", "PAR_300", "Ta_200", "c", "p_QNH"};
		//String formulaText = "PAR_300_U*1000/5.59";
		//String formulaText = "PAR_300_U";	
		//String formulaText = "42e7";
		//String formulaText = "2^3";
		//String formulaText = "2*3";
		//String formulaText = "2+3";
		//String formulaText = "a + b + c";
		//String formulaText = "a * b * c";
		//String formulaText = "((PAR_300_U*a/b+c*p_QNH/PAR_300 * Ta_200)^1.7-27)*c";
		//String formulaText = "(((a + b + c + Ta_200) * (PAR_300_U^2.7 + p_QNH^1.2 + b/a)) / PAR_300^17) ^ (b/(27+a)) + (a-1)*(b-(2*PAR_300^(c/123)))*(c-3)";
		String formulaText = "(  0 < Ta_200 ? 6.1121 * exp( (18.678 - Ta_200/234.5) * (Ta_200/(257.14+Ta_200)) ) : 6.1115 * exp( (23.036 - Ta_200/333.7) * (Ta_200/(279.82+Ta_200)) )  )";
		//String formulaText = "(7<2?3:4)";
		//String formulaText = "exp(Ta_200)";

		Formula formula = PropertyComputation.parseFormula(formulaText);
		log.info("formula "+formula);
		Environment env = new Environment(sensorNames);

		Formula f1 = formula.accept(new FormulaResolveVisitor(env));
		log.info("f1 "+f1);
		Computation c = f1.accept(new FormulaCompileVisitor(env));
		log.info("c "+computationToString(c));

		Computation computation = formula.compile(env);

		float[] params = new float[]{1f, 2f, 3f, 4f, 5f, 6f, 7f};
		float v = computation.eval(999, params);
		log.info(v);

		String javaText = formula.compileToString(new Environment(sensorNames));
		log.info(javaText);


		ClassPool pool = ClassPool.getDefault();

		CtClass evalAClass = pool.makeClass("EvalA", pool.get(Computation.class.getName()));
		evalAClass.addMethod(CtNewMethod.make("public float eval (float[] data) { return "+javaText+"; }",evalAClass));
		@SuppressWarnings("unchecked")
		Class<? extends Computation> clazzA = evalAClass.toClass();
		Computation objA = clazzA.newInstance();
		//log.info(objA.eval(999, params));


		CtClass evalIClass = pool.makeClass("EvalI");
		evalIClass.addMethod(CtNewMethod.make("public float eval (float[] data) { return "+javaText+"; }",evalIClass));
		evalIClass.setInterfaces(new CtClass[] { pool.get(EvaluatorA.class.getName()) });
		@SuppressWarnings("unchecked")
		Class<? extends EvaluatorA> clazzI = evalIClass.toClass();
		EvaluatorA objI = clazzI.newInstance();
		log.info(objI.eval(params));

		Map<String, Function<float[], Float>> map = new HashMap<>();
		//map.put("direct", data -> (((float) Math.pow((double)(((((data[0]+data[1])+data[4])+data[6])*((((float) Math.pow((double)data[2],(double)2.7f))+((float) Math.pow((double)data[5],(double)1.2f)))+(data[1]/data[0])))/((float) Math.pow((double)data[3],(double)17.0f))),(double)(data[1]/(27.0f+data[0]))))+(((data[0]-1.0f)*(data[1]-(2.0f*((float) Math.pow((double)data[3],(double)(data[4]/123.0f))))))*(data[4]-3.0f))) );
		map.put("treeDirect", data->computation.eval(0,data));
		map.put("treeVisitor", data->c.eval(0,data));
		//map.put("genI", data->objI.eval(data));
		//map.put("genA", data->objA.eval(0,data));
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
