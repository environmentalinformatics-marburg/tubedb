package tsdb.util.processingchain;

/**
 * Simple processing chain entry with a title.
 * immutable
 * @author woellauer
 */
public class ProcessingChainTitle implements ProcessingChainEntry {
	
	public final String title;
	
	public static ProcessingChainTitle createUnknown() {
		return new ProcessingChainTitle("unknown");
	}
	
	public static ProcessingChainTitle of(String title) {
		if(title==null) {
			return createUnknown();
		} else {
			return new ProcessingChainTitle(title);
		}
	}
	
	private ProcessingChainTitle(String title) {
		this.title = title;
	}

	@Override
	public String getProcessingTitle() {
		return title;
	}
	
	@Override
	public String toString() {
		return getProcessingTitle();
	}

}
