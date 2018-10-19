package tsdb.iterator;

import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.MoveIterator;
import tsdb.util.iterator.TsEnumerator;
import tsdb.util.iterator.TsIterator;
import tsdb.util.iterator.TsIteratorEnumerator;

public class ZipIterator extends MoveIterator {
	
	private TsEnumerator ita;
	private TsEnumerator itb;
	private int alen;
	private int blen;
	private int len;
	
	public static TsSchema createSchema(TsIterator ita, TsIterator itb) {
		TsSchema sa = ita.getSchema();
		TsSchema sb = itb.getSchema();
		String[] names = new String[sa.length + sb.length];
		System.arraycopy(sa.names, 0, names, 0, sa.length);
		System.arraycopy(sb.names, 0, names, sa.length, sb.length);
		Aggregation aggregation = sa.aggregation;
		int timeStep = sa.timeStep;
		boolean isContinuous = sa.isContinuous;
		boolean hasQualityFlags = sa.hasQualityFlags;
		boolean hasInterpolatedFlags = false;
		boolean hasQualityCounters = false;
		return new TsSchema(names, aggregation, timeStep, isContinuous, hasQualityFlags, hasInterpolatedFlags, hasQualityCounters);
	}

	public ZipIterator(TsIterator ita, TsIterator itb) {
		super(createSchema(ita,itb));
		this.ita = TsIteratorEnumerator.of(ita);
		this.itb = TsIteratorEnumerator.of(itb);
		this.alen = ita.getSchema().length;
		this.blen = itb.getSchema().length;
		len = alen + blen;
		this.ita.moveNext();
		this.itb.moveNext();
	}

	@Override
	protected TsEntry getNext() {
		TsEntry ca = ita.current();
		TsEntry cb = itb.current();
		if(ca == null && cb == null) {
			return null;
		}
		float[] data = new float[len];
		DataQuality[] qf = new DataQuality[len];
		long timestamp = cb == null ? ca.timestamp : (ca == null ? cb.timestamp : Math.min(ca.timestamp, cb.timestamp));
		if(ca != null && ca.timestamp == timestamp) {
			System.arraycopy(ca.data, 0, data, 0, alen);
			if(ca.qualityFlag != null) {
				System.arraycopy(ca.qualityFlag, 0, qf, 0, alen);
				for (int i = blen; i < len; i++) {
					qf[i] = DataQuality.Na;
				}
			} else {
				for (int i = 0; i < len; i++) {
					qf[i] = DataQuality.Na;
				}
			}
			ita.moveNext();
		}
		if(cb != null && cb.timestamp == timestamp) {
			System.arraycopy(cb.data, 0, data, alen, blen);
			if(cb.qualityFlag != null) {
				for (int i = 0; i < alen; i++) {
					qf[i] = DataQuality.Na;
				}
				System.arraycopy(cb.qualityFlag, 0, qf, alen, blen);
			} else {
				for (int i = 0; i < len; i++) {
					qf[i] = DataQuality.Na;
				}
			}
			itb.moveNext();
		}
		return new TsEntry(timestamp, data, qf, null);
	}

}
