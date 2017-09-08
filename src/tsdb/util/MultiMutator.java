package tsdb.util;

public class MultiMutator extends Mutator {

	private final Mutator[] mutators;

	public MultiMutator(Mutator[] mutators) {
		this.mutators = mutators;
	}

	@Override
	public void apply(long timestamp, float[] data) {
		for(Mutator mutator:mutators) {
			mutator.apply(timestamp, data);
		}
	}

	public static class MultiMutator2 extends Mutator {

		private final Mutator mutator1;
		private final Mutator mutator2;

		public MultiMutator2(Mutator mutator1, Mutator mutator2) {
			this.mutator1 = mutator1;
			this.mutator2 = mutator2;
		}

		@Override
		public void apply(long timestamp, float[] data) {
			mutator1.apply(timestamp, data);
			mutator2.apply(timestamp, data);
		}
	}

	public static class MultiMutator3 extends Mutator {

		private final Mutator mutator1;
		private final Mutator mutator2;
		private final Mutator mutator3;

		public MultiMutator3(Mutator mutator1, Mutator mutator2, Mutator mutator3) {
			this.mutator1 = mutator1;
			this.mutator2 = mutator2;
			this.mutator3 = mutator3;
		}

		@Override
		public void apply(long timestamp, float[] data) {
			mutator1.apply(timestamp, data);
			mutator2.apply(timestamp, data);
			mutator3.apply(timestamp, data);
		}
	}

	public static class MultiMutator4 extends Mutator {

		private final Mutator mutator1;
		private final Mutator mutator2;
		private final Mutator mutator3;
		private final Mutator mutator4;

		public MultiMutator4(Mutator mutator1, Mutator mutator2, Mutator mutator3, Mutator mutator4) {
			this.mutator1 = mutator1;
			this.mutator2 = mutator2;
			this.mutator3 = mutator3;
			this.mutator4 = mutator4;
		}

		@Override
		public void apply(long timestamp, float[] data) {
			mutator1.apply(timestamp, data);
			mutator2.apply(timestamp, data);
			mutator3.apply(timestamp, data);
			mutator4.apply(timestamp, data);
		}
	}

}
