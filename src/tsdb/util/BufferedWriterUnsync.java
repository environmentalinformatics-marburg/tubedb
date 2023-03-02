package tsdb.util;

import java.io.IOException;
import java.io.Writer;

/**
 * 
 * Derived from BufferedWriter without sync locks and close checks
 *
 */
public class BufferedWriterUnsync extends Writer {

	private static final int DEFAULT_BUFFER_SIZE = 8192;

	public char buf[];
	public int pos;

	private Writer out;

	public BufferedWriterUnsync(Writer out) {
		this(out, DEFAULT_BUFFER_SIZE);
	}

	public BufferedWriterUnsync(Writer out, int size) {
		super(out);
		this.out = out;
		buf = new char[size];
		pos = 0;
	}

	public void flushBuffer() throws IOException {
		if (pos == 0) {
			return;
		}
		out.write(buf, 0, pos);
		pos = 0;
	}

	public void write(int c) throws IOException {
		if (pos >= buf.length) {
			flushBuffer();
		}
		buf[pos++] = (char) c;
	}

	public void write(char cbuf[], int off, int len) throws IOException {
		if ((off < 0) || (off > cbuf.length) || (len < 0) ||
				((off + len) > cbuf.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}

		if (len >= buf.length) {
			flushBuffer();
			out.write(cbuf, off, len);
			return;
		}

		int b = off, t = off + len;
		while (b < t) {
			int d = Math.min(buf.length - pos, t - b);
			System.arraycopy(cbuf, b, buf, pos, d);
			b += d;
			pos += d;
			if (pos >= buf.length) {
				flushBuffer();
			}
		}
	}

	public void write(String s, int off, int len) throws IOException {
		int b = off, t = off + len;
		while (b < t) {
			int d = Math.min(buf.length - pos, t - b);
			s.getChars(b, b + d, buf, pos);
			b += d;
			pos += d;
			if (pos >= buf.length) {
				flushBuffer();
			}
		}
	}

	public void newLine() throws IOException {		
		if (pos + 1 >= buf.length) {
			flushBuffer();
		}
		buf[pos++] = '\r';
		buf[pos++] = '\n';
	}

	public void flush() throws IOException {
		flushBuffer();
		out.flush();
	}

	public void close() throws IOException {
		if (out == null) {
			return;
		}
		try (Writer w = out) {
			flushBuffer();
		} finally {
			out = null;
			buf = null;
		}
	}
	
	public void writeChar(char c) throws IOException {
		if (pos >= buf.length) {
			flushBuffer();
		}
		buf[pos++] = (char) c;
	}
}
