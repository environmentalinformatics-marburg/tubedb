package tsdb.usecase;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;

public class TestingKiliDirectoryRead {
	
	public static int fileCounter = 0;
	public static int validASCfileCounter = 0;

	public static void main(String[] args) {


		String kili_basepath = "c:/timeseriesdatabase_data_source_structure_kili/";

		loadDirectoryOfAllExploratories_structure_kili(Paths.get(kili_basepath));
		
		System.out.println("fileCounter: "+fileCounter);
		System.out.println("validASCfileCounter: "+validASCfileCounter);

	}

	public static void loadDirectoryOfAllExploratories_structure_kili(Path kiliPath) {
		System.out.println("loadDirectoryOfAllExploratories_structure_kili:\t"+kiliPath);
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(kiliPath);
			for(Path path:stream) {
				DirectoryStream<Path> subStream = Files.newDirectoryStream(path,"ra*");
				for(Path subPath:subStream) {
					loadOneDirectory_structure_kili(subPath);
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public static void loadOneDirectory_structure_kili(Path kiliPath) {
		try {
			if(Files.exists(kiliPath)) {
				TreeMap<String,Path> validFileMap = new TreeMap<String,Path>();
				DirectoryStream<Path> stream = Files.newDirectoryStream(kiliPath);
				System.out.println("*** load directory: "+kiliPath+" ***");
				for(Path path:stream) {
					
					String filename = path.getName(path.getNameCount()-1).toString();
					int ascIndex = filename.indexOf(".asc");
					if(ascIndex!=-1) {
						fileCounter++;
						
						String fileKey = filename.substring(0, ascIndex);
						
						System.out.println("OK "+fileKey+"\t\t\t"+filename);
						
						validFileMap.putIfAbsent(fileKey, path);
						
						
						
						insertOneFile(path);
					} else {
						System.out.println("no ---------------------------- "+filename);
					}					
				}
				
				for(Path ascPath:validFileMap.values()) {
					validASCfileCounter++;
					System.out.println("asc file: "+ascPath);
				}
				
				
			} else {
				System.out.println("directory not found: "+kiliPath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void insertOneFile(Path path) {
		//System.out.println("read: "+path);
	}

}
