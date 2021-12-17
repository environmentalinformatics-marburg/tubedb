package tsdb.component.labeledproperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


import org.tinylog.Logger;

import tsdb.dsl.Environment;
import tsdb.dsl.FormulaBuilder;
import tsdb.dsl.FormulaCollectUnsafeVarVisitor;
import tsdb.dsl.FormulaCompileVisitor;
import tsdb.dsl.FormulaResolveUnifyVisitor;
import tsdb.dsl.formula.Formula;
import tsdb.util.Computation;
import tsdb.util.DataRow;
import tsdb.util.Mutator;
import tsdb.util.Mutators;
import tsdb.util.Util;
import tsdb.util.yaml.YamlMap;

public class PropertyComputation {
	

	public final String target;
	public final Formula formula_org;

	public static PropertyComputation parse(YamlMap map) {
		return parse(map.optString("target"), map.optString("formula"));
	}

	public static PropertyComputation parse(String target, String formulaText) {
		if(target==null) {
			Logger.error("missing target");
			return null;
		}
		Formula formula = FormulaBuilder.parseFormula(formulaText);
		return new PropertyComputation(target, formula);	
	}	

	public PropertyComputation(String target, Formula formula) {
		this.target = target;
		this.formula_org = formula;
	}

	public void calculate(Collection<DataRow> rows, String[] sensorNames) {
		Map<String, Integer> sensorMap = Util.stringArrayToMap(sensorNames, true);
		Integer p = sensorMap.get(target);
		if(p == null) {
			throw new RuntimeException("target not found: "+target+"  in "+Arrays.toString(sensorNames));
		}
		int pos = p;
		Environment env = new Environment(sensorMap);
		Formula formula = formula_org.accept(new FormulaResolveUnifyVisitor(env));
		Computation computation = formula.accept(new FormulaCompileVisitor(env));
		for(DataRow row:rows) {
			float[] data = row.data;
			data[pos] = computation.eval(row.timestamp, data);
		}
	}

	public void calculate(Collection<DataRow> rows, String[] sensorNames, long firstTimestamp, long lastTimestamp) {
		Iterator<DataRow> it = rows.iterator();
		if(!it.hasNext()) {
			return;
		}
		DataRow cur = it.next();
		while(cur.timestamp<firstTimestamp) {
			if(!it.hasNext()) {
				return;
			}
			cur = it.next();
		}
		
		Map<String, Integer> sensorMap = Util.stringArrayToMap(sensorNames, true);
		Integer p = sensorMap.get(target);
		if(p == null) {
			throw new RuntimeException("target not found: "+target+"  in "+Arrays.toString(sensorNames));
		}
		int pos = p;
		Environment env = new Environment(sensorMap);
		Formula formula = formula_org.accept(new FormulaResolveUnifyVisitor(env));
		int[] unsafeVarIndices = formula.accept(new FormulaCollectUnsafeVarVisitor()).getDataVarIndices(env);
		Computation computation = formula.accept(new FormulaCompileVisitor(env));
		Mutator mutator = Mutators.getMutator(computation, pos, unsafeVarIndices);
		
		while(cur.timestamp<=lastTimestamp) {			
			mutator.apply(cur.timestamp, cur.data);
			if(!it.hasNext()) {
				return;
			}
			cur = it.next();
		}
	}
}
