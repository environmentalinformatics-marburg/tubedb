package tsdb.graph.processing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.graph.node.Node;
import tsdb.util.iterator.TsIterator;

public class Statistics {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();
	
	public static class StatisticsData {
		public final long columns;
		public final long cnt[];
		public final double sum[];
		public final double qsum[];
		public final double min[];
		public final double max[];

		public StatisticsData(int columns) {
			this.columns = columns;
			this.cnt = new long[columns];
			this.sum = new double[columns];
			this.qsum = new double[columns];
			this.min = new double[columns];
			this.max = new double[columns];
			for(int i=0;i<columns;i++) {
				cnt[i] = 0;
				sum[i] = 0;
				qsum[i] = 0;
				min[i] = Double.POSITIVE_INFINITY;
				max[i] = Double.NEGATIVE_INFINITY;
			}
		}
		
		public double getAverage(int colIndex) {
			return sum[colIndex]/cnt[colIndex];
		}
		
		public double getVariance(int colIndex) {
			return (1d/(cnt[colIndex]-1d)) * (qsum[colIndex] - ((1d/cnt[colIndex])*(sum[colIndex]*sum[colIndex])));
		}
				
		public double getStandardDeviation(int colIndex) {
			return Math.sqrt((cnt[colIndex]*qsum[colIndex]-sum[colIndex]*sum[colIndex])/(cnt[colIndex]*(cnt[colIndex]-1)));
		}
	}

	private static class AverageProcessor extends StatisticsData {
		public AverageProcessor(int columns) {
			super(columns);
		}

		public void process(float[] data) {
			for(int i=0;i<columns;i++) {
				final double value = data[i];
				if(!Double.isNaN(value)) {
					cnt[i]++;
					sum[i] += value;
					qsum[i] += value*value;
					if(value<min[i]) {
						min[i] = value;
					}
					if(max[i]<value) {
						max[i] = value;
					}
				}
			}
		}

		public void process(TsIterator it) {
			if(it==null||!it.hasNext()) {
				return;
			}
			while(it.hasNext()) {
				float[] data = it.next().data;
				process(data);
			}
		}
	}

	private final Node source;

	public Statistics(Node source) {
		this.source = source;
	}

	public static Statistics create(Node source) {
		return new Statistics(source);

	}

	public StatisticsData get(Long start, Long end) {
		TsIterator it = source.get(start, end);
		if(it==null||!it.hasNext()) {
			return null;
		}
		AverageProcessor averageProcessor = new AverageProcessor(it.getSchema().length);
		averageProcessor.process(it);
		return averageProcessor;
	}

}
