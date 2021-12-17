package tsdb.testing;

import java.util.Arrays;
import java.util.List;


import org.tinylog.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.bytecode.MethodInfo;
import tsdb.dsl.Environment;
import tsdb.dsl.FormulaBuilder;
import tsdb.dsl.FormulaCollectUnsafeVarVisitor;
import tsdb.dsl.FormulaCompileVisitor;
import tsdb.dsl.FormulaJavaVisitor;
import tsdb.dsl.FormulaResolveUnifyVisitor;
import tsdb.dsl.formula.Formula;
import tsdb.util.Computation;
import tsdb.util.DataRow;
import tsdb.util.Mutator;
import tsdb.util.Mutators;
import tsdb.util.Timer;

public class TestingFormulaCompile {
	

	public static void main(String[] args) throws Exception {
		String[] sensorNames = new String[] {"a", "rH_200", "PAR_300_U", "PAR_300", "Ta_200", "c", "p_QNH", "Ta_200_max", "Ta_200_min", "b", "WV"};
		String sensorNameTarget = "Ta_200";
		Environment env = new Environment(sensorNames);
		int targetIndex = env.getSensorIndex(sensorNameTarget);

		//String formulaText = "((month > 2 ? 1 : (month == 2 ? 0.75 : 0.5))) * ((Ta_200 > 0 ? Ta_200 : 0))";
		//String formulaText = "(Ta_200 + 27 - a / PAR_300_U) * c + a^(1.07+WV)";
		//String formulaText = "(26.7 <= Ta_200 && 40 <= rH_200 ? -8.784695 +1.61139411*Ta_200 +2.338549*rH_200 -0.14611605*Ta_200*rH_200 -1.2308094e-2*Ta_200^2 -1.6424828e-2*rH_200^2 +2.211732e-3*Ta_200^2*rH_200 +7.2546e-4*Ta_200*rH_200^2 -3.582e-6*Ta_200^2*rH_200^2 : Ta_200 -8.784695 +1.61139411*Ta_200 +2.338549*rH_200 -0.14611605*Ta_200*rH_200 -1.2308094e-2*Ta_200^2 -1.6424828e-2*rH_200^2 +2.211732e-3*Ta_200^2*rH_200 +7.2546e-4*Ta_200*rH_200^2 -3.582e-6*Ta_200^2*rH_200^2)";
		String formulaText = "((rH_200>10 || c<7 ? PAR_300 : p_QNH)) * (a+rH_200+PAR_300_U+PAR_300+Ta_200+c+p_QNH+Ta_200_max+Ta_200_min+b+WV)";

		int SIZE = 1_000_000;
		DataRow[] dataRows = new DataRow[SIZE];
		for (int i = 0; i < dataRows.length; i++) {
			float[] data = new float[] {1,2,3,4,5,6,7,8,9,10,11};
			long timestamp = i;
			dataRows[i] = new DataRow(data, timestamp);
		}

		Formula formula_org = FormulaBuilder.parseFormula(formulaText);
		Formula formula = formula_org.accept(new FormulaResolveUnifyVisitor(env));
		int[] unsafeVarIndices = formula.accept(new FormulaCollectUnsafeVarVisitor()).getDataVarIndices(env);
		Logger.info("unsafeVarIndices " + Arrays.toString(unsafeVarIndices));
		Computation computation = formula.accept(new FormulaCompileVisitor(env));
		Mutator mutator = Mutators.getMutator(computation, targetIndex, unsafeVarIndices);


		FormulaJavaVisitor formulaJavaVisitor = new FormulaJavaVisitor(env);
		String javaText = formula.accept(formulaJavaVisitor);
		Logger.info(javaText);
		Computation genComputation = createComputation(formulaJavaVisitor, javaText);
		Logger.info(genComputation);
		Mutator genComputationMutator = Mutators.getMutator(genComputation, targetIndex, unsafeVarIndices);

		Mutator genMutator = createMutator(formulaJavaVisitor, javaText, targetIndex, unsafeVarIndices);

		int REPEATS = 10;
		int LOOPS = 10;       

		for (int repeat = 0; repeat < REPEATS; repeat++) {

			Timer.start("tree");
			for (int loop = 0; loop < LOOPS; loop++) {
				for(DataRow dataRow:dataRows) {
					mutator.apply(dataRow.timestamp, dataRow.data);
				}
			}
			Logger.info(Timer.stop("tree"));

			Timer.start("genComputation");
			for (int loop = 0; loop < LOOPS; loop++) {
				for(DataRow dataRow:dataRows) {
					genComputationMutator.apply(dataRow.timestamp, dataRow.data);
				}
			}
			Logger.info(Timer.stop("genComputation"));

			Timer.start("genMutator");
			for (int loop = 0; loop < LOOPS; loop++) {
				for(DataRow dataRow:dataRows) {
					genMutator.apply(dataRow.timestamp, dataRow.data);
				}
			}
			Logger.info(Timer.stop("genMutator"));

		}
	}

