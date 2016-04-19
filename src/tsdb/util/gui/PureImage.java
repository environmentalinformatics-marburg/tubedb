package tsdb.util.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

public interface PureImage {
	
	BufferedImage getBufferedImage();
	void writeJpeg(OutputStream out, float quality) throws IOException;

}
