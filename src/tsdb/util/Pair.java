package tsdb.util;

/**
 * general pair of two types
 * immutable if a and b are immutable
 * @author woellauer
 *
 * @param <A>
 * @param <B>
 */
public final class Pair<A,B> {
	
	public final A a;
	public final B b;
	
	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}
	
	public static <A,B> Pair<A, B> of(A a, B b) {
		return new Pair<A, B>(a,b);
	}
	
	public static <A,B> A projA(Pair<A,B> p) {
		return p.a;
	}
	
	public static <A,B> B projB(Pair<A, B> p) {
		return p.b;
	}

	@Override
	public int hashCode() {
		return 31 * ((a == null) ? 0 : a.hashCode()) + ((b == null) ? 0 : b.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Pair<?, ?> other = (Pair<?, ?>) obj;
		if (a == null) {
			if (other.a != null) return false;
		} else if (!a.equals(other.a)) 	return false;
		if (b == null) {
			if (other.b != null) return false;
		} else if (!b.equals(other.b)) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("Pair [a=%s, b=%s]", a, b);
	}
}
