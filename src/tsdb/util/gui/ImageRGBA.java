package tsdb.util.gui;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ar.com.hjg.pngj.FilterType;
import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.PngWriter;

/**
 * Base class of image creators.
 * Image data is as an int array with one int per pixel and RGBA-Format (red, green, blue, alpha).
 * @author woellauer
 *
 */
public class ImageRGBA implements PureImage {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();
	
	protected final int width;
	protected final int height;

	protected int[] imageBuffer;

	public ImageRGBA(int width, int height) {
		this.width = width;
		this.height = height;
		this.imageBuffer = new int[width*height];
	}
	
	public ImageRGBA(int width, int height, int[] imageBuffer) {
		this.width = width;
		this.height = height;
		this.imageBuffer = imageBuffer;
	}
	
	/**
	 * get Data reference (no copy) from BufferedImage
	 * Color model needs to be of type int: TYPE_INT_RGB or TYPE_INT_ARGB
	 * @param bufferedImage
	 * @return
	 */
	public static ImageRGBA ofBufferedImage(BufferedImage bufferedImage) {
		WritableRaster raster = bufferedImage.getRaster();
		DataBufferInt dataBuffer = (DataBufferInt) raster.getDataBuffer();
		return new ImageRGBA(bufferedImage.getWidth(), bufferedImage.getHeight(), dataBuffer.getData());
	}

	public int[] getRawArray() {
		return imageBuffer;
	}

	/**
	 * Get JavaFX image 
	 * @return
	 */
	/*public WritableImage getWritableImage() {
		WritableImage writableImage = new WritableImage(width, height);
		writableImage.getPixelWriter().setPixels(0, 0, width, height, WritablePixelFormat.getIntArgbInstance(), imageBuffer, 0, width);
		return writableImage;
	}*/
	
	/**
	 * Get JavaFX image 
	 * @return
	 */
	/*public WritableImage getWritableImage2() { //slow
		BufferedImage bi = getBufferedImage_FAST();
		return SwingFXUtils.toFXImage(bi, null);
	}*/
	
	@Override
	public BufferedImage getBufferedImage() {
		return getBufferedImage_FAST();
	}

	/*public BufferedImage getBufferedImage() {//slow old default (from JavaFX image)
		return SwingFXUtils.fromFXImage(getWritableImage(), null);
	}*/
	
	public BufferedImage getBufferedImage_SLOW() {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		bi.setRGB(0, 0, width, height, imageBuffer, 0, width);
		return bi;
	}
	
	public BufferedImage getBufferedImage_NEW() {//slower than getBufferedImage_FAST()
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = bi.getRaster(); //sun.awt.image.IntegerInterleavedRaster r = null;		
		int[] pixel = new int[1];
		int ic=0;
		for (int y = 0; y<height; y++) {
            for (int x = 0; x<width; x++) {
            	pixel[0] = imageBuffer[ic++];
                raster.setDataElements(x, y, pixel);
            }
        }
		return bi;
	}
	
	public BufferedImage getBufferedImage_FAST() {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = bi.getRaster();
		DataBufferInt dataBuffer = (DataBufferInt) raster.getDataBuffer();		
		System.arraycopy(imageBuffer, 0, dataBuffer.getData(), 0, imageBuffer.length);
		return bi;
	}
	
	/*public BufferedImage getBufferedImage_FAST2() { //uses internal API as fast as getBufferedImage_FAST
	BufferedImage bi = new BufferedImage(screen_width, screen_height, BufferedImage.TYPE_INT_ARGB);
	WritableRaster raster = bi.getRaster();
	System.arraycopy(imageBuffer, 0, ((sun.awt.image.IntegerInterleavedRaster)raster).getDataStorage(), 0, imageBuffer.length);
	return bi;
	}*/
	
	public BufferedImage getBufferedImage_TYPE_INT_RGB_FAST() {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = bi.getRaster();
		DataBufferInt dataBuffer = (DataBufferInt) raster.getDataBuffer();		
		System.arraycopy(imageBuffer, 0, dataBuffer.getData(), 0, imageBuffer.length);
		return bi;
	}
	
	public BufferedImage getBufferedImage_TYPE_3BYTE_BGR_FAST() {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		WritableRaster raster = bi.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		int size = imageBuffer.length;
		int ic=0;
		for (int i = 0; i < size; i++) {
			int pixel = imageBuffer[i];
			data[ic++] = (byte) pixel;
			data[ic++] = (byte) (pixel>>8);
			data[ic++] = (byte) (pixel>>16);
		}
		return bi;
	}	

	private static class ImageLineWrapper implements IImageLine {

		private final int[] data;
		private final int width;
		public int currentLine = 0;

		public ImageLineWrapper(int[] data, int width) {
			this.data = data;
			this.width = width;
		}

		@Override
		public void readFromPngRaw(byte[] raw, int len, int offset, int step) {
			throw new RuntimeException("not implemented");

		}

		@Override
		public void endReadFromPngRaw() {
			throw new RuntimeException("not implemented");

		}

		@Override
		public void writeToPngRaw(byte[] raw) {
			int[] src = data;
			raw[0] = -100; // FilterType.FILTER_UNKNOWN
			int srcPos = currentLine*width;
			int dstPos = 1;

			for(int j=0;j<width;j++) {
				int c = src[srcPos++];
				raw[dstPos++] = (byte) (c>>16);
				raw[dstPos++] = (byte) (c>>8);	
				raw[dstPos++] = (byte) c;					
			}			
		}		
	}

