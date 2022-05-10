package tsdb.util;

import java.io.Serializable;
import java.util.Arrays;

public class Measurement implements Serializable {
	
	public final String name;
	public final DataEntry[] values;
	
	public Measurement(String name, DataEntry[] values) {
		this.name = name;
		this.values = values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(values);
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
		Measurement other = (Measurement) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Measurement [name=" + name + ", values=" + Arrays.toString(values) + "]";
	}	
}
