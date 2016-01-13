package tsdb.util;

import java.io.Serializable;

/**
 * Entry of a time series with timestamp, data values, quality flags and counters and interpolation flag.
 * immutable (Field values should not be changed.)
 * @author woellauer
 */
public class TsEntry implements Serializable {
	private static final long serialVersionUID = -4568612556987905958L;
	public final long timestamp;	
	public final float[] data;
	public final DataQuality[] qualityFlag; // maybe null
	public final int[][] qualityCounter; // maybe null
	public final boolean[] interpolated; // maybe null

	public static TsEntry of(long timestamp, float ... values) {
		return new TsEntry(timestamp, values);
	}

	public TsEntry(long timestamp, float[] data, DataQuality[] qualityFlag,int[][] qualityCounter) {
		this(timestamp, data, qualityFlag, qualityCounter, null);
	}

	public TsEntry(long timestamp, float[] data, DataQuality[] qualityFlag,int[][] qualityCounter,boolean[] interpolated) {
		this.timestamp = timestamp;
		this.data = data;
		this.qualityFlag = qualityFlag;
		this.qualityCounter = qualityCounter;
		this.interpolated = interpolated;
	}

	public TsEntry(long timestamp,float[] data) {
		this(timestamp, data, null, null, null);
	}

	public TsEntry(long timestamp, float[] data, DataQuality[] qualityFlag) {
		this(timestamp, data, qualityFlag, null, null);
	}

	public TsEntry(long timestamp, Pair<float[],DataQuality[]> dataPair) {
		this(timestamp,dataPair.a,dataPair.b);
	}

	public TsEntry(long timestamp, Void unused, Pair<float[],int[][]> dataPair) {		
		this(timestamp,dataPair.a, null,dataPair.b, null);
		if(unused!=null) {
			throw new RuntimeException();
		}
	}

	public static TsEntry createNaN(long timestamp, int columnCount) {
		return new TsEntry(timestamp, createNanData(columnCount), createNaQuality(columnCount));
	}

	public static float[] createNanData(int columnCount) {
		float[] data = new float[columnCount];
		for(int i=0;i<columnCount;i++) {
			data[i] = Float.NaN;
		}
		return data;
	}

	public static DataQuality[] createNaQuality(int columnCount) {
		DataQuality[] qualityFlag = new DataQuality[columnCount];
		for(int i=0;i<columnCount;i++) {
			qualityFlag[i] = DataQuality.Na;
		}
		return qualityFlag;
	}

	@Override
	public String toString() {
		return timestamp+"|"+TimeUtil.oleMinutesToLocalDateTime(timestamp)+" "+Util.arrayToString(data)+/*" "+qualityFlagToString()+" "+interpolatedFlagToString()+*/" "+qualityCountersToString();
	}

	/*public String qualityFlagToString() {
		if(qualityFlag==null) {
			return "q-";
		} else {
			String s="q";
			for(int qIndex=0; qIndex<qualityFlag.length; qIndex++) {
				switch(qualityFlag[qIndex]) {
				case Na:
					s+='n';
					break;
				case NO:
					s+='0';
					break;
				case PHYSICAL:
					s+='1';
					break;
				case STEP:
					s+='2';
					break;
				case EMPIRICAL:
					s+='3';
					break;
				default:
					s+='?';

				}
			}
			return s;
		}
	}*/

	/*private String interpolatedFlagToString() {
		if(interpolated==null) {
			return "i-";
		} else {
			String s="i";
			for(int qIndex=0; qIndex<interpolated.length; qIndex++) {
				s+=interpolated[qIndex]?'y':"n";
			}
			return s;			
		}
	}*/

	public String qualityCountersToString() {
		String s = "";	
		if(qualityCounter==null) {
			if(interpolated==null) {
				for(int i=0;i<data.length;i++) {
					if(i>0) {
						s += "_";
					}
					s+=Float.isNaN(data[i])?/*"c0"*/"":"c1";
				}
			} else {
				for(int i=0;i<data.length;i++) {
					if(i>0) {
						s += "_";
					}
					s+="c1"+(interpolated[i]?"i1":/*"i0"*/"");
				}				
			}
		} else {
			for(int i=0;i<qualityCounter.length;i++) {
				if(i>0) {
					s += "_";
				}
				for(int q=0;q<qualityCounter[i].length;q++) {
					if(qualityCounter[i][q]>0) {
						switch(q) {
						case 0:
							s+='c';
							break;
						case 1:
							s+='i';
							break;
						default:
							s+='?';
						}
						s += qualityCounter[i][q];
					}
				}

			}			
		}
		return s;
	}
}