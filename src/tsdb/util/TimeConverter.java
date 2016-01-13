package tsdb.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Converter of timestamps with reference to file time data.
 * immutable
 * @author woellauer
 *
 */
public final class TimeConverter {
	
	private final double dActTimeToSecondFactor;
	private final double startTime;
	private final double startTimeToDayFactor;
	//private final double sampleRate;

	public TimeConverter(double startTimeToDayFactor, double dActTimeToSecondFactor, double startTime, double sampleRate) {
		this.startTimeToDayFactor = startTimeToDayFactor;
		this.dActTimeToSecondFactor = dActTimeToSecondFactor;
		this.startTime = startTime;
		//this.sampleRate = sampleRate;
	}	

	public double offsetToOleAutomatonTime(int offset) {
		return startTime*startTimeToDayFactor + (offset*dActTimeToSecondFactor)/86400d;		
	}

	public LocalDateTime offsetToDateTime(int offset) {
		return TimeUtil.oleAutomatonTimeToDateTime(offsetToOleAutomatonTime(offset));
	}

	public double getStartOleAutomatonTime() {
		return startTime*startTimeToDayFactor;
	}

	public LocalDateTime getStartDateTime() {
		return TimeUtil.oleAutomatonTimeToDateTime(getStartOleAutomatonTime());
	}

	public long getStartTimeOleMinutes() {
		return (long) Math.round(getStartOleAutomatonTime()*24d*60d);
	}

	public Duration getTimeStep() {
		long oleAutomatonTimeSeconds = (long) Math.round((dActTimeToSecondFactor/86400d)*24*60*60);		
		return Duration.ofSeconds(oleAutomatonTimeSeconds);
	}

	public long getTimeStepMinutes() {
		return (long) Math.round((dActTimeToSecondFactor/86400d)*24d*60d);
	}

}
