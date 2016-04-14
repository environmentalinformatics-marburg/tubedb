package tsdb.explorer;

import java.util.function.Function;

import javafx.scene.control.TableColumn;

/**
 * Column of type String
 */
class StringColumn<T> extends TableColumn<T, String> {
	public StringColumn(String text) {
		super(text);
	}
	
	public StringColumn(String text, Function<T, String> extractor) {
		super(text);
		FXUtil.setColumnExtractor(this, extractor);
	}
}