package tsdb.util;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Methods of this class throw exeption and add an entry in log if an assumtion is false.
 * @author woellauer
 *
 */
public final class AssumptionCheck {
	
	private static final Logger log = LogManager.getLogger();

	private AssumptionCheck(){}

	/**
	 * Callback to generate text if assumtion is false.
	 * @author woellauer
	 *
	 */
	public static interface CallBackText {
		public String call();
	}

	/**
	 * Throws an exception with text an writes text to log.
	 * @param text
	 */
	public static void throwText(String text) {
		log.error(text);
		throw new RuntimeException(text);
	}

	/**
	 * Throws an exception if check is false.
	 * @param check
	 */
	public static void throwFalse(boolean check) {
		if(!check) {
			throwText("check false");	
		}
	}

	/**
	 * Throws an exception if check is false.
	 * @param check
	 * @param text
	 */
	public static void throwFalse(boolean check, String text) {
		if(!check) {
			throwText(text);	
		}
	}

	/**
	 * Throws an exception if check is false.
	 * @param check
	 * @param cbText
	 */
	public static void throwFalse(boolean check, CallBackText cbText) {
		if(!check) {
			try {
				throwText(cbText.call());
			} catch(Exception e) {
				throwText("unknown: "+e);
			}	
		}
	}

	/**
	 * Throws an exception if check is true.
	 * @param check
	 * @param errorText
	 */
	public static void throwTrue(boolean check, String text) {
		if(check) {
			throwText(text);	
		}		
	}

	/**
	 * Throws an exception if check is true;
	 * @param check
	 * @param cbText
	 */
	public static void throwTrue(boolean check, CallBackText cbText) {
		if(check) {
			try {
				throwText(cbText.call());
			} catch(Exception e) {
				throwText("unknown: "+e);
			}	
		}		
	}

	/**
	 * Throws an exception if object is null. 
	 * @param o
	 */
	public static void throwNull(Object o) {
		if(o==null) {
			throwText("null");
		}
	}
	
	/**
	 * Throws an exception if array or elements are null. 
	 * No nested array check.
	 * @param a
	 */
	public static <T> void throwNullArray(T[] a) {
		if(a==null) {
			throwText("null");
		}
		for(T e:a) {
			if(e==null) {
				throwText("null");
			}
		}
	}

	/**
	 * Throws an exception if object is null.
	 * @param o
	 * @param text
	 */
	public static void throwNullText(Object o, String text) {
		if(o==null) {
			throwText(text);
		}
	}

	/**
	 * Throws an exception if at least one object is null;
	 * @param o
	 */
	public static void throwNulls(Object ... o) {
		if(o==null) {
			throwText("array null");
		}
		for(int i=0;i<o.length;i++) {				
			if(o[i]==null) {
				throwText("null in "+(i+1)+".");
			}
		}
	}

	/**
	 * Throws an exception if a is greater than b.
	 * @param a if null, check is not performed.
	 * @param b if null, check is not performed.
	 */
	public static void throwGreater(Long a, Long b) {
		if(a!=null&&b!=null&&a>b) {
			throwText("not a<b: "+a+"  "+b);
		}
	}

	/**
	 * Throws an exception if a is greater than b.
	 * @param a
	 * @param b
	 */
	public static void throwGreater(long a, long b) {
		if(a>b) {
			throwText("not a<b: "+a+"  "+b);
		}
	}
	
	/**
	 * Throws if array is null or empty
	 * @param array
	 */
	public static void throwEmpty(Object[] array) {
		if(array==null||array.length==0) {
			throw new RuntimeException("array empty");
		}		
	}
	
	public static void throwNotContained(String[] source, String[] target) {
		throwNull(source);
		throwNull(target);
		Map<String, Integer> map = Util.stringArrayToMap(target);
		for(String name:source) {
			if(!map.containsKey(name)) {
				throw new RuntimeException("name not in target: "+name+"   "+Util.arrayToString(target));
			}
		}
	}
}
