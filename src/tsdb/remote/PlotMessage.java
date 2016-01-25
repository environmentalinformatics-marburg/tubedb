package tsdb.remote;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Contains a data receiver message and a date of one plot.
 * @author woellauer
 *
 */
public class PlotMessage implements Serializable {
	private static final long serialVersionUID = 9178544825203468112L;
	
	public final String plot;
	public final LocalDateTime dateTime;
	public final String message;
	public PlotMessage(String plot, LocalDateTime dateTime, String message) {
		this.plot = plot;
		this.dateTime = dateTime;
		this.message = message;
	}
}