package tsdb.loader.be;

import java.io.Serializable;

/**
 * This class contains header data from UDBF-File of one sensor.
 * @author woellauer
 *
 */
class SensorHeader implements Serializable {
	
	private static final long serialVersionUID = -4739683382625294938L;
	
	public final String name;
	public final String unit;
	public final short dataType;

	public SensorHeader(String name, String unit, short dataType) {
		this.name = name;
		this.unit = unit;
		this.dataType = dataType;
	}

	public void printHeader() {
		System.out.println("sensor: "+name+"\t unit: "+unit);
		
	}
	
	public String getName() {
		return name;
	}
	
	public String getUnit() {
		return unit;
	}
	
	@Override
	public String toString() {
		return name+":"+unit+":"+dataType;
	}
	
	public static String[] toSensorNames(SensorHeader[] sensorHeaders) {
		int len = sensorHeaders.length;
		String[] names = new String[len];
		for (int i = 0; i < len; i++) {
			names[i] = sensorHeaders[i].name;
		}
		return names;
	}

}
