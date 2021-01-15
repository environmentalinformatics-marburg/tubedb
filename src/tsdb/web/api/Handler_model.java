package tsdb.web.api;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.json.JSONWriter;

import tsdb.component.Sensor;
import tsdb.dsl.FormulaBuilder;
import tsdb.dsl.FormulaJavaVisitor;
import tsdb.dsl.FormulaToJsonTreeVisitor;
import tsdb.dsl.FormulaToStringVisitor;
import tsdb.dsl.FormulaUnifyVisitor;
import tsdb.dsl.formula.Formula;
import tsdb.remote.RemoteTsDB;

public class Handler_model extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_model(RemoteTsDB tsdb) {
		super(tsdb, "model");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("application/json;charset=utf-8");
		JSONWriter json = new JSONWriter(response.getWriter());
		writeModel(json);		
	}

	private void writeModel(JSONWriter json) throws RemoteException {
		json.object();
		json.key("model");
		json.object();
		json.key("sensors");
		json.object();
		for(Sensor sensor : tsdb.getSensors()) {
			json.key(sensor.name);
			json.object();
			if(sensor.description != null) {
				json.key("description");
				json.value(sensor.description);
			}
			if(sensor.unitDescription != null) {
				json.key("unit");
				json.value(sensor.unitDescription);
			}
			json.key("aggregation_hour");
			json.value(sensor.getAggregationHour().toString());
			json.key("aggregation_day");
			json.value(sensor.getAggregationDay().toString());
			json.key("aggregation_week");
			json.value(sensor.getAggregationWeek().toString());
			json.key("aggregation_month");
			json.value(sensor.getAggregationMonth().toString());
			json.key("aggregation_year");
			json.value(sensor.getAggregationYear().toString());
			if(-Float.MAX_VALUE < sensor.physicalMin || sensor.physicalMax < Float.MAX_VALUE) {
				json.key("physical_range");
				json.array();
				json.value(sensor.physicalMin);
				json.value(sensor.physicalMax);
				json.endArray();
			}
			if(0f < sensor.stepMin || sensor.stepMax < Float.MAX_VALUE) {
				json.key("step_range");
				json.array();
				json.value(sensor.stepMin);
				json.value(sensor.stepMax);
				json.endArray();
			}
			if(sensor.empiricalDiff != null) {
				json.key("empirical_diff");
				json.value(sensor.empiricalDiff);
			}
			if(sensor.useInterpolation) {
				json.key("interpolation_mse");
				json.value(sensor.maxInterpolationMSE);
			}
			json.key("category");
			json.value(sensor.category.toString());
			json.key("visibility");
			json.value(sensor.internal ? "internal" : "public");
			if(sensor.isDerived()) {
				json.key("derived");
				json.value(sensor.isDerived());
			}
			if(sensor.raw_source != null && sensor.raw_source.length > 0) {
				json.key("raw_source");
				json.array();
				for(String name:sensor.raw_source) {
					json.value(name);
				}
				json.endArray();
			}
			if(sensor.dependency != null && sensor.dependency.length > 0) {
				json.key("dependency");
				json.array();
				for(String name:sensor.dependency) {
					json.value(name);
				}
				json.endArray();
			}
			if(sensor.raw_func != null) {
				json.key("raw_func");
				json.value(sensor.raw_func);

				try {
					Formula formula_org = FormulaBuilder.parseFormula(sensor.raw_func);
					Formula formula_unified = formula_org.accept(FormulaUnifyVisitor.DEFAULT);
					String funcText = formula_unified.accept(FormulaToStringVisitor.DEFAULT);
					if(funcText != null && !funcText.isEmpty()) {
						json.key("raw_func_parsed");				
						json.value(funcText);
						json.key("raw_func_tree");				
						formula_unified.accept(new FormulaToJsonTreeVisitor(json));		
					}
				} catch(Exception e) {
					log.warn(e);
				}
			}
			if(sensor.post_hour_func != null) {
				json.key("post_hour_func");
				json.value(sensor.post_hour_func);
				
				try {
					Formula formula_org = FormulaBuilder.parseFormula(sensor.post_hour_func);
					Formula formula_unified = formula_org.accept(FormulaUnifyVisitor.DEFAULT);
					String funcText = formula_unified.accept(FormulaToStringVisitor.DEFAULT);
					FormulaToJsonTreeVisitor jsonVisitor = new FormulaToJsonTreeVisitor(json);
					formula_unified.accept(jsonVisitor);
					if(funcText != null && !funcText.isEmpty()) {
						json.key("post_hour_func_parsed");				
						json.value(funcText);	
						json.key("post_hour_func_tree");				
						formula_unified.accept(new FormulaToJsonTreeVisitor(json));		
					}
				} catch(Exception e) {
					log.warn(e);
				}
			}
			if(sensor.post_day_func != null) {
				json.key("post_day_func");
				json.value(sensor.post_day_func);
				
				try {
					Formula formula_org = FormulaBuilder.parseFormula(sensor.post_day_func);
					Formula formula_unified = formula_org.accept(FormulaUnifyVisitor.DEFAULT);
					String funcText = formula_unified.accept(FormulaToStringVisitor.DEFAULT);
					if(funcText != null && !funcText.isEmpty()) {
						json.key("post_day_func_parsed");				
						json.value(funcText);
						json.key("post_day_func_tree");				
						formula_unified.accept(new FormulaToJsonTreeVisitor(json));		
					}
				} catch(Exception e) {
					log.warn(e);
				}				
			}
			json.endObject();
		}
		json.endObject();  // end sensors
		json.endObject(); // end model
		json.endObject(); // end

	}


}
