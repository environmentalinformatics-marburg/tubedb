package tsdb;

import java.util.Collection;

import tsdb.util.DataRow;
import tsdb.util.Util;

public class PropertyCNR4 {

	private static final String[] names = new String[]{"SWDR_300", "SWDR_300_U", "SWUR_300", "SWUR_300_U", "LWDR_300", "LWDR_300_U", "LWUR_300", "LWUR_300_U", "Trad"};
	private static final double sigma =  5.670367E-8;  // Stefan–Boltzmann constant 5.670367*10^-8 W/(m^2*K^4)
	private static final double zero_degree = 273.15; // 0°C in K

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

	public void calculate(Collection<DataRow> rows, String[] sensorNames) {
		int[] index = Util.stringArrayToPositionIndexArray(names,sensorNames, false, true);
		
		int i_swdr = index[0];
		int i_swdr_u = index[1];
		int i_swur = index[2];
		int i_swur_u = index[3];
		
		int i_lwdr = index[4];
		int i_lwdr_u = index[5];
		int i_lwur = index[6];
		int i_lwur_u = index[7];
		
		int t_temp = index[8];


		boolean is_swdr = Double.isFinite(swdr);
		boolean is_swur = Double.isFinite(swur);
		boolean is_lwdr = Double.isFinite(lwdr);
		boolean is_lwur = Double.isFinite(lwur);

		for(DataRow row:rows) {
			Float[] v = row.data;
			if(is_swdr) {
				v[i_swdr] = (float) (v[i_swdr_u]*1000d/swdr);
			}
			if(is_swur) {
				v[i_swur] = (float) (v[i_swur_u]*1000d/swur);
			}

			if(is_lwdr|is_lwur) {
				double additional = sigma*Math.pow(v[t_temp]+zero_degree, 4d);
				if(is_lwdr) {
					v[i_lwdr] = (float) (v[i_lwdr_u]*1000d/lwdr + additional);
				}
				if(is_lwur) {
					v[i_lwur] = (float) (v[i_lwur_u]*1000d/lwur + additional);
				}
			}
		}
	}
}
