package tsdb.util.processingchain;

import java.util.Arrays;

/**
 * Processing chain with multiple chains as source and one additional entry
 * immutable (Fields should not be changed.)
 * @author woellauer
 */
public class ProcessingChainMultiSources implements ProcessingChain {	
	public final ProcessingChain[] sources;
	public final ProcessingChainEntry entry;
	
	public static ProcessingChainMultiSources of(ProcessingChainSupplier[] sources, ProcessingChainEntry entry) {
		if(sources==null) {
			sources = new ProcessingChainSupplier[0];
		}
		if(entry==null) {
			entry = ProcessingChainEntry.createUnknown();
		}
		ProcessingChain[] chains = Arrays.stream(sources).map(it->it==null?ProcessingChain.createUnknown():it.getProcessingChain()).toArray(ProcessingChain[]::new);
		return of(chains,entry);
	}
	
	public static ProcessingChainMultiSources of(ProcessingChain[] sources, ProcessingChainEntry entry) {
		if(sources==null) {
			sources = new ProcessingChain[0];
		}
		for(int i=0;i<sources.length;i++) {
			if(sources[i]==null) {
				sources[i] = ProcessingChain.createUnknown();
			}
		}
		if(entry==null) {
			entry = ProcessingChainEntry.createUnknown();
		}
		return new ProcessingChainMultiSources(sources,entry);
	}
	
	public static ProcessingChainMultiSources of(ProcessingChainSupplier primarySource, ProcessingChainSupplier[] secondarySources, ProcessingChainEntry entry) {
		if(primarySource==null) {
			primarySource = ProcessingChainSupplier.createUnknown();
		}
		if(secondarySources==null) {
			secondarySources = new ProcessingChainSupplier[0];
		}
		return of(merge(primarySource,secondarySources), entry);
	}
	
	private ProcessingChainMultiSources(ProcessingChain[] sources, ProcessingChainEntry entry) {
		this.sources = sources;
		this.entry = entry;
	}
	
	private static ProcessingChainSupplier[] merge(ProcessingChainSupplier primarySource, ProcessingChainSupplier[] secondarySources) {
		ProcessingChainSupplier[] itSources = new ProcessingChainSupplier[secondarySources.length+1];
		itSources[0] = primarySource;
		for(int i=0;i<secondarySources.length;i++) {
			itSources[i+1] = secondarySources[i];
		}
		return itSources;
	}	

	@Override
	public String getText() {
		String s="(";
		for(ProcessingChain e:sources) {
			s+=e.getText()+"  ";
		}
		s+=") -> "+entry.getProcessingTitle();
		return s;
	}
}
