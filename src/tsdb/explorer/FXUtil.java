package tsdb.explorer;

import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
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

}
