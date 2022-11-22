package tsdb.usecase;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.SortedMap;

public class TestingCharset {

	public static void main(String[] args) {


		SortedMap<String, Charset> x = Charset.availableCharsets();
		for(String k:x.keySet()) {
			System.out.println(k);
		}
		System.out.println();
		System.out.println("default: " + Charset.defaultCharset());
		System.out.println("UTF-8: " + StandardCharsets.UTF_8);
		System.out.println("windows-1252: " + Charset.forName("windows-1252"));
	}

}
