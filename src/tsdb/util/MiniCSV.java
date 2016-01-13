package tsdb.util;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Locale;

public class MiniCSV {

	private final PrintStream out;
	private boolean rowFinished;
	private boolean rowBegun;

	public MiniCSV(String filename) throws FileNotFoundException {
		this.out = new PrintStream(new BufferedOutputStream(new FileOutputStream(filename)),false);
		this.rowFinished = false;
		this.rowBegun = false;
	}

	public void close() {
		out.close();
	}

	public void writeString(String s) {
		if(rowFinished) {
			if(rowBegun) {
				out.print('\n');
				rowBegun = false;
			}
			rowFinished = false;
		}
		if(rowBegun) {
			out.print(',');
		}
		out.print(s);
		rowBegun = true;
	}

	public void writeDouble(double v) {
		if(rowFinished) {
			if(rowBegun) {
				out.print('\n');
				rowBegun = false;
			}
			rowFinished = false;
		}
		if(rowBegun) {
			out.print(',');
		}
		out.format(Locale.ENGLISH,"%3.3f", v);
		rowBegun = true;
	}

	public void writeLong(long n) {
		if(rowFinished) {
			if(rowBegun) {
				out.print('\n');
				rowBegun = false;
			}
			rowFinished = false;
		}
		if(rowBegun) {
			out.print(',');
		}
		out.print(n);
		rowBegun = true;
	}

	public void finishRow() {
		rowFinished = true;
	}
}