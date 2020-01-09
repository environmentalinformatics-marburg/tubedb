package tsdb.testing;

/**
 * testing average aggregation of wind direction
 * @author woellauer
 *
 */
public class TestingWindDirection {

	public static void main(String[] args) {

		/*
		wd_values <- c(183,185,185,197,189,190)
		ws_values <- c(4.35,3.91,3.51,4.08,3.63,3.35)

		wd_rad <- (wd_values*pi)/180

		u_values <- -ws_values*sin(wd_rad)
		v_values <- -ws_values*cos(wd_rad)

		wd_rad_result <- atan2(u_values,v_values)+pi

		wd_result <- (wd_rad_result*180)/pi

		wd_values
		wd_result

		u_avg <- sum(u_values)/6
		v_avg <- sum(v_values)/6
		avg_rad_result <- atan2(u_avg,v_avg)+pi
		avg_result <- (avg_rad_result*180)/pi
		avg_result
		*/		
		
		float[] wd_values = new float[]{183,185,185,197,189,190};
		float[] ws_values = new float[]{4.35f,3.91f,3.51f,4.08f,3.63f,3.35f};
		
		float[] wd_rad = new float[6];
		for(int i=0;i<6;i++) wd_rad[i] = (float) ((wd_values[i]*Math.PI)/180);
		
		float[] u_values = new float[6];
		float[] v_values = new float[6];
		for(int i=0;i<6;i++) {
			u_values[i] = (float) (-ws_values[i]*Math.sin(wd_rad[i]));
			v_values[i] = (float) (-ws_values[i]*Math.cos(wd_rad[i]));
		}
		
		float[] wd_rad_result = new float[6];
		for(int i=0;i<6;i++) wd_rad_result[i] = (float) (Math.atan2(u_values[i],v_values[i])+Math.PI);
		
		float u_sum = 0;
		float v_sum = 0;
		for(int i=0;i<6;i++) {
			u_sum += u_values[i];
			v_sum += v_values[i];
		}
		float u_avg = u_sum/6;
		float v_avg = v_sum/6;
		
		float avg_rad_result = (float) (Math.atan2(u_avg,v_avg)+Math.PI);
		float avg_result = (float) ((avg_rad_result*180)/Math.PI);
		
		System.out.println("avg_result:\t"+avg_result);
		
		
		
		

	}

}
