package tsdb.explorer.metadata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.javafx.binding.StringConstant;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import tsdb.component.Region;
import tsdb.remote.RemoteTsDB;

/**
 * Overview of regions
 * @author woellauer
 *
 */
public class RegionView {
	private static final Logger log = LogManager.getLogger();
	
	private TableView<Region> tableRegion;
	
	private Node node;

	private Label lblStatus;
	
	public RegionView() {
		node = createContent();
	}
	
	public Node getNode() {
		return node;
	}
	
	@SuppressWarnings("unchecked")
	private Node createContent() {
		BorderPane borderPane = new BorderPane();
		
		tableRegion = new TableView<Region>();
		tableRegion.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		borderPane.setLeft(tableRegion);
		TableColumn<Region,String> colName = new TableColumn<Region,String>("name");
		colName.setCellValueFactory(cdf->StringConstant.valueOf(cdf.getValue().name));
		colName.setComparator(String.CASE_INSENSITIVE_ORDER);
		colName.setSortType(SortType.ASCENDING);
		tableRegion.getColumns().addAll(colName);
		tableRegion.getSortOrder().clear();
		tableRegion.getSortOrder().add(colName);
		
		
		GridPane detailPane = new GridPane();
		borderPane.setCenter(detailPane);
		detailPane.setStyle("-fx-border-style:solid;-fx-border-color: transparent;-fx-border-width: 20;");
		detailPane.setHgap(10);
		detailPane.setVgap(10);

		Label lblRegion = new Label();
		detailPane.add(new Label("Region"), 0, 0);
		detailPane.add(lblRegion, 1, 0);
		
		Label lblRegionLong = new Label();
		detailPane.add(new Label("Full Name"), 0, 1);
		detailPane.add(lblRegionLong, 1, 1);
		
		Label lblRegionViewTimeRange = new Label();
		detailPane.add(new Label("View Time Range"), 0, 2);
		detailPane.add(lblRegionViewTimeRange, 1, 2);
		
		tableRegion.getSelectionModel().selectedItemProperty().addListener((s,o,region)->{
			if(region!=null) {
				lblRegion.setText(region.name);
				lblRegionLong.setText(region.longName);
				lblRegionViewTimeRange.setText(region.viewTimeRange.toString());
			} else {
				lblRegion.setText(null);
				lblRegionLong.setText(null);
				lblRegionViewTimeRange.setText(null);
			}
		});
		
		HBox statusPane = new HBox();
		lblStatus = new Label("status");
		statusPane.getChildren().addAll(lblStatus);
		borderPane.setBottom(statusPane);
		
		return borderPane;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void collectData(RemoteTsDB tsdb) {
		TableColumn[] save = tableRegion.getSortOrder().toArray(new TableColumn[0]);
		ObservableList<Region> regionList = FXCollections.observableArrayList();
		try {
			Region[] regions = tsdb.getRegions();
			if(regions!=null) {
				regionList.addAll(regions);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
		regionList.addListener(this::onVirtualPlotListInvalidation);
		tableRegion.setItems(regionList);
		tableRegion.sort();
		tableRegion.getSortOrder().setAll(save);
	}

	public void selectRegion(String name) {
		for(Region item:tableRegion.getItems()) {
			if(item.name.equals(name)) {
				tableRegion.getSelectionModel().select(item);
				return;
			}
		}
		tableRegion.getSelectionModel().clearSelection();		
	}
	
	private void onVirtualPlotListInvalidation(Observable o) {
		lblStatus.setText(tableRegion.getItems().size()+" entries");
	}
}
