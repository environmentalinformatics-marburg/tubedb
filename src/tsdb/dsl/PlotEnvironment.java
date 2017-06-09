package tsdb.dsl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import tsdb.Plot;
import tsdb.dsl.formula.Formula;
import tsdb.dsl.formula.FormulaNum;

public class PlotEnvironment extends Environment {

	private final Plot plot;

	public static final String[] RESOLVERS = new String[]{
			"latitude",
			"longitude"
	};

	public static final HashSet<String> RESOLVER_SET = new HashSet<String>(Arrays.asList(RESOLVERS));	

	public PlotEnvironment(Plot plot, Map<String, Integer> sensorMap) {
		super(sensorMap);
		this.plot = plot;
	}

	public boolean containsResolver(String name) {
		return RESOLVER_SET.contains(name);
	}

	@Override
	public Formula resolve(String name) {
		switch(name) {
		case "latitude":
			return FormulaNum.of((float)plot.getLatLon()[0]);
		case "longitude":
			return FormulaNum.of((float)plot.getLatLon()[1]);
		case "elevation":
			return FormulaNum.of((float)plot.getElevation());			
		default:
			throw new RuntimeException("unknown variable "+name);
		}
	}
}
