package tsdb.explorer;

import java.util.function.Function;

import com.sun.javafx.binding.StringConstant;

import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import tsdb.StationProperties;
import tsdb.util.TimeUtil;
import tsdb.util.TimestampInterval;

public final class FXUtil {
	
	private FXUtil(){}
	
	@SuppressWarnings("unchecked")
	public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> cellFactoryWithOnClicked(EventHandler<? super MouseEvent> cb) {
		return col -> {
			TableCell<S,T> cell = (TableCell<S,T>) TableColumn.DEFAULT_CELL_FACTORY.call(col);
			cell.setOnMouseClicked(cb);
			return cell;
		};
	}
	
	public static class TimestampTableCell extends TableCell<TimestampInterval<StationProperties>, Long> {
		@Override
		protected void updateItem(Long item, boolean empty) {
			super.updateItem(item, empty);
			if(empty) {
				super.setText(null);
			} else if (item == null) {
				super.setText("*");
			} else {
				super.setText(TimeUtil.oleMinutesToText(item));
			}
		}			
	}
	
	public static <T> Callback<CellDataFeatures<T, String>, ObservableValue<String>> getCellValueFactory(Function<T, String> extractor) {
		return (CellDataFeatures<T, String> c)-> StringConstant.valueOf(extractor.apply(c.getValue()));
	}
	
	public static <T> Callback<CellDataFeatures<T, String>, ObservableValue<String>> getCellValueFactoryObservable(Function<T, ObservableValue<String>> extractor) {
		return (CellDataFeatures<T, String> c)-> extractor.apply(c.getValue());
	}
	
	public static <T> void setColumnExtractor(TableColumn<T,String> column, Function<T, String> extractor) {
		column.setCellValueFactory(getCellValueFactory(extractor));
	}
	
	public static <T> void setColumnObservableExtractor(TableColumn<T,String> column, Function<T, ObservableValue<String>> extractor) {
		column.setCellValueFactory(getCellValueFactoryObservable(extractor));
	}

}
