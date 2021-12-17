package tsdb.graph.processing;

import java.util.Arrays;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import org.tinylog.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.component.Sensor;
import tsdb.component.iterator.FillIterator;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.ContinuousGen;
import tsdb.iterator.InterpolationAverageLinearIterator;
import tsdb.util.AggregationInterval;
import tsdb.util.Pair;
import tsdb.util.TimeUtil;
import tsdb.util.Util;
import tsdb.util.iterator.TsIterator;

public class InterpolatedAverageLinear extends Continuous.Abstract {
	

	private static final int MAX_TRAINING_PLOT_COUNT = 15;
	private static final int MIN_TRAINING_VALUE_COUNT_HOUR = 4*7*24; // four weeks with one hour time interval
	private static final int MIN_TRAINING_VALUE_COUNT_DAY = 4*7; // four weeks with one day time interval

	private final Continuous source;
	private final Continuous trainingTarget;
	private Continuous[] trainingSources;
	private final String[] interpolationSchema;
	private final int MIN_TRAINING_VALUE_COUNT;
	private final double[] maxMSEs;

	public InterpolatedAverageLinear(TsDB tsdb, Continuous source, Continuous trainingTarget, Continuous[] trainingSources, String[] interpolationSchema, AggregationInterval sourceAgg, double[] maxMSEs) {
		super(tsdb);
		this.source = source;
		this.trainingTarget = trainingTarget;
		this.trainingSources = trainingSources;
		this.interpolationSchema = interpolationSchema;

		switch(sourceAgg) {
		case HOUR:
			MIN_TRAINING_VALUE_COUNT = MIN_TRAINING_VALUE_COUNT_HOUR;
			break;
		case DAY:
			MIN_TRAINING_VALUE_COUNT = MIN_TRAINING_VALUE_COUNT_DAY;
			break;
		default:
			throw new RuntimeException("unknown aggregation for interpolation "+sourceAgg);
		}

		this.maxMSEs = maxMSEs;
	}

	public static Continuous of(TsDB tsdb, String plotID, String[] querySchema, ContinuousGen continuousGen, AggregationInterval sourceAgg) {
		Continuous source = continuousGen.get(plotID, querySchema);

		String[] iSchema = Arrays.stream(querySchema)
				.filter(sensorName -> tsdb.getSensor(sensorName).useInterpolation)
				.toArray(String[]::new);

		if(iSchema.length==0) {
			Logger.info("no interpolatable sensors for "+plotID+"   "+Arrays.toString(querySchema));
			return source;
		}

		Continuous trainingTarget = continuousGen.get(plotID, iSchema);
		String[] interpolationSchema = trainingTarget.getSchema();

		Continuous[] trainingSources = source.getSourcePlot().getNearestPlots()
				.limit(MAX_TRAINING_PLOT_COUNT)
				.map(p->{
					String[] validSchema = p.getValidSchemaEntriesWithVirtualSensors(interpolationSchema);
					if(validSchema.length==0) {
						return null;
					}
					Continuous node = continuousGen.get(p.getPlotID(), validSchema);
					if(interpolationSchema.length!=validSchema.length) {
						node = Projected.of(node, interpolationSchema);
					}
					return node;
				})
				.filter(Util::notNull)
				.toArray(Continuous[]::new);

		if(trainingSources.length==0) {
			Logger.info("no interpolation sources for "+plotID+"   "+Arrays.toString(querySchema));
			return source;
		}

		double[] maxMSEs = tsdb.getSensorStream(interpolationSchema).mapToDouble(Sensor::getMaxInterpolationMSE).toArray();

		return new InterpolatedAverageLinear(tsdb, source, trainingTarget, trainingSources, interpolationSchema, sourceAgg, maxMSEs);
	}

