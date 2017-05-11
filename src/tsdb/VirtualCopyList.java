package tsdb;

public class VirtualCopyList {
	public final String[] sources;
	public final String target;
	public VirtualCopyList(String[] sources, String target) {
		this.sources = sources;
		this.target = target;
	}
	public static VirtualCopyList of(String[] sources, String target) {
		return new VirtualCopyList(sources, target);
	}
}