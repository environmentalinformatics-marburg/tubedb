package tsdb.explorer;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * view of property (name, value)
 */
public class PropertyView {
	
	public final Label lblName;
	public final Label lblValue;
	
	public PropertyView(String name, String value) {
		lblName = new Label(name);
		lblValue = new Label(value);
	}
	
	public void setGrid(GridPane pridPane, int rowNr) {
		pridPane.add(lblName, 0, rowNr);
		pridPane.add(lblValue, 1, rowNr);
	}

}
