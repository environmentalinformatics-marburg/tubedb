package tsdb.util;

import javax.validation.constraints.NotNull;

/**
 * Interval with start, end and name.
 * immutable
 * @author woellauer
 *
 */
public class NamedInterval extends Interval {
	private static final long serialVersionUID = 1L;
	
	public final @NotNull String name;

	/**
	 * create an interval
	 * Invariant start <= end is checked.
	 * @param start
	 * @param end
	 * @return
	 */
	protected NamedInterval(int start, int end, @NotNull String name) {
		super(start, end);
		AssumptionCheck.throwNull(name);
		this.name = name;
	}
	
	/**
	 * create an interval
	 * Invariant start <= end is checked.
	 * @param start
	 * @param end
	 * @return
	 */
	public static NamedInterval of(int start, int end, @NotNull String name) {
		return new NamedInterval(start, end, name);
	}
	
	@Override
	public String toString() {
		return "["+TimeUtil.oleMinutesToText((long) start)+" .. "+TimeUtil.oleMinutesToText((long) end)+" "+name+"]";
	}

}
