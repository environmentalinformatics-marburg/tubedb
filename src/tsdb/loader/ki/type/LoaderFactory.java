package tsdb.loader.ki.type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.StationProperties;

/**
 * Facatory for loaders by logger type name
 * @author woellauer
 *
 */
public class LoaderFactory {	
	private static final Logger log = LogManager.getLogger();
	
	private LoaderFactory(){}
	
	public static AbstractLoader createLoader(String loggerTypeName, String[] input_schema, StationProperties properties, String sourceInfo) {
		switch(loggerTypeName) {
		case "wxt":
			return new Loader_wxt(input_schema, properties, sourceInfo);
		case "pu1":
			return new Loader_pu1(input_schema, properties, sourceInfo);
		case "pu2":
			return new Loader_pu2(input_schema, properties, sourceInfo);
		case "rad":
			return new Loader_rad(input_schema, properties, sourceInfo);
		case "tfi":
			//return new Loader_tfi(input_schema, properties, csvtimeSeries);
			log.warn("don't load generated tfi files");
			return null;
		case "gp1":
			return new Loader_gp1(input_schema, properties, sourceInfo);
		case "rug":
			return new Loader_rug(input_schema, properties, sourceInfo);					
		default:
			log.warn("no loader found");
			return null;
		}
	}

}
