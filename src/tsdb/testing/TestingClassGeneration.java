package tsdb.testing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;

public class TestingClassGeneration {
	private static final Logger log = LogManager.getLogger();
	
	public interface Evaluator {
	    public double eval (double x);
	}
	
	public static abstract class AbstractEvaluator {		
	    public abstract double eval (double x);
	}

	public static void main(String[] args) throws Exception {
		log.info(Evaluator.class.getName());
		
		/*ClassPool pool = ClassPool.getDefault();
		CtClass evalClass = pool.makeClass("Eval");
		evalClass.addMethod(CtNewMethod.make("public double eval (double x) { return x*42; }",evalClass));
		evalClass.setInterfaces(new CtClass[] { pool.makeClass(Evaluator.class.getName()) });
		Class<?> clazz = evalClass.toClass();
		Evaluator obj = (Evaluator) clazz.newInstance();
		log.info(obj.eval(100));*/
		
		
		ClassPool pool = ClassPool.getDefault();
		CtClass evalClass = pool.makeClass("Eval",  pool.get(AbstractEvaluator.class.getName()));
		evalClass.addMethod(CtNewMethod.make("public double eval (double x) { return x*42; }",evalClass));
		@SuppressWarnings("unchecked")
		Class<? extends AbstractEvaluator> clazz = (Class<? extends AbstractEvaluator>) evalClass.toClass();
		AbstractEvaluator obj = clazz.newInstance();
		log.info(obj.eval(100));

	}

}
