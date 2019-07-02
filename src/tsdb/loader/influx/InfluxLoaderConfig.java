package tsdb.loader.influx;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import tsdb.util.yaml.YamlMap;


public class InfluxLoaderConfig {
	private static final Logger log = LogManager.getLogger();

	public final String url;
	public final String user;
	public final String password;
	public final String database;
	public final Sensor[] sensors;

	public static class Sensor {
		public final String loggerName;
		public final String srcName;
		public final String dstName;

		public Sensor(String loggerName, String srcName, String dstName) {
			this.loggerName = loggerName;
			this.srcName = srcName;
			this.dstName = dstName;
		}

		public static Sensor ofYaml(YamlMap yamlMap) {
			String loggerName = yamlMap.getString("logger_name");
			String srcName = yamlMap.getString("src_name");
			String dstName = yamlMap.getString("dst_name");
			return new Sensor(loggerName, srcName, dstName);
		}
	}

	public InfluxLoaderConfig(Path path) {
		try {
			InputStream in = new FileInputStream(path.toFile());
			YamlMap configMap = YamlMap.ofObject(new Yaml().load(in));
			log.info(configMap == null);
			url = configMap.getString("url");
			user = configMap.getString("user");
			password = configMap.getString("password");
			database = configMap.getString("database");			
			sensors = configMap.getList("sensors").asMaps().stream().map(Sensor::ofYaml).toArray(Sensor[]::new);						
		} catch (Exception e) {
			log.error("config YAML file error in "+path+"  "+e);
			throw new RuntimeException(e);
		}
	}

}
