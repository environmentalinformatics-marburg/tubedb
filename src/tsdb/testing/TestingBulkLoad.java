package tsdb.testing;

import java.io.File;
import java.util.ArrayList;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun.Tuple2;

public class TestingBulkLoad {

	public static void main(String[] args) {
		
		
		DB db = DBMaker.newFileDB(new File("c:/TestingBulkLoad/"+"cachedb"))
				.closeOnJvmShutdown()
				.make();
		System.out.println("init...");
		final int count = 1000000;
		ArrayList<Tuple2<Long,Double>> list = new ArrayList<Tuple2<Long,Double>>(count);
		for(long i=0;i<count;i++) {
			list.add(new Tuple2<Long, Double>(i, i*10000d+(i%11d)));
		}
		System.out.println("insert...");
		
		db.createTreeMap("newName").pumpPresort(50000000).pumpSource(list.iterator()).makeLongMap();
		
		/*BTreeMap<Long, Double> map = db.createTreeMap("newName").make();
		for(Tuple2<Long, Double> t:list) {
			map.put(t.a, t.b);
		}*/
		
		System.out.println("commit...");
		db.commit();
		System.out.println("compact...");
		db.compact();		
		System.out.println("...end");
		db.close();
		
		db = DBMaker.newFileDB(new File("c:/TestingBulkLoad/"+"cachedb"))
				.closeOnJvmShutdown()
				.make();
		
		BTreeMap<Object, Object> map2 = db.getTreeMap("newName");
		System.out.println(map2.size());
		
		db.close();
		
		

	}

}
