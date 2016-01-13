package tsdb.util.gui;


public interface TimeSeriesPainter {
	
	public enum PosHorizontal {LEFT,CENTER,RIGHT};
	public enum PosVerical {TOP,CENTER,BOTTOM};
	
	float getMinX();
	float getMinY();
	float getMaxX();
	float getMaxY();	
	void setColor(int r, int g, int b);
	void setColorTransparent();	
	void drawLine(float x0, float y0, float x1, float y1);
	void fillCircle(float cx, float cy, float r);
	void setColorAxisLine();
	void setColorZeroLine();
	void setColorYScaleLine();
	void setColorYScaleText();
	void drawText(String text, float x, float y, PosHorizontal posHorizontal, PosVerical posVerical);
	void setColorXScaleYearText();
	void setColorXScaleYearLine();
	void setColorXScaleMonthText();
	void setColorXScaleMonthLine();
	void setColorXScaleDayText();
	void setColorXScaleDayLine();
	void setColorXScaleHourLine();
	void setColorXScaleHourText();		
	void fillRect(float xMin, float yMin, float xMax, float yMax);
	void setColorValueLineTemperature();
	void setColorValueLineTemperatureSecondary();
	void setColorConnectLineTemperature();
	void setColorConnectLineTemperatureSecondary();
	void setColorValueLineUnknown();
	void setColorValueLineUnknownSecondary();
	void setColorConnectLineUnknown();
	void setColorConnectLineUnknownSecondary();
	void setColorRectWater();	
	void setColorRectWaterSecondary();
	void setIndexedColor(float value);
	void setIndexedColorRange(float min, float max);
	void setColorScale(String name);
	float[] getIndexColorRange();
	void setFontDefault();
	void setFontSmall();
	void setLineStyleDotted();
	void setLineStyleSolid();
}
