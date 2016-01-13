package tsdb.component;

import static tsdb.util.AssumptionCheck.throwNulls;

import java.io.Serializable;

import tsdb.util.Interval;

/**
 * A region names a collection of plots (from one project).
 * Every general station is associated with one region.
 * @author woellauer
 *
 */
public class Region implements Serializable {
	private static final long serialVersionUID = -8897183157291637247L;
	
	public final String name;
	public final String longName;
	public Interval viewTimeRange; //nullable  //TODO use entry
	
	public Region(String name, String longName) {
		throwNulls(name,longName);
		this.name = name;
		this.longName = longName;
		this.viewTimeRange = null;
	}

}
