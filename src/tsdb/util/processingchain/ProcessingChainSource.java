package tsdb.util.processingchain;

/**
 * A processing chain with one entry as source
 * immutable
 * @author woellauer
 */
public class ProcessingChainSource implements ProcessingChain  {
	public final ProcessingChainEntry entry;
	
	public static ProcessingChainSource createUnknown() {		
		return new ProcessingChainSource(ProcessingChainEntry.createUnknown());
	}
	
	public static ProcessingChainSource of(ProcessingChainEntry entry) {
		if(entry==null) {
			return ProcessingChainSource.createUnknown();
		} else {
			return new ProcessingChainSource(entry);
		}
	}
	
	private ProcessingChainSource(ProcessingChainEntry entry) {
		this.entry = entry;
	}
	
	@Override
	public String getText() {
		return "|-"+entry.getProcessingTitle();
	}
}
