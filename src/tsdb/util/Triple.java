package tsdb.util;

/**
 * tuple of three entries
 * immutable if a, b and c are immutable
 * @author woellauer
 *
 * @param <A>
 * @param <B>
 * @param <C>
 */
public class Triple<A,B,C> {
	public final A a;
	public final B b;
	public final C c;
	
	public Triple(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
}
