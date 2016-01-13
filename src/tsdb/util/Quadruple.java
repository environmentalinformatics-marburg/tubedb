package tsdb.util;

/**
 * tuple of four entries
 * immutable if a, b, c and d are immutable
 * @author woellauer
 *
 * @param <A>
 * @param <B>
 * @param <C>
 * @param <D>
 */
public class Quadruple<A,B,C,D> {
	public final A a;
	public final B b;
	public final C c;
	public final D d;
	
	public Quadruple(A a, B b, C c, D d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
}
