package tsdb.testing;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.TreeMap;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class TestingYamlWriter {

	public static void main(String[] args) throws IOException {


		TreeMap<String,Object> map = new TreeMap<String,Object>();
		map.put("creation", "now");
		map.put("quality checks", 2);
		map.put("list",Arrays.asList(1,2,3,4,5,6,7,8,9));
		map.put("text", "long     text   long     text   long     text   long     text   long     text   long     text   long     text   long     text   long     text   long     text   long     text");


		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		OutputStreamWriter writer = new OutputStreamWriter(System.out);
		yaml.dump(map, writer);
		writer.flush();
	}

}
