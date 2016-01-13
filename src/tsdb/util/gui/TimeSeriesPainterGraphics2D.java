package tsdb.util.gui;

import static tsdb.util.AssumptionCheck.throwNull;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimeSeriesPainterGraphics2D implements TimeSeriesPainter {

	Graphics2D gc;

	float minX;
	float minY;
	float maxX;
	float maxY;

	Color color_black = new Color(0,0,0);
	Color color_grey = new Color(190,190,190);
	Color color_light_blue = new Color(220,220,255);
	
	Font fontDefault;
	Font fontSmall;

	public TimeSeriesPainterGraphics2D(BufferedImage bufferedImage) {
		this(bufferedImage.createGraphics(),0,0,bufferedImage.getWidth(),bufferedImage.getHeight());
	}

	public TimeSeriesPainterGraphics2D(Graphics2D gc,float minX, float minY, float maxX, float maxY) {
		throwNull(gc);
		gc.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		//gc.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);		
		//gc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		gc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		//gc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		//gc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		this.gc = gc;
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		
		fontDefault = gc.getFont();
		fontSmall = fontDefault.deriveFont(9f);
		
		this.indexedColors = firstIndexedColors;
	}
	
	@Override
	public void setFontDefault() {
		gc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		gc.setFont(fontDefault);
	}
	
	@Override
	public void setFontSmall() {
		gc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		gc.setFont(fontSmall);
	}

	@Override
	public float getMinX() {
		return minX;
	}

	@Override
	public float getMinY() {
		return minY;
	}

	@Override
	public float getMaxX() {
		return maxX;
	}

	@Override
	public float getMaxY() {
		return maxY;
	}

	@Override
	public void setColor(int r, int g, int b) {
		gc.setColor(new Color(r,g,b));

	}
	
	@Override
	public void setColorTransparent() {
		gc.setColor(new Color(255,255,255,255));
		
	}
	
	@Override
	public void setLineStyleDotted() {
		final float pattern[] = { 2.0f,2.0f };
		BasicStroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f);
		gc.setStroke(stroke);
	}
	
	@Override
	public void setLineStyleSolid() {
		BasicStroke stroke = new BasicStroke(1.0f);
		gc.setStroke(stroke);
	}

	@Override
	public void drawLine(float x0, float y0, float x1, float y1) {
		gc.drawLine((int)x0, (int)y0, (int)x1, (int)y1);		
	}
	
	@Override
	public void fillCircle(float cx, float cy, float r) {
		gc.fillOval((int)(cx-r), (int)(cy-r), (int)(r*2), (int)(r*2));
		
	}

	@Override
	public void fillRect(float xMin, float yMin, float xMax, float yMax) {
		gc.fillRect((int)xMin, (int)yMin, (int)(xMax-xMin+1), (int)(yMax-yMin+1));
	}

	@Override
	public void setColorValueLineTemperature() {
		gc.setColor(new Color(220, 0, 0));
	}

	@Override
	public void setColorValueLineTemperatureSecondary() {
		gc.setColor(new Color(156,232,17));
	}

	@Override
	public void setColorConnectLineTemperature() {
		gc.setColor(new Color(220,180,180));		
	}

	@Override
	public void setColorConnectLineTemperatureSecondary() {
		gc.setColor(new Color(132,150,99));		
	}

	@Override
	public void setColorValueLineUnknown() {
		gc.setColor(color_black);
	}

	@Override
	public void setColorValueLineUnknownSecondary() {
		gc.setColor(new Color(156,232,17));
	}

	@Override
	public void setColorConnectLineUnknown() {
		gc.setColor(color_grey);		
	}

	@Override
	public void setColorConnectLineUnknownSecondary() {
		gc.setColor(new Color(132,150,99));		
	}

	@Override
	public void setColorRectWater() {
		gc.setColor(new Color(0, 0, 200));		
	}

	@Override
	public void setColorRectWaterSecondary() {
		gc.setColor(new Color(156,232,17));		
	}

	@Override
	public void setColorAxisLine() {
		gc.setColor(color_grey);		
	}

	@Override
	public void setColorZeroLine() {
		gc.setColor(new Color(120,120,120));		
	}

	@Override
	public void setColorYScaleLine() {
		//gc.setColor(color_light_blue);
		gc.setColor(new Color(220,220,255));
	}

	@Override
	public void setColorYScaleText() {
		gc.setColor(color_black);		
	}

	@Override
	public void drawText(String text, float x, float y, PosHorizontal posHorizontal, PosVerical posVerical) {
		Rectangle2D rect = gc.getFontMetrics().getStringBounds(text, gc);		
		float xPos;
		switch(posHorizontal) {
		case LEFT:
			xPos = (float)(x-rect.getMinX());
			break;
		case CENTER:
			xPos = (float)(x-rect.getMinX()-(rect.getMaxX()-rect.getMinX())/2);
			break;
		case RIGHT:
			xPos = (float)(x-rect.getMaxX());
			break;
		default:
			throw new RuntimeException();				
		}
		float yPos;
		switch(posVerical) {
		case TOP:
			yPos = (float)(y-rect.getMinY());
			break;
		case CENTER:
			yPos = (float)(y-rect.getMinY()-(rect.getMaxY()-rect.getMinY())/2);
			break;
		case BOTTOM:
			yPos = (float)(y-rect.getMaxY());
			break;
		default:
			throw new RuntimeException();		
		}		
		gc.drawString(text, xPos, yPos);		
	}

	//***************** XScale text
	
	@Override
	public void setColorXScaleYearText() {
		gc.setColor(new Color(0,0,0));		
	}
	
	@Override
	public void setColorXScaleMonthText() {
		gc.setColor(new Color(100,100,100));
	}
	
	@Override
	public void setColorXScaleDayText() {
		gc.setColor(new Color(150,150,150));		
	}
	
	@Override
	public void setColorXScaleHourText() {
		gc.setColor(new Color(180,180,180));		
	}
	
	//***************** XScale line
	
	@Override
	public void setColorXScaleYearLine() {
		//gc.setColor(new Color(120,120,170));
		gc.setColor(new Color(100,100,220));
	}	

	@Override
	public void setColorXScaleMonthLine() {
		//gc.setColor(new Color(190,190,220));
		gc.setColor(new Color(190,190,255));
	}
	
	@Override
	public void setColorXScaleDayLine() {
		//gc.setColor(new Color(220,220,255));
		gc.setColor(new Color(220,220,255));
	}	
	
	@Override
	public void setColorXScaleHourLine() {
		//gc.setColor(new Color(240,240,255));
		gc.setColor(new Color(235,235,255));
	}
	
	
	
	private static Map<String,Color[]> colorScaleMap = new ConcurrentHashMap<String,Color[]>();

	

	private float minValue = -10f;
	private float maxValue = 30f;
	private static Color[] firstIndexedColors;
	private Color[] indexedColors;
	static {
		firstIndexedColors = new Color[6*256];
		for(int i=0;i<firstIndexedColors.length;i++) {
			
			float v = ((float)i)/((float)(firstIndexedColors.length-1));
			
			firstIndexedColors[i] = getSpectraColor(v);
			
		}
		colorScaleMap.put("default", firstIndexedColors);
	}
	
	public static void setIndexedColors(String name, Color[] indexedColors) {		
		colorScaleMap.put(name, indexedColors);		
		firstIndexedColors = indexedColors;
		
		/*Color[] c = new Color[indexedColors.length*2];
		for(int i=0;i<indexedColors.length;i++) {
			c[i] = indexedColors[i];
			c[c.length-1-i] = indexedColors[i];
		}
		
		colorScaleMap.put("round_rainbow", c);*/

	}
	
	@Override
	public void setColorScale(String name) {
		Color[] c = colorScaleMap.get(name);
		if(c==null) {
			c = colorScaleMap.get("default");
		}
		if(c!=null) {
			indexedColors = c;
		}		
	}

	private static Color getSpectraColor(float value) {
		
		//float x = 380f+(value*(780f-380f));
		float x = 380f+(value*(645f-380f));
		
		float r=0f;
		float g=0f;
		float b=0f;
		if ((x>=380f)&&(x<=440f)) { 
			r = -1f*(x-440f)/(440f-380f);
			g = 0f;
			b = 1f;
		}
		if ((x>=440f)&&(x<=490f)) {
			r = 0f;
			g = (x-440f)/(490f-440f);
			b = 1f;
		}
		if ((x>=490f)&&(x<=510f)) { 
			r = 0f;
			g = 1f;
			b = -1f*(x-510f)/(510f-490f);
		}
		if ((x>=510f)&&(x<=580f)) { 
			r = (x-510f)/(580f-510f);
			g = 1f;
			b = 0f;
		}
		if ((x>=580f)&&(x<=645f)) {
			r = 1f;
			g = -1f*(x-645f)/(645f-580f);
			b = 0f;
		}
		/*if ((x>=645f)&&(x<=780f)) {
			r = 1f;
			g = 0f;
			b = 0f;
		}*/
		return new Color((int)(r*255f),(int)(g*255f),(int)(b*255f));
	}

	@Override
	public void setIndexedColor(float value) {
		//getSpectraColor(380);
		if(value>minValue) {
			if(value<maxValue) {
				float f = (value-minValue)/(maxValue-minValue);
				gc.setColor(indexedColors[(int) (f*(indexedColors.length-1f))]);
				
			} else {
				gc.setColor(indexedColors[indexedColors.length-1]);
			}
		} else {
			gc.setColor(indexedColors[0]);
		}
	}
	
	@Override
	public void setIndexedColorRange(float min, float max) {
		minValue = min;
		maxValue = max;
	}
	
	@Override
	public float[] getIndexColorRange() {
		return new float[]{minValue,maxValue};
	}

	



}
