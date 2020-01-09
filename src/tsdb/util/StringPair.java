package tsdb.util;

/**
 * Pair of type String
 */
public class StringPair extends Pair<String, String> {
	public StringPair(String a, String b) {
		super(a, b);
	}
	
	public static StringPair of(String a, String b) {
		return new StringPair(a,b);
	}
}