	@Override
	public TsIterator getExactly(long start, long end) {
		Logger.trace("lin get "+TimeUtil.oleMinutesToText(start, end));
		long[] trainingInterval = trainingTarget.getTimestampBaseInterval();
		if(trainingInterval==null) {
			Logger.info("no data in "+trainingTarget.getSourceName());
			return null;
		}
		long trainingStart = trainingInterval[0];
		long trainingEnd = trainingInterval[1];

		TsIterator trainingTargetIterator = new FillIterator(trainingTarget.getExactly(trainingStart, trainingEnd));
		if(TsIterator.isNotLive(trainingTargetIterator)) {
			return null;
		}



		@SuppressWarnings("unchecked")
		Pair<Continuous,TsIterator>[] trainingSourcePairs = Arrays.stream(trainingSources)
		.map(s->Pair.of(s,new FillIterator(s.getExactly(trainingStart, trainingEnd))))
		.filter(p->TsIterator.isLive(p.b))
		.toArray(Pair[]::new);

		trainingSources = Arrays.stream(trainingSourcePairs).map(Pair::projA).toArray(Continuous[]::new);
		TsIterator[] trainingIterators = Arrays.stream(trainingSourcePairs).map(Pair::projB).toArray(TsIterator[]::new);

		SimpleRegression[][] simpleRegressions = new SimpleRegression[trainingIterators.length][];
		Arrays.setAll(simpleRegressions, i->{			
			SimpleRegression[] row = new SimpleRegression[interpolationSchema.length];
			Arrays.setAll(row, j->new SimpleRegression());
			return row;
		});

		while(trainingTargetIterator.hasNext()) {
			float[] sourceData = trainingTargetIterator.next().data;
			for(int trainingIndex=0;trainingIndex<trainingIterators.length;trainingIndex++) {
				float[] trainingData = trainingIterators[trainingIndex].next().data;
				for(int column=0;column<interpolationSchema.length;column++) {
					if(!Float.isNaN(trainingData[column]) && !Float.isNaN(sourceData[column])) {
						simpleRegressions[trainingIndex][column].addData(trainingData[column], sourceData[column]);
					}
				}
			}
		}

		double[][] intercepts = new double[trainingIterators.length][interpolationSchema.length];
		double[][] slopes = new double[trainingIterators.length][interpolationSchema.length];
		double[][] weights = new double[trainingIterators.length][interpolationSchema.length];

		for(int trainingIndex=0;trainingIndex<trainingIterators.length;trainingIndex++) {
			SimpleRegression[] regs = simpleRegressions[trainingIndex];
			for(int column=0;column<interpolationSchema.length;column++) {
				SimpleRegression reg = regs[column];
				final double MAX_MSE = maxMSEs[column];
				double mse = reg.getMeanSquareError();
				if(reg.getN()<MIN_TRAINING_VALUE_COUNT || MAX_MSE<mse) {
					intercepts[trainingIndex][column] = Double.NaN;
					slopes[trainingIndex][column] = Double.NaN;
					weights[trainingIndex][column] = Double.NaN;
				} else {
					intercepts[trainingIndex][column] = reg.getIntercept();
					slopes[trainingIndex][column] = reg.getSlope();
					weights[trainingIndex][column] = mse;
					//Logger.info("linear regression "+reg.getN()+"  "+reg.getIntercept()+" "+reg.getSlope()+" "+reg.getMeanSquareError());
				}
			}
		}

		for(int column=0;column<interpolationSchema.length;column++) {
			double min_mse = Double.POSITIVE_INFINITY;
			for(int trainingIndex=0;trainingIndex<trainingIterators.length;trainingIndex++) {
				double mse = weights[trainingIndex][column];
				if(Double.isFinite(mse)&&mse<min_mse) {
					min_mse = mse;
				}
			}
			for(int trainingIndex=0;trainingIndex<trainingIterators.length;trainingIndex++) {
				double mse = weights[trainingIndex][column];
				weights[trainingIndex][column] = Math.pow(min_mse/mse,5);
				//Logger.info("w "+mse+" -> "+weights[trainingIndex][column]);
			}
		}


		TsIterator sourceIterator = new FillIterator(source.getExactly(start, end));
		TsIterator[] interpolationIterators = Arrays.stream(trainingSources).map(s->new FillIterator(s.getExactly(start, end))).toArray(TsIterator[]::new);




		/*TsIterator it = new TsIterator(sourceIterator.getSchema()) {

			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return it.hasNext();
			}

			@Override
			public TsEntry next() {
				// TODO Auto-generated method stub
				return null;
			}

		};*/

		/*if(sourceIterator==null) {
			Logger.error("no interpolation source");
			//return null;
		}*/

		if(interpolationIterators[0]==null) {
			Logger.error("no interpolation training sources");
			//return null;
		}

		InterpolationAverageLinearIterator it = new InterpolationAverageLinearIterator(sourceIterator, interpolationIterators, intercepts, slopes, interpolationSchema, weights);
		return it; 
	}

	@Override
	public TsIterator get(Long start, Long end) {
		if(start==null || end==null) {
			long[] interval = source.getTimestampBaseInterval();
			if(start==null) {
				start = interval[0];
			}
			if(end==null) {
				end = interval[1];
			}
		}		
		return getExactly(start, end);
	}

	@Override
	public Station getSourceStation() {
		return source.getSourceStation();
	}

	@Override
	public VirtualPlot getSourceVirtualPlot() {
		return source.getSourceVirtualPlot();
	}

	@Override
	public long[] getTimestampInterval() {
		return source.getTimestampInterval();
	}

	@Override
	public boolean isConstantTimestep() {
		return source.isConstantTimestep();
	}

	@Override
	public String[] getSchema() {
		return source.getSchema();
	}
}
