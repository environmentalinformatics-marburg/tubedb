package tsdb.util.processingchain;

/**
 * Processing chain with one chain as source and one additional entry
 * immutable
 * @author woellauer
 */
public class ProcessingChainOneSource implements ProcessingChain {	
	public final ProcessingChain source;
	public final ProcessingChainEntry entry;
	
	public static ProcessingChainOneSource of(ProcessingChain source, ProcessingChainEntry entry) {
		if(source==null) {
			source = ProcessingChain.createUnknown();
		}
		if(entry==null) {
			entry = ProcessingChainEntry.createUnknown();
		}
		return new ProcessingChainOneSource(source,entry);
	}
	
	public static ProcessingChainOneSource of(ProcessingChainSupplier source, ProcessingChainEntry entry) {
		ProcessingChain chain = source==null?ProcessingChain.createUnknown():source.getProcessingChain();
		if(entry==null) {
			entry = ProcessingChainEntry.createUnknown();
		}
		return new ProcessingChainOneSource(chain,entry);		
	}
	
	public static ProcessingChainOneSource of(ProcessingChain source, String entry) {
		return of(source,ProcessingChainEntry.of(entry));
	}
	
	public static ProcessingChainOneSource of(ProcessingChainSupplier source, String entry) {
		return of(source,ProcessingChainEntry.of(entry));
	}
	
	
	private ProcessingChainOneSource(ProcessingChain source, ProcessingChainEntry entry) {
		this.source = source;
		this.entry = entry;
	}
	
	@Override
	public String getText() {
		return source.getText()+" -> "+entry.getProcessingTitle();
	}
}
