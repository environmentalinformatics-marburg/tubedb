package tsdb.explorer;

import javafx.scene.layout.GridPane;

/**
 * List of properties (name, value)
 */
public class PropertyGrid {
	
	private final GridPane gridPane;
	private int rowNr=0;
	
	public PropertyGrid() {
		gridPane = new GridPane();
		gridPane.setStyle("-fx-border-style:solid;-fx-border-color: transparent;-fx-border-width: 20;");
		gridPane.setHgap(10);
		gridPane.setVgap(10);
	}
	
	public GridPane getRoot() {
		return gridPane;
	}
	
	public void add(PropertyView propertyView) {
		propertyView.setGrid(gridPane, rowNr++);
	}

}