	public void writePngUncompressed(OutputStream out) {
		int cols = width;
		int rows = height;
		int bitdepth = 8;
		boolean alpha = false;
		ImageInfo imgInfo =new ImageInfo(cols, rows, bitdepth, alpha);
		PngWriter pngWriter = new PngWriter(out, imgInfo);
		pngWriter.setCompLevel(0);
		pngWriter.setFilterType(FilterType.FILTER_NONE);
		ImageLineWrapper imageLineWrapper = new ImageLineWrapper(imageBuffer,width);
		for(int i=0;i<rows;i++) {
			imageLineWrapper.currentLine = i;
			pngWriter.writeRow(imageLineWrapper);
		}
		pngWriter.end();
	}
	
	/**
	 * fast good compression
	 * @param out
	 */
	public void writePngCompressed(OutputStream out) {
		writePngCompressed(out, 1);
	}
	
	/**
	 * 
	 * @param out
	 * @param level  few compression 1 to best compression 9
	 */
	public void writePngCompressed(OutputStream out, int level) {
		int cols = width;
		int rows = height;
		int bitdepth = 8;
		boolean alpha = false;
		ImageInfo imgInfo =new ImageInfo(cols, rows, bitdepth, alpha);
		PngWriter pngWriter = new PngWriter(out, imgInfo);
		pngWriter.setCompLevel(level);
		pngWriter.setFilterType(FilterType.FILTER_ADAPTIVE_FAST);
		ImageLineWrapper imageLineWrapper = new ImageLineWrapper(imageBuffer,width);
		for(int i=0;i<rows;i++) {
			imageLineWrapper.currentLine = i;
			pngWriter.writeRow(imageLineWrapper);
		}
		pngWriter.end();
	}

	public void writePngCompressedSlowDefault(OutputStream out) throws IOException {
		BufferedImage bufferedImage = getBufferedImage();
		ImageIO.write(bufferedImage, "png", out);
	}
	
	/**
	 * 
	 * @param out
	 * @param quality 0.0 to 1.0
	 * @throws IOException
	 */
	@Override
	public void writeJpeg(OutputStream out, float quality) throws IOException {
		//BufferedImage.TYPE_3BYTE_BGR is fastest for jpeg compression
		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
		ImageWriteParam iwp = writer.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(quality);
		writer.setOutput(ImageIO.createImageOutputStream(out));
		
		/*BufferedImage bi = new BufferedImage(screen_width, screen_height, BufferedImage.TYPE_3BYTE_BGR); //slow
		bi.setRGB(0, 0, screen_width, screen_height, imageBuffer, 0, screen_width);*/
		//BufferedImage bi = getBufferedImage_TYPE_INT_RGB_FAST(); //bit slower
		BufferedImage bi = getBufferedImage_TYPE_3BYTE_BGR_FAST(); //fastest
		
		IIOImage image = new IIOImage(bi, null, null);
		writer.write(null, image, iwp);
		writer.dispose();
	}

	/**
	 * Clears all pixels to not transparent black.
	 */
	protected void clearImage() {		
		int size = imageBuffer.length;
		int[] data = imageBuffer;
		for (int i = 0; i < size; i++) {
			data[i] = 0xff000000;
		}
	}

	/**
	 * Fills small gabs in image.
	 */
	public void fillPixel() {
		for(int y=1;y<height-1;y++) {
			int y_base = y*width;
			for(int x=1;x<width-1;x++) {
				int base = (int) (y_base+x);
				if(imageBuffer[base] == 0xff000000) {
					int rsum = 0;
					int gsum = 0;
					int bsum = 0;
					int cnt = 0;
					int v = imageBuffer[(y-1)*width+x];
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
			gsum += (v&0x0000ff00)>>8;
		bsum += (v&0x000000ff);
					}
					v = imageBuffer[y*width+(x-1)]; 
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}
					v = imageBuffer[y*width+(x+1)];
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}
					v = imageBuffer[(y+1)*width+x];
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}

					v = imageBuffer[(y-1)*width+(x-1)];
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}
					v = imageBuffer[(y+1)*width+(x-1)]; 
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}
					v = imageBuffer[(y+1)*width+(x+1)];
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}
					v = imageBuffer[(y-1)*width+(x+1)];
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}
					if(cnt>0) {
						imageBuffer[base] = 0xff000000 | ((rsum/cnt)<<16) | ((gsum/cnt)<<8) | (bsum/cnt);
					}
				}
			}
		}

		for(int y=height-2;y>0;y--) {
			int y_base = y*width;
			for(int x=width-2;x>0;x--) {
				int base = (int) (y_base+x);
				if(imageBuffer[base] == 0xff000000) {
					int rsum = 0;
					int gsum = 0;
					int bsum = 0;
					int cnt = 0;
					int v = imageBuffer[(y-1)*width+x];
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}
					v = imageBuffer[y*width+(x-1)]; 
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}
					v = imageBuffer[y*width+(x+1)];
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}
					v = imageBuffer[(y+1)*width+x];
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}

					v = imageBuffer[(y-1)*width+(x-1)];
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}
					v = imageBuffer[(y+1)*width+(x-1)]; 
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}
					v = imageBuffer[(y+1)*width+(x+1)];
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}
					v = imageBuffer[(y-1)*width+(x+1)];
					if(v != 0xff000000) {
						cnt++;
						rsum += (v&0x00ff0000)>>16;
						gsum += (v&0x0000ff00)>>8;
						bsum += (v&0x000000ff);
					}

					if(cnt>0) {
						imageBuffer[base] = 0xff000000 | ((rsum/cnt)<<16) | ((gsum/cnt)<<8) | (bsum/cnt);
					}
				}
			}
		}		
	}
}