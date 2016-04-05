package tsdb.util;

/**
 * Interval with start, end and name.
 * immutable
 * @author woellauer
 *
 */
public class NamedInterval extends Interval {
	private static final long serialVersionUID = 1L;
	
	public final String name;

	protected NamedInterval(int start, int end, String name) {
		super(start, end);
		this.name = name;
	}
	
	public static NamedInterval of(int start, int end, String name) {
		return new NamedInterval(start, end, name);
	}

}
