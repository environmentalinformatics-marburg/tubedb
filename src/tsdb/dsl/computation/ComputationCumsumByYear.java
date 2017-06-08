package tsdb.dsl.computation;

import tsdb.util.TimeUtil;

public class ComputationCumsumByYear extends Computation {
	private final Computation parameter;
	private long timestampMin = 0;
	private long timestampMax = 0;
	private float acc = 0f;
	
	public ComputationCumsumByYear(Computation parameter) {
		this.parameter = parameter;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		if(timestamp<timestampMin || timestampMax<timestamp) {
			int currentYear = TimeUtil.oleMinutesToLocalDateTime(timestamp).getYear();
			timestampMin = TimeUtil.ofDateStartMinute(currentYear);
			timestampMax = TimeUtil.ofDateEndMinute(currentYear);
			acc = 0f;
		}
		float a = parameter.eval(timestamp, data);
		acc += a;
		return acc;	
	}
}
