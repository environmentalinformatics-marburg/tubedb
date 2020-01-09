package tsdb.testing;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TestingZip {
	
	public static void main(String[] args) throws IOException {
		
		FileOutputStream fileOutputStream = new FileOutputStream("c:/testing/file.zip");
		
		ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
		zipOutputStream.setComment("new comment");
		zipOutputStream.setLevel(9);
		
		ZipEntry zipEntry = new ZipEntry("filename.dat");
		zipOutputStream.putNextEntry(zipEntry);
		PrintStream printStream = new PrintStream(zipOutputStream);
		printStream.print("Hello Hello");
		zipOutputStream.close();
		
	}

}
