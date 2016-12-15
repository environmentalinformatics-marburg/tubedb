package tsdb.util.yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class YamlMap {
	
	private Map<String, Object> map;
	
	public YamlMap(Map<String, Object> map) {
		this.map = map;
	}
	
	@SuppressWarnings("unchecked")
	public static YamlMap ofObject(Object map) {
		return new YamlMap((Map<String, Object>) map);
	}
	
	public static final YamlMap EMPTY_MAP = new YamlMap(new HashMap<>());
	
	public Object getObject(String name) {
		Object o = map.get(name);
		if(o==null) {
			throw new RuntimeException("element not found "+name);
		}
		return o;
	}
	
	public Object optObject(String name, Object def) {
		if(contains(name)) {
			return getObject(name);
		}
		return def;
	}	
	
	
	public boolean contains(String name) {
		return map.containsKey(name);
	}
		
	public String getString(String name) {
		Object o = getObject(name);
		return o.toString();
	}
	
	@SuppressWarnings("unchecked")
	YamlMap getMap(String name) {
		Object o = getObject(name);
		if(o instanceof Map) {
			return new YamlMap((Map<String, Object>) o);
		}
		throw new RuntimeException("element is not a map "+name);
	}
	
	<T> T funMap(String name, Function<YamlMap, T> fun, Supplier<T> optFun) {
		if(contains(name)) {
			return fun.apply(getMap(name));
		}
		return optFun.get();
	}

	@SuppressWarnings("unchecked")
	public YamlList getList(String name) {
		Object o = getObject(name);
		if(o instanceof List) {
			return new YamlList((List<Object>) o);
		}
		//throw new RuntimeException("element is not a map "+name);
		ArrayList<Object> list = new ArrayList<Object>(1);
		list.add(o);
		return new YamlList(list);
	}
	
	public YamlList optList(String name) {
		if(contains(name)) {
			return getList(name);
		}
		return new YamlList(new ArrayList<Object>(0));
	}
	
	public String optString(String name) {
		return optString(name, null);
	}

	public String optString(String name, String def) {
		if(contains(name)) {
			return getString(name);
		}
		return def;
	}
	
	public Number getNumber(String name) {
		Object o = getObject(name);
		if(o instanceof Number) {
			return (Number) o;
		}
		throw new RuntimeException("element is not a number "+name);
	}
	
	public int getInt(String name) {
		return getNumber(name).intValue();
	}

	public int optInt(String name, int def) {
		if(contains(name)) {
			return getInt(name);
		}
		return def;
	}
	
	public double getDouble(String name) {
		return getNumber(name).doubleValue();
	}

	public double optDouble(String name) {
		if(contains(name)) {
			return getDouble(name);
		}
		return Double.NaN;
	}

	public void funString(String name, Consumer<String> fun) {
		if(contains(name)) {
			fun.accept(getString(name));
		}		
	}
	
	@Override
	public String toString() {
		return map.toString();
	}


}
