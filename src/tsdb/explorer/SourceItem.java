package tsdb.explorer;

import tsdb.component.SourceEntry;

/**
 * Items in View of SourceCatalog
 * @author woellauer
 *
 */
public class SourceItem {	
	public final SourceEntry sourceEntry;
	public String generalStationName;
	public String regionName;
	public String plotid;
	
	public SourceItem(SourceEntry sourceEntry) {
		this.sourceEntry = sourceEntry;
		this.generalStationName = null;
		this.regionName = null;
		this.plotid = null;
	}
}
