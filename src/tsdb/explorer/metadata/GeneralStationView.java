package tsdb.explorer.metadata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.javafx.binding.StringConstant;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import tsdb.component.Region;
import tsdb.remote.GeneralStationInfo;
import tsdb.remote.RemoteTsDB;

/**
 * Overview of general stations
 * @author woellauer
 *
 */
public class GeneralStationView {
	private static final Logger log = LogManager.getLogger();
	
	private TableView<GeneralStationInfo> tableGeneralStation;
	
	private Node node;

	private final MetadataScene metadataScene;

	private Label lblStatus;
	
	public GeneralStationView(MetadataScene metadataScene) {
		this.metadataScene = metadataScene;
		node = createContent();
	}
	
	public Node getNode() {
		return node;
	}
	
	@SuppressWarnings("unchecked")
	private Node createContent() {
		BorderPane borderPane = new BorderPane();
				
		tableGeneralStation = new TableView<GeneralStationInfo>();
		tableGeneralStation.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		borderPane.setLeft(tableGeneralStation);
		TableColumn<GeneralStationInfo,String> colName = new TableColumn<GeneralStationInfo,String>("name");
		colName.setCellValueFactory(cdf->StringConstant.valueOf(cdf.getValue().name));
		colName.setComparator(String.CASE_INSENSITIVE_ORDER);
		tableGeneralStation.getColumns().addAll(colName);
		tableGeneralStation.getSortOrder().clear();
		tableGeneralStation.getSortOrder().add(colName);
				
		GridPane detailPane = new GridPane();
		borderPane.setCenter(detailPane);
		detailPane.setStyle("-fx-border-style:solid;-fx-border-color: transparent;-fx-border-width: 20;");
		detailPane.setHgap(10);
		detailPane.setVgap(10);

		Label lblGeneral = new Label();
		detailPane.add(new Label("General Station"), 0, 0);
		detailPane.add(lblGeneral, 1, 0);
		
		Label lblGeneralLong = new Label();
		detailPane.add(new Label("Full Name"), 0, 1);
		detailPane.add(lblGeneralLong, 1, 1);
		
		Label lblGroup = new Label();
		detailPane.add(new Label("Group"), 0, 2);
		detailPane.add(lblGroup, 1, 2);
		
		Hyperlink lblRegion = new Hyperlink();
		lblRegion.setOnAction(e->{
			GeneralStationInfo generalstation = tableGeneralStation.getSelectionModel().selectedItemProperty().get();
			Region region = generalstation.region;
			metadataScene.selectRegion(region.name);
			lblRegion.setVisited(false);
		});		
		detailPane.add(new Label("Region"), 0, 3);
		detailPane.add(lblRegion, 1, 3);
		
		Label lblStations = new Label();
		detailPane.add(new Label("Station Plots"), 0, 4);
		detailPane.add(lblStations, 1, 4);
		
		Label lblVirtualPlots = new Label();
		detailPane.add(new Label("Virtual Plots"), 0, 5);
		detailPane.add(lblVirtualPlots, 1, 5);
		
		tableGeneralStation.getSelectionModel().selectedItemProperty().addListener((s,o,general)->{
			if(general!=null) {
				lblGeneral.setText(general.name);
				lblGeneralLong.setText(general.longName);
				lblGroup.setText(general.group);
				lblRegion.setText(general.region==null?null:general.region.name);
				lblStations.setText(""+general.stationCount);
				lblVirtualPlots.setText(""+general.virtualPlotCount);
			} else {
				lblGeneral.setText(null);
				lblGeneralLong.setText(null);
				lblGroup.setText(null);
				lblRegion.setText(null);
				lblStations.setText(null);
				lblVirtualPlots.setText(null);
			}
		});
		
		HBox statusPane = new HBox();
		lblStatus = new Label("status");
		statusPane.getChildren().addAll(lblStatus);
		borderPane.setBottom(statusPane);
		
		return borderPane;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void collectData(RemoteTsDB tsdb) {
		TableColumn[] save = tableGeneralStation.getSortOrder().toArray(new TableColumn[0]);
		ObservableList<GeneralStationInfo> generalList = FXCollections.observableArrayList();
		try {
			GeneralStationInfo[] generals = tsdb.getGeneralStations();
			if(generals!=null) {
				generalList.addAll(generals);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
		generalList.addListener(this::onVirtualPlotListInvalidation);
		tableGeneralStation.setItems(generalList);
		tableGeneralStation.sort();
		tableGeneralStation.getSortOrder().setAll(save);
	}
	
	public void selectGeneralStation(String name) {
		for(GeneralStationInfo item:tableGeneralStation.getItems()) {
			if(item.name.equals(name)) {
				tableGeneralStation.getSelectionModel().select(item);
				return;
			}
		}
		tableGeneralStation.getSelectionModel().clearSelection();
	}
	
	private void onVirtualPlotListInvalidation(Observable o) {
		lblStatus.setText(tableGeneralStation.getItems().size()+" entries");
	}

}
