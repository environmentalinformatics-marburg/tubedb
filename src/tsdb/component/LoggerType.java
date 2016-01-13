package tsdb.component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * This class contains metadata that associated with a logger type (station type).
 * @author woellauer
 *
 */
public class LoggerType implements Serializable {
	private static final long serialVersionUID = -8623721500856325122L;
	public String typeName;
	public String[] sensorNames;
	public Map<String,String> sensorNameTranlationMap;
	
	public LoggerType(String typeName, String[] sensorNames) {
		this.typeName = typeName;
		this.sensorNames = sensorNames;
		this.sensorNameTranlationMap = new HashMap<String, String>();
	}
	
	@Override
	public String toString() {
		return "logger: "+typeName;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((typeName == null) ? 0 : typeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LoggerType other = (LoggerType) obj;
		if (typeName == null) {
			if (other.typeName != null)
				return false;
		} else if (!typeName.equals(other.typeName))
			return false;
		return true;
	}
}