	private static Computation createComputation(FormulaJavaVisitor formulaJavaVisitor, String javaText) throws Exception {
		ClassPool pool = ClassPool.getDefault();
		CtClass ctComputation = pool.get(Computation.class.getName());
		CtClass evalAClass = pool.makeClass("EvalA", ctComputation);
		for (int i = 0; i < formulaJavaVisitor.computations.size(); i++) {
			Logger.info("add field");
			evalAClass.addField(new CtField(ctComputation, "c"+i, evalAClass));	
		}
		evalAClass.addMethod(CtNewMethod.make("public float eval(long timestamp, float[] data) { return "+javaText+"; }",evalAClass));
		CtClass[] parameters = new CtClass[formulaJavaVisitor.computations.size()];
		/*for (int i = 0; i < parameters.length; i++) {
			parameters[i] = ctComputation;
		}*/
		parameters = new CtClass[]{pool.get(List.class.getName())};
		CtConstructor ctConstructor = new CtConstructor(parameters, evalAClass);
		Logger.info(ctConstructor.getSignature());
		MethodInfo mi = ctConstructor.getMethodInfo();
		System.out.println(mi.getClass()+"   "+parameters.length);

		String body = "{";
		for (int i = 0; i < formulaJavaVisitor.computations.size(); i++) {
			body += "System.out.println($1.getClass());";
			//body += "this.c"+i+"=$1.get("+i+");";
			body += "c"+i+" = (tsdb.util.Computation)$1.get("+i+");";
		}
		body += "}";
		Logger.info("body "+body);
		ctConstructor.setBody(body);
		evalAClass.addConstructor(ctConstructor);

		@SuppressWarnings("unchecked")
		Class<? extends Computation> clazzA = (Class<? extends Computation>) evalAClass.toClass();

		Computation objA = (Computation) clazzA.getConstructors()[0].newInstance(formulaJavaVisitor.computations);
		Logger.info(objA.toString());
		return objA;
	}

	private static Mutator createMutator(FormulaJavaVisitor formulaJavaVisitor, String javaText, int targetIndex, int... parameterIndices) throws Exception {
		ClassPool pool = ClassPool.getDefault();
		CtClass ctMutator = pool.get(Mutator.class.getName());
		CtClass mutatorAClass = pool.makeClass("MutatorA", ctMutator);
		CtClass ctComputation = pool.get(Computation.class.getName());
		for (int i = 0; i < formulaJavaVisitor.computations.size(); i++) {
			Logger.info("add field");
			//CtClass ctComputation = pool.get(formulaJavaVisitor.computations.get(i).getClass().getName());
			mutatorAClass.addField(new CtField(ctComputation, "c"+i, mutatorAClass));	
		}
		String prog;
		if(parameterIndices.length == 0) {
			prog = "data[" + targetIndex + "] = " + javaText + ";";
		} else {
			String pred = "Float.isFinite(data[" + parameterIndices[0] + "])";
			for(int i=1; i<parameterIndices.length; i++) {
				pred += " && Float.isFinite(data[" + parameterIndices[i] + "])";
			}
			prog = "data[" + targetIndex + "] = " + pred +" ? " + javaText + " : Float.NaN;";
		}
		mutatorAClass.addMethod(CtNewMethod.make("public void apply(long timestamp, float[] data) { "+ prog +" }",mutatorAClass));
		CtClass[] parameters = new CtClass[formulaJavaVisitor.computations.size()];
		/*for (int i = 0; i < parameters.length; i++) {
			parameters[i] = ctComputation;
		}*/
		parameters = new CtClass[]{pool.get(List.class.getName())};
		CtConstructor ctConstructor = new CtConstructor(parameters, mutatorAClass);
		Logger.info(ctConstructor.getSignature());
		MethodInfo mi = ctConstructor.getMethodInfo();
		System.out.println(mi.getClass()+"   "+parameters.length);

		String body = "{";
		for (int i = 0; i < formulaJavaVisitor.computations.size(); i++) {
			body += "System.out.println($1.getClass());";
			body += "c"+i+" = (tsdb.util.Computation)$1.get("+i+");";
			//body += "c"+i+" = ("+formulaJavaVisitor.computations.get(i).getClass().getName()+")$1.get("+i+");";
		}
		body += "}";
		Logger.info("body "+body);
		ctConstructor.setBody(body);
		mutatorAClass.addConstructor(ctConstructor);

		@SuppressWarnings("unchecked")
		Class<? extends Computation> clazzA = (Class<? extends Computation>) mutatorAClass.toClass();

		Mutator objA = (Mutator) clazzA.getConstructors()[0].newInstance(formulaJavaVisitor.computations);
		Logger.info(objA.toString());
		return objA;
	}



}
