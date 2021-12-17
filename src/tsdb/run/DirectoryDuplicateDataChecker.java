package tsdb.run;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.stream.StreamSupport;


import org.tinylog.Logger;

import tsdb.util.Util;

public class DirectoryDuplicateDataChecker {
	

	public static void main(String[] args) throws Exception {
		Logger.info("start...");
		String baseRoot = "C:/timeseriesdatabase_source/be_tsm";
		String compareRoot = "C:/timeseriesdatabase_source/be_tsm_hd";
		String targetRoot = "C:/timeseriesdatabase_source/be_tsm_target";

		Path[] paths = getPaths(Paths.get(compareRoot));

		for(Path subPath:paths) {
			if(Files.isDirectory(subPath)) {
				String compareDir = subPath.toString();
				String dir = subPath.getFileName().toString();
				String baseDir = baseRoot+'/'+dir;
				if(Files.exists(Paths.get(baseDir))) {
					//Logger.info("check "+baseDir+" -> "+compareDir);
					String targetDir = targetRoot+'/'+dir;
					DirectoryDuplicateDataChecker checker = new DirectoryDuplicateDataChecker(baseDir, compareDir, targetDir);
					checker.loadBase();
					checker.compare();
					checker.copy();
				} else {
					Logger.warn("dir does not exist in base: skip "+compareDir);
				}
			} else {
				Logger.warn("not checked: "+subPath);
			}
			System.gc();
		}
		Logger.info("...end");
	}	

	private final String baseDir;
	private final String compareDir;
	private final String targetDir;

	private final HashMap<String,Long> baseMap = new HashMap<String,Long>();
	private final HashMap<String,Path> compareMap = new HashMap<String,Path>();

	public DirectoryDuplicateDataChecker(String baseDir, String compareDir, String targetDir) {
		this.baseDir = baseDir;
		this.compareDir = compareDir;
		this.targetDir = targetDir;
	}

	private static Path[] getPaths(Path root) throws IOException {
		DirectoryStream<Path> dirStream = Files.newDirectoryStream(root);
		Path[] paths = StreamSupport.stream(dirStream.spliterator(), false)
				.sorted()
				.toArray(Path[]::new);
		dirStream.close();
		return paths;
	}

	private void loadBase() throws Exception {
		Path[] paths = getPaths(Paths.get(baseDir));		
		for(Path filename:paths) {
			String md5 = getMD5(filename);
			if(!baseMap.containsKey(md5)) {
				baseMap.put(md5, (long)1);
			} else {
				Long c = baseMap.get(md5);
				baseMap.put(md5,c+1);
				//Logger.warn("duplicate md5 in base "+baseMap.get(md5)+" new "+filename);				
			}
		}
	}

	private static String getMD5(Path filename) throws Exception {
		MessageDigest md5 = MessageDigest.getInstance("md5");
		byte[] bytes = Files.readAllBytes(filename);
		byte[] checksum = md5.digest(bytes);
		return Util.bytesToHex(checksum);
	}

	private void compare() throws Exception {
		Path[] paths = getPaths(Paths.get(compareDir));		
		for(Path filename:paths) {
			String md5 = getMD5(filename);
			if(!baseMap.containsKey(md5)) {
				if(!compareMap.containsKey(md5)) {
					compareMap.put(md5, filename);	
					//String f = filename.getFileName().toString();
					//String targetFile = targetDir+'/'+f;
				} else {
					Logger.info("skip duplicate "+filename);
				}
			}
		}		
	}

	private void copy() throws Exception {
		if(Files.exists(Paths.get(targetDir))) {
			Logger.warn("target dir already exits "+targetDir);
		}

		compareMap.values().stream().sorted().forEach(filename -> {
			Path source = filename;
			String f = filename.getFileName().toString();
			if(f.toLowerCase().endsWith(".csv")) {
				Logger.info("skip csv file "+filename);
			} else {
				if(!f.toLowerCase().endsWith(".dat")) {
					Logger.warn("no .dat file "+filename);
				}
				Path target = Paths.get(targetDir+'/'+"hd_"+f);
				if(Files.exists(target)) {
					Logger.warn("target already exits "+target);
				}
				createPathOfFile(target.toFile());
				Logger.info("copy "+source+" -> "+target);	
				try {
					Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
				} catch (Exception e) {
					throw new RuntimeException("loop",e);
				}
			}
		});

	}

	public static boolean createPathOfFile(File filename) {
		try {
			filename.getParentFile().mkdirs();
			return true;
		} catch(Exception e) {
			Logger.error("path not created: "+filename+"    "+e);
			return false;
		}		
	}
}
