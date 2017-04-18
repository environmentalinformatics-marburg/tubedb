package tsdb.component.labeledproperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.dsl.FormulaLexer;
import tsdb.dsl.FormulaParser;
import tsdb.util.DataRow;
import tsdb.util.Util;
import tsdb.util.yaml.YamlMap;

public class PropertyComputation {
	private static final Logger log = LogManager.getLogger();

	public final String target;
	public final Formula formula;

	public static PropertyComputation parse(YamlMap map) {
		return parse(map.optString("target"), map.optString("formula"));
	}

	public static PropertyComputation parse(String target, String formulaText) {
		if(target==null) {
			log.error("missing target");
			return null;
		}
		Formula formula = parseFormula(formulaText);

		return new PropertyComputation(target, formula);	
	}

	public static Formula parseFormula(String formulaText) {
		if(formulaText==null || formulaText.trim().isEmpty()) {
			log.error("missing formula");
			return null;
		}		
		CodePointCharStream stream = CharStreams.fromString(formulaText, "formula");
		FormulaLexer lexer = new FormulaLexer(stream);	
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		FormulaParser parser = new FormulaParser(tokens);
		Formula formula = parser.expression().accept(FormulaCompileVisitor.DEFAULT);
		return formula;
	}	

	public PropertyComputation(String target, Formula formula) {
		this.target = target;
		this.formula = formula;
	}

	public void calculate(Collection<DataRow> rows, String[] sensorNames) {
		Map<String, Integer> sensorMap = Util.stringArrayToMap(sensorNames, true);
		Integer p = sensorMap.get(target);
		if(p == null) {
			throw new RuntimeException("target not found: "+target+"  in "+Arrays.toString(sensorNames));
		}
		int pos = p;
		Computation computation = formula.compile(sensorMap);
		for(DataRow row:rows) {
			float[] data = row.data;
			data[pos] = computation.eval(data);
		}
	}

	public void calculate(Collection<DataRow> rows, String[] sensorNames, long firstTimestamp, long lastTimestamp) {
		Map<String, Integer> sensorMap = Util.stringArrayToMap(sensorNames, true);
		Integer p = sensorMap.get(target);
		if(p == null) {
			throw new RuntimeException("target not found: "+target+"  in "+Arrays.toString(sensorNames));
		}
		int pos = p;
		Computation computation = formula.compile(sensorMap);
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
		while(cur.timestamp<=lastTimestamp) {
			float[] data = cur.data;
			data[pos] = computation.eval(data);
			if(!it.hasNext()) {
				return;
			}
			cur = it.next();
		}
	}
}
