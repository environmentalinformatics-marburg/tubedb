package tsdb.explorer;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import tsdb.util.Pair;

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
	
	public static <A, B> ConstValue<A> ofPairA(Pair<A,B> pair) {
		return ()->pair.a;
	}
	
	public static <A, B> ConstValue<B> ofPairB(Pair<A,B> pair) {
		return ()->pair.b;
	}
}