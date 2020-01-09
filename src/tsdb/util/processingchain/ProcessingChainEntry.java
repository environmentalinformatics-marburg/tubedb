package tsdb.util.processingchain;

/**
 * Base interface and factory of processing chain entries
 * @author woellauer
 *
 */
public interface ProcessingChainEntry {	
	/**
	 * get title of this entry
	 * @return
	 */
	String getProcessingTitle();
	
	public static ProcessingChainTitle createUnknown() {
		return ProcessingChainTitle.createUnknown();
	}
	
	public static ProcessingChainTitle of(String title) {
		return ProcessingChainTitle.of(title);
	}	
}
