package tsdb.dsl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TimeUtil;

public class FormulaVar extends Formula {
	private static final Logger log = LogManager.getLogger();

	private static final String[] NON_DATA_VARIABLES = new String[]{
			"year",
			"year_float",
			"year_fraction",
			"month", 
			"days_of_month", 
			"day_of_month", 
			"day_of_month_float", 
			"days_of_year", 
			"day_of_year", 
			"day_of_year_float", 
			"day_fraction",  
			"hour"
	};
	private static final HashSet<String> NON_DATA_VARIABLES_SET = new HashSet<String>(Arrays.asList(NON_DATA_VARIABLES));

	public final String name;
	public final boolean positive;


	public FormulaVar(String name, boolean positive) {
		this.name = name;
		this.positive = positive;
	}

	@Override
	public Computation compile(Map<String,Integer> sensorMap) {
		switch(name) {
		case "year":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return TimeUtil.oleMinutesToLocalDateTime(timestamp).getYear();				
					}
				};	
			} else {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return - TimeUtil.oleMinutesToLocalDateTime(timestamp).getYear();			
					}
				};				
			}
		case "year_float":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {						
						LocalDateTime d = TimeUtil.oleMinutesToLocalDateTime(timestamp);
						return d.getYear() + (d.getDayOfYear()-1+d.getHour()/24f) / d.toLocalDate().lengthOfYear();			
					}
				};	
			} else {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						LocalDateTime d = TimeUtil.oleMinutesToLocalDateTime(timestamp);
						return - d.getYear() + (d.getDayOfYear()-1+d.getHour()/24f) / d.toLocalDate().lengthOfYear();			
					}
				};				
			}
		case "year_fraction":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {						
						LocalDateTime d = TimeUtil.oleMinutesToLocalDateTime(timestamp);
						return (d.getDayOfYear()-1+d.getHour()/24f) / d.toLocalDate().lengthOfYear();			
					}
				};	
			} else {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						LocalDateTime d = TimeUtil.oleMinutesToLocalDateTime(timestamp);
						return - (d.getDayOfYear()-1+d.getHour()/24f) / d.toLocalDate().lengthOfYear();			
					}
				};				
			}			
		case "month":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return TimeUtil.oleMinutesToLocalDateTime(timestamp).getMonthValue();				
					}
				};	
			} else {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return - TimeUtil.oleMinutesToLocalDateTime(timestamp).getMonthValue();			
					}
				};				
			}
		case "day_of_month":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return TimeUtil.oleMinutesToLocalDateTime(timestamp).getDayOfMonth();				
					}
				};	
			} else {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return - TimeUtil.oleMinutesToLocalDateTime(timestamp).getDayOfMonth();		
					}
				};				
			}
		case "days_of_month":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return TimeUtil.oleMinutesToLocalDateTime(timestamp).toLocalDate().lengthOfMonth();				
					}
				};	
			} else {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return - TimeUtil.oleMinutesToLocalDateTime(timestamp).toLocalDate().lengthOfMonth();	
					}
				};				
			}			
		case "day_of_month_float":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						LocalDateTime d = TimeUtil.oleMinutesToLocalDateTime(timestamp);
						return d.getDayOfMonth()+d.getHour()/24f;				
					}
				};	
			} else {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						LocalDateTime d = TimeUtil.oleMinutesToLocalDateTime(timestamp);
						return - d.getDayOfMonth()+d.getHour()/24f;		
					}
				};				
			}			
		case "day_of_year":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return TimeUtil.oleMinutesToLocalDateTime(timestamp).getDayOfYear();				
					}
				};	
			} else {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return - TimeUtil.oleMinutesToLocalDateTime(timestamp).getDayOfYear();	
					}
				};				
			}
		case "days_of_year":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return TimeUtil.oleMinutesToLocalDateTime(timestamp).toLocalDate().lengthOfYear();				
					}
				};	
			} else {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return - TimeUtil.oleMinutesToLocalDateTime(timestamp).toLocalDate().lengthOfYear();	
					}
				};				
			}			
		case "day_of_year_float":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						LocalDateTime d = TimeUtil.oleMinutesToLocalDateTime(timestamp);
						return d.getDayOfYear()+d.getHour()/24f;				
					}
				};	
			} else {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						LocalDateTime d = TimeUtil.oleMinutesToLocalDateTime(timestamp);
						return - d.getDayOfYear()+d.getHour()/24f;	
					}
				};				
			}
		case "day_fraction":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return TimeUtil.oleMinutesToLocalDateTime(timestamp).getHour()/24f;			
					}
				};	
			} else {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return - TimeUtil.oleMinutesToLocalDateTime(timestamp).getHour()/24f;		
					}
				};				
			}	
		case "hour":
			if(positive) {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return TimeUtil.oleMinutesToLocalDateTime(timestamp).getHour();				
					}
				};	
			} else {
				return new Computation(){
					@Override
					public float eval(long timestamp, float[] data) {
						return - TimeUtil.oleMinutesToLocalDateTime(timestamp).getHour();	
					}
				};				
			}			
		default:
			Integer p = sensorMap.get(name);
			if(p == null) {
				throw new RuntimeException("sensor not found: "+name+"  in  "+sensorMap);
			}
			if(positive) {
				return new Computation(){
					int pos = sensorMap.get(name);
					@Override
					public float eval(long timestamp, float[] data) {
						return data[pos];				
					}
				};
			} else {
				return new Computation(){
					int pos = sensorMap.get(name);
					@Override
					public float eval(long timestamp, float[] data) {
						return - data[pos];				
					}
				};
			}
		}
	}

	@Override
	public String compileToString(Map<String, Integer> sensorMap) {
		Integer p = sensorMap.get(name);
		if(p == null) {
			throw new RuntimeException("sensor not found: "+name+"  in  "+sensorMap);
		}
		int pos = p.intValue();
		if(positive) {
			return "data["+pos+"]";
		} else {
			return " - data["+pos+"]";
		}
	}

	@Override
	public void collectDataVariables(Set<String> collector) {
		if(!NON_DATA_VARIABLES_SET.contains(name)) {
			collector.add(name);		
		}
	}
}
