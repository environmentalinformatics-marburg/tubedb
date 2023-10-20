package tsdb.util.yaml;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;

public class YamlTimestampSafeConstructor extends SafeConstructor {
	public YamlTimestampSafeConstructor() {
		super(new LoaderOptions());
		Construct stringConstructor = this.yamlConstructors.get(Tag.STR);
		this.yamlConstructors.put(Tag.TIMESTAMP, stringConstructor);
	}
}