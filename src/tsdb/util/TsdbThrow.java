package tsdb.util;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Helper class for Exceptions.
 * @author woellauer
 *
 */
public class TsdbThrow {

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		try {
			HashMap<String,String> map = new HashMap<String,String>();
			String y = map.get("null");
			System.out.println(y.charAt(0));
		} catch (Exception e) {
			//e.printStackTrace();
			for(StackTraceElement entry:e.getStackTrace()) {
				System.err.println(entry);

				String s = /*entry.getClassName() + "." +*/ entry.getMethodName() +
						(entry.isNativeMethod() ? "(Native Method)" :
							(entry.getFileName() != null && entry.getLineNumber() >= 0 ?
									"(" + entry.getFileName() + ":" + entry.getLineNumber() + ")" :
										(entry.getFileName() != null ?  "("+entry.getFileName()+")" : "(Unknown Source)")));

				//s = "(TestingThrow.java:12)";

				s += "                 "+entry.getClassName();

				System.err.println(getClass(entry.getClassName(),entry.getFileName())+"::"+entry.getMethodName()+"   "+entry.getLineNumber());

				Integer[] x = {1,2,3};
				Arrays.asList(x).contains(1);
				Arrays.stream(x).anyMatch(y->y==1);


			}
		}

	}

	public static String getClass(String fullClassname,String filename) {
		if(filename==null || !filename.toLowerCase().endsWith(".java")) {
			return fullClassname;
		}
		String mainClassName = filename.substring(0, filename.toLowerCase().lastIndexOf(".java"));		
		int mainClassIndex = fullClassname.lastIndexOf(mainClassName);
		if(mainClassIndex<0) {
			return fullClassname;
		}
		return fullClassname.substring(mainClassIndex);
	}

	public void getMethod(StackTraceElement entry) {

	}

	public static void printStackTrace(Exception e) {
		StringBuilder s = new StringBuilder();
		s.append("*** "+e.getMessage()+" ***\n");
		for(StackTraceElement entry:e.getStackTrace()) {
			s.append(getClass(entry.getClassName(),entry.getFileName())+"::"+entry.getMethodName()+"\n");
		}
		System.err.println(s);
	}
}
