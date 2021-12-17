package tsdb.run;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import org.tinylog.Logger;

import tsdb.TsDBFactory;

public class CheckForIncompleteFiles {
	

	public static void main(String[] args) throws IOException {

		Path rootPath = Paths.get(TsDBFactory.SOURCE_BE_TSM_PATH);

		traverseDirectory(rootPath);

	}

	public static void traverseDirectory(Path rootPath) throws IOException {
		//Logger.info(rootPath);

		DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath);

		for(Path path:stream) {
			if(path.toFile().isDirectory()) {
				traverseDirectory(path);
			} else {
				traverseFile(path);
			}
		}
	}
	
	public static void traverseFile(Path path) throws IOException {
		byte[] bytes = Files.readAllBytes(path);
		/*RandomAccessFile raf = new RandomAccessFile(path.toString(),"r");
		FileChannel fileChannel = raf.getChannel();
		fileChannel.read(dsts);
		fileChannel.close();
		raf.close();*/
		
		int null_counter = 0;
		for(int i=bytes.length-1;i>=0;i--) {
			if(bytes[i]==0) {
				null_counter++;
			} else {
				break;
			}
		}
		/*for(byte b:bytes) {
			if(b==0) {
				null_counter++;
			} else {
				null_counter=0;
			}
		}*/
		if(null_counter>56) {
			Logger.info(path+"     "+null_counter+"  of   "+bytes.length+"      "+((bytes.length-null_counter)*100)/bytes.length+"% complete");
		}
	}

}
