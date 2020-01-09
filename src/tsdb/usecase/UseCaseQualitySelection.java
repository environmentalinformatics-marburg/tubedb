package tsdb.usecase;

import java.util.EnumSet;

public class UseCaseQualitySelection {
	
	public static void main(String[] args) {
		System.out.println("start...");
		
		
		quality(EnumSet.of(QualityFlag.PHYSICAL_RANGE_CHECK, QualityFlag.EMPIRICAL_RANGE_CHECK, QualityFlag.STEP_CHECK));
		
		
		
		System.out.println("...end");
	}
	
	public enum QualityFlag { STEP_CHECK, EMPIRICAL_RANGE_CHECK, PHYSICAL_RANGE_CHECK };
	
	public static void quality(boolean stepCheck, boolean empiricalRangeCheck, boolean physicalRangeCheck) {
		
	}
	
	public static void quality( EnumSet<QualityFlag> qualityFlags) {
		
	}

}
