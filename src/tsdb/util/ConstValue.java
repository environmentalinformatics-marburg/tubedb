package tsdb.util;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Functional interface for ObservableValue
 * @author woellauer
 *
 * @param <T>
 */
@FunctionalInterface 
interface ConstValue<T> extends ObservableValue<T> {
	@Override
	public default void addListener(InvalidationListener listener) {}
	@Override
	public default void removeListener(InvalidationListener listener) {}
	@Override
	public default void addListener(ChangeListener<? super T> listener) {}
	@Override
	public default void removeListener(ChangeListener<? super T> listener) {}
}