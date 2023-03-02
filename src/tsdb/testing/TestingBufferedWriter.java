package tsdb.testing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.tinylog.Logger;

import tsdb.util.BufferedWriterUnsync;
import tsdb.util.Timer;

public class TestingBufferedWriter {

	public static void main(String[] args) throws IOException {
		
		final String S_1 = "-127.2549000000";
		char[] C_1 = S_1.toCharArray();

		final int REPEAT = 100;
		final int LEN = 100_000_000;
		for(int r = 0; r < REPEAT; r++) {
			Timer.start("BufferedWriter String");
			try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(OutputStream.nullOutputStream(), StandardCharsets.UTF_8))) {
				for(int i = 0; i < LEN; i++) {
					writer.write(S_1);
				}
			}
			Logger.info(Timer.stop("BufferedWriter String"));

			Timer.start("BufferedWriterUnsync String");
			try(BufferedWriterUnsync writer = new BufferedWriterUnsync(new OutputStreamWriter(OutputStream.nullOutputStream(), StandardCharsets.UTF_8))) {
				for(int i = 0; i < LEN; i++) {
					writer.write(S_1);
				}
			}
			Logger.info(Timer.stop("BufferedWriterUnsync String"));
			
			Timer.start("BufferedWriter char[]");
			try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(OutputStream.nullOutputStream(), StandardCharsets.UTF_8))) {
				for(int i = 0; i < LEN; i++) {
					writer.write(C_1);
				}
			}
			Logger.info(Timer.stop("BufferedWriter char[]"));

			Timer.start("BufferedWriterUnsync char[]");
			try(BufferedWriterUnsync writer = new BufferedWriterUnsync(new OutputStreamWriter(OutputStream.nullOutputStream(), StandardCharsets.UTF_8))) {
				for(int i = 0; i < LEN; i++) {
					writer.write(C_1);
				}
			}
			Logger.info(Timer.stop("BufferedWriterUnsync char[]"));
		}

	}

}
