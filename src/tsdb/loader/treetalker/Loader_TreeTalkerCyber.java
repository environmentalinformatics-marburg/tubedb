package tsdb.loader.treetalker;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.json.JSONObject;
import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.util.AssumptionCheck;

public class Loader_TreeTalkerCyber {

	private final TsDB tsdb;
	private int offsetMinutes = 0;

	public Loader_TreeTalkerCyber(TsDB tsdb, JSONObject jsonObject) {
		AssumptionCheck.throwNull(tsdb);
		this.tsdb = tsdb;
		
		if(jsonObject != null) {
			String time_offset = jsonObject.optString("time_offset", null);
			if(time_offset != null) {
				Duration duration = Duration.parse(time_offset);
				offsetMinutes = (int) duration.toMinutes();
				Logger.info("time_offset minutes: " + offsetMinutes);
			}
		}		
	}

	public void loadDirectoryRecursive(Path path) {		
		Logger.info("TreeTalkerCyber import Directory "+path);
		try(DirectoryStream<Path> rootStream = Files.newDirectoryStream(path)) {
			for(Path sub:rootStream) {
				if(!Files.isDirectory(sub)) {
					try {
						loadFile(sub);
					} catch (Exception e) {
						e.printStackTrace();
						Logger.error(e+"  in "+sub);
					}
				} else {
					loadDirectoryRecursive(sub);
				}

			}
		} catch (Exception e) {
			Logger.error(e);
		}		
	}

	public void loadFile(Path filename) {
		Logger.info("TreeTalkerCyber import File "+ filename);
		try {
			TreeTalkerCyberTable treeTalkerCyberTable = new TreeTalkerCyberTable(offsetMinutes);
			treeTalkerCyberTable.importFile(tsdb, filename.toFile());
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error(e);
		}
	}
}
