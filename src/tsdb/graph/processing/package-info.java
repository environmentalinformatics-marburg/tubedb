/**
 * Provides node implementations to build processing graphs.
 * <p>
 * Typically nodes use iterators for processing. It's preferred to use nodes that internally manage iterators to process time series data instead
 * of directly using iterators.
 */
package tsdb.graph.processing;