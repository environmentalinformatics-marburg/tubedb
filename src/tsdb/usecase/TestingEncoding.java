package tsdb.usecase;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;


import org.tinylog.Logger;

import tsdb.util.Timer;

public class TestingEncoding {
	

	static int REPEATES = 100;
	static int LOOPS = 10_000_000;


	public static void main(String[] args) throws IOException {
		Logger.info(Charset.defaultCharset());

		for(String cs:Charset.availableCharsets().keySet()) {
			Logger.info(cs);
		}

		String[] charsetNames = new String[]{"windows-1252","UTF-8","US-ASCII","ISO-8859-1"/*,"UTF8OutputStreamWriter"*/};

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for(int repeat=0;repeat<LOOPS;repeat++) {
			for(String charsetName:charsetNames) {
				out.reset();
				Writer writer;
				/*if(charsetName.equals("UTF8OutputStreamWriter")) {
					writer = new UTF8OutputStreamWriter(out);
				} else {*/
				Charset charset = Charset.forName(charsetName);
				writer = new OutputStreamWriter(out, charset);
				/*}*/				
				BufferedWriter bufferedWriter = new BufferedWriter(writer);
				Timer.start(charsetName);
				for(int loop=0;loop<LOOPS;loop++) {
					bufferedWriter.write(" "+loop+" ");
				}
				bufferedWriter.flush();
				writer.flush();
				Logger.info(Timer.stop(charsetName));
			}
		}
	}

}