package tsdb;

public class PropertyCNR4 {
	
	public final double swdr;
	public final double swur;
	public final double lwdr;
	public final double lwur;
	
	public PropertyCNR4(double swdr, double swur, double lwdr, double lwur) {
		this.swdr = swdr;
		this.swur = swur;
		this.lwdr = lwdr;
		this.lwur = lwur;
	}

	public static PropertyCNR4 parse(YamlMap map) {
		double swdr = map.optDouble("SWDR");
		double swur = map.optDouble("SWUR");
		double lwdr = map.optDouble("LWDR");
		double lwur = map.optDouble("LWUR");		
		return new PropertyCNR4(swdr, swur, lwdr, lwur);
	}

	@Override
	public String toString() {
		return "PropertyCNR4 [swdr=" + swdr + ", swur=" + swur + ", lwdr=" + lwdr + ", lwur=" + lwur + "]";
	}
}
