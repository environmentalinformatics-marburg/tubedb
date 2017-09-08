package tsdb.util;

import java.util.List;

public class Mutators {
	private Mutators() {}

	public static Mutator getMutator(Computation computation, int targetIndex, int... parameterIndices) {
		switch(parameterIndices.length) {
		case 0: 
			return new MutatorUnchecked(computation, targetIndex);
		case 1: 
			return new MutatorChecked1(computation, targetIndex, parameterIndices[0]);
		case 2: 
			return new MutatorChecked2(computation, targetIndex, parameterIndices[0], parameterIndices[1]);
		case 3: 
			return new MutatorChecked3(computation, targetIndex, parameterIndices[0], parameterIndices[1], parameterIndices[2]);
		case 4: 
			return new MutatorChecked4(computation, targetIndex, parameterIndices[0], parameterIndices[1], parameterIndices[2], parameterIndices[3]);
		default: 
			return new MutatorCheckedVar(computation, targetIndex, parameterIndices);
		}
	}

	public static Mutator bundle(List<Mutator> mutators) {		
		switch(mutators.size()) {
		case 0:
			return null;
		case 1:
			return mutators.get(0);
		case 2:
			return new MultiMutator.MultiMutator2(mutators.get(0), mutators.get(1));
		case 3:
			return new MultiMutator.MultiMutator3(mutators.get(0), mutators.get(1), mutators.get(2));
		case 4:
			return new MultiMutator.MultiMutator4(mutators.get(0), mutators.get(1), mutators.get(2), mutators.get(3));			
		default:
			return new MultiMutator(mutators.toArray(new Mutator[0]));
		}
	}

	private static class MutatorUnchecked extends Mutator {		
		private final Computation computation;
		private final int targetIndex;

		public MutatorUnchecked(Computation computation, int targetIndex) {
			this.computation = computation;
			this.targetIndex = targetIndex;
		}

		@Override
		public void apply(long timestamp, float[] data) {
			data[targetIndex] = computation.eval(timestamp, data);

		}		
	}

	private static class MutatorChecked1 extends Mutator {		
		private final Computation computation;
		private final int targetIndex;
		private final int parameter1Index;

		public MutatorChecked1(Computation computation, int targetIndex, int parameter1Index) {
			this.computation = computation;
			this.targetIndex = targetIndex;
			this.parameter1Index = parameter1Index;
		}

		@Override
		public void apply(long timestamp, float[] data) {
			//System.out.println(computation);
			data[targetIndex] = Float.isFinite(data[parameter1Index]) ? computation.eval(timestamp, data) : Float.NaN;

		}		
	}

	private static class MutatorChecked2 extends Mutator {		
		private final Computation computation;
		private final int targetIndex;
		private final int parameter1Index;
		private final int parameter2Index;

		public MutatorChecked2(Computation computation, int targetIndex, int parameter1Index, int parameter2Index) {
			this.computation = computation;
			this.targetIndex = targetIndex;
			this.parameter1Index = parameter1Index;
			this.parameter2Index = parameter2Index;
		}

		@Override
		public void apply(long timestamp, float[] data) {
			data[targetIndex] = Float.isFinite(data[parameter1Index]) && Float.isFinite(data[parameter2Index]) ? computation.eval(timestamp, data) : Float.NaN;

		}		
	}

	private static class MutatorChecked3 extends Mutator {		
		private final Computation computation;
		private final int targetIndex;
		private final int parameter1Index;
		private final int parameter2Index;
		private final int parameter3Index;

		public MutatorChecked3(Computation computation, int targetIndex, int parameter1Index, int parameter2Index, int parameter3Index) {
			this.computation = computation;
			this.targetIndex = targetIndex;
			this.parameter1Index = parameter1Index;
			this.parameter2Index = parameter2Index;
			this.parameter3Index = parameter3Index;
		}

		@Override
		public void apply(long timestamp, float[] data) {
			data[targetIndex] = Float.isFinite(data[parameter1Index]) && Float.isFinite(data[parameter2Index]) && Float.isFinite(data[parameter3Index]) ? computation.eval(timestamp, data) : Float.NaN;

		}		
	}

	private static class MutatorChecked4 extends Mutator {		
		private final Computation computation;
		private final int targetIndex;
		private final int parameter1Index;
		private final int parameter2Index;
		private final int parameter3Index;
		private final int parameter4Index;

		public MutatorChecked4(Computation computation, int targetIndex, int parameter1Index, int parameter2Index, int parameter3Index, int parameter4Index) {
			this.computation = computation;
			this.targetIndex = targetIndex;
			this.parameter1Index = parameter1Index;
			this.parameter2Index = parameter2Index;
			this.parameter3Index = parameter3Index;
			this.parameter4Index = parameter4Index;
		}

		@Override
		public void apply(long timestamp, float[] data) {
			data[targetIndex] = Float.isFinite(data[parameter1Index]) && Float.isFinite(data[parameter2Index]) && Float.isFinite(data[parameter3Index]) && Float.isFinite(data[parameter4Index]) ? computation.eval(timestamp, data) : Float.NaN;

		}		
	}

	private static class MutatorCheckedVar extends Mutator {		
		private final Computation computation;
		private final int targetIndex;
		private final int[] parameterIndices;

		public MutatorCheckedVar(Computation computation, int targetIndex, int... parameterIndices) {
			this.computation = computation;
			this.targetIndex = targetIndex;
			this.parameterIndices = parameterIndices;
		}

		@Override
		public void apply(long timestamp, float[] data) {
			data[targetIndex] = computation.eval(timestamp, data);
			boolean finite = true;
			for(int parameterIndex:parameterIndices) {
				if(!Float.isFinite(data[parameterIndex])) {
					finite = false;
				}
			}
			data[targetIndex] = finite ? computation.eval(timestamp, data) : Float.NaN;
		}		
	}
}
