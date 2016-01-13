package tsdb.graph.node;

/**
 * Interface for generator function for nodes
 * @author woellauer
 *
 */
public interface NodeGen {
	public Node get(String stationID, String[] schema);
}
