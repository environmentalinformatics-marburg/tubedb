package tsdb.util.processingchain;

/**
 * Base interface and factory for processing chains 
 * @author woellauer
 *
 */
@FunctionalInterface
public interface ProcessingChain {	
	public String getText();
	
	public static ProcessingChainSource createUnknown() {		
		return ProcessingChainSource.createUnknown();
	}
	
	public static ProcessingChainSource of(ProcessingChainEntry entry) {
		return ProcessingChainSource.of(entry);
	}
	
	public static ProcessingChainOneSource of(ProcessingChain source, ProcessingChainEntry entry) {
		return ProcessingChainOneSource.of(source, entry);
	}
	
	public static ProcessingChainOneSource of(ProcessingChainSupplier source, ProcessingChainEntry entry) {
		return ProcessingChainOneSource.of(source, entry);
	}
	
	public static ProcessingChainOneSource of(ProcessingChain source, String entry) {
		return ProcessingChainOneSource.of(source, entry);
	}
	
	public static ProcessingChainOneSource of(ProcessingChainSupplier source, String entry) {
		return ProcessingChainOneSource.of(source, entry);
	}
	
	public static ProcessingChainMultiSources of(ProcessingChainSupplier[] sources, ProcessingChainEntry entry) {
		return ProcessingChainMultiSources.of(sources, entry);
	}
}
