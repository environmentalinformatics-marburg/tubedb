package tsdb.explorer.metadata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.javafx.binding.ObjectConstant;
import com.sun.javafx.binding.StringConstant;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import tsdb.StationProperties;
import tsdb.component.LoggerType;
import tsdb.explorer.FXUtil;
import tsdb.remote.GeneralStationInfo;
import tsdb.remote.RemoteTsDB;
import tsdb.remote.StationInfo;
import tsdb.util.TimeUtil;
import tsdb.util.TimestampInterval;

/**
 * Overview of stations
 * @author woellauer
 *
 */
public class StationView {
	private static final Logger log = LogManager.getLogger();	
	
	private TableView<StationInfo> tableStation;
	private CheckBox checkBoxShowPlotStationsOnly;
	private FilteredList<StationInfo> filteredStationList;
	
	private Node node;
	
	private final MetadataScene metadataScene;

	private Label lblStatus;

	private TableView<TimestampInterval<StationProperties>> tableInterval;
	
	public StationView(MetadataScene metadataScene) {
		this.metadataScene = metadataScene;
		node = createContent();
	}
	
	public Node getNode() {
		return node;
	}
	
	
	@SuppressWarnings("unchecked")
	private Node createContent() {
		BorderPane borderPane = new BorderPane();

		tableStation = new TableView<StationInfo>();
		tableStation.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<StationInfo,String> colName = new TableColumn<StationInfo,String>("name");
		colName.comparatorProperty().set(String.CASE_INSENSITIVE_ORDER);
		colName.setCellValueFactory(cdf->StringConstant.valueOf(cdf.getValue().stationID));

		tableStation.getColumns().addAll(colName);
		tableStation.getSortOrder().clear();
		tableStation.getSortOrder().add(colName);

		borderPane.setLeft(tableStation);

		checkBoxShowPlotStationsOnly = new CheckBox("show stations that are plots only");
		checkBoxShowPlotStationsOnly.setOnAction(this::updateStationPredicate);
		borderPane.setTop(checkBoxShowPlotStationsOnly);

		GridPane detailPane = new GridPane();
		detailPane.setStyle("-fx-border-style:solid;-fx-border-color: transparent;-fx-border-width: 20;");
		detailPane.setHgap(10);
		detailPane.setVgap(10);

		Label lblStation = new Label();
		detailPane.add(new Label("Station"), 0, 0);
		detailPane.add(lblStation, 1, 0);

		Hyperlink lblLogger = new Hyperlink();
		lblLogger.setOnAction(e->{
			StationInfo station = tableStation.getSelectionModel().selectedItemProperty().get();
			LoggerType loggerType = station.loggerType;
			metadataScene.selectLogger(loggerType.typeName);
			lblLogger.setVisited(false);
		});
		detailPane.add(new Label("Logger"), 0, 1);
		detailPane.add(lblLogger, 1, 1);

		Hyperlink lblGeneralStation = new Hyperlink();
		lblGeneralStation.setOnAction(e->{
			StationInfo station = tableStation.getSelectionModel().selectedItemProperty().get();
			GeneralStationInfo generalStation = station.generalStationInfo;
			metadataScene.selectGeneralStation(generalStation.name);
			lblGeneralStation.setVisited(false);
		});
		detailPane.add(new Label("General Station"), 0, 2);
		detailPane.add(lblGeneralStation, 1, 2);

		Label lblLocation = new Label();
		detailPane.add(new Label("Location"), 0, 3);
		detailPane.add(lblLocation, 1, 3);

		Label lblSecondary = new Label();
		detailPane.add(new Label("Secondary ID"), 0, 4);
		detailPane.add(lblSecondary, 1, 4);
		
		tableInterval = new TableView<TimestampInterval<StationProperties>>();
		tableInterval.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		TableColumn<TimestampInterval<StationProperties>,Long> colIntervalStart = new TableColumn<TimestampInterval<StationProperties>,Long>("Start");
		colIntervalStart.setCellValueFactory(cdf->ObjectConstant.<Long>valueOf(cdf.getValue().start));
		colIntervalStart.setMinWidth(150);
		colIntervalStart.setCellFactory(p->new FXUtil.TimestampTableCell());
		colIntervalStart.setComparator(TimeUtil.TIMESTAMP_START_ASC_COMPARATOR);
		TableColumn<TimestampInterval<StationProperties>,Long> colIntervalEnd = new TableColumn<TimestampInterval<StationProperties>,Long>("End");
		colIntervalEnd.setCellValueFactory(cdf->ObjectConstant.<Long>valueOf(cdf.getValue().end));
		colIntervalEnd.setMinWidth(150);
		colIntervalEnd.setCellFactory(p->new FXUtil.TimestampTableCell());
		colIntervalEnd.setComparator(TimeUtil.TIMESTAMP_END_ASC_COMPARATOR);
		TableColumn<TimestampInterval<StationProperties>,String> colIntervalPlot = new TableColumn<TimestampInterval<StationProperties>,String>("Plot");
		colIntervalPlot.setCellValueFactory(cdf->StringConstant.valueOf(cdf.getValue().value.get_plotid()));		
		colIntervalPlot.setCellFactory(FXUtil.cellFactoryWithOnClicked(e->{
			StationProperties stationProperties = tableInterval.getSelectionModel().selectedItemProperty().get().value;
			String serial = stationProperties.get_serial();
			metadataScene.selectStation(serial);
		}));
		colIntervalPlot.setComparator(String.CASE_INSENSITIVE_ORDER);
		colIntervalPlot.setMinWidth(150);		
		tableInterval.getColumns().addAll(colIntervalStart,colIntervalEnd,colIntervalPlot);
		tableInterval.getSortOrder().clear();
		tableInterval.getSortOrder().addAll(colIntervalStart,colIntervalEnd,colIntervalPlot);

		GridPane.setRowIndex(tableInterval, 5);
		GridPane.setColumnIndex(tableInterval, 0);
		GridPane.setColumnSpan(tableInterval, 2);
		detailPane.getChildren().add(tableInterval);
		
		

		tableStation.getSelectionModel().selectedItemProperty().addListener((s,o,station)->{
			@SuppressWarnings("rawtypes")
			TableColumn[] save = tableInterval.getSortOrder().toArray(new TableColumn[0]);
			if(station!=null) {
				lblStation.setText(station.stationID);
				lblLogger.setText(station.loggerType.typeName);
				lblGeneralStation.setText(station.generalStationInfo==null?null:station.generalStationInfo.name);				
				String loc;
				if(Double.isNaN(station.geoPosLatitude)&&Double.isNaN(station.geoPosLatitude)) {
					loc = null;
				} else {
					loc = station.geoPosLatitude+"  ,  "+station.geoPoslongitude;
				}
				lblLocation.setText(loc);
				lblSecondary.setText(station.alternativeID);
				tableInterval.setItems(FXCollections.observableList(station.intervalList));
				tableInterval.sort();
			} else {
				tableInterval.setItems(null);
			}
			tableInterval.getSortOrder().setAll(save);
		});

		borderPane.setCenter(detailPane);
		
		HBox statusPane = new HBox();
		lblStatus = new Label("status");
		statusPane.getChildren().addAll(lblStatus);
		borderPane.setBottom(statusPane);

		return borderPane;
	}
	
	private void updateStationPredicate(ActionEvent e) {
		if(checkBoxShowPlotStationsOnly.isSelected()) {
			filteredStationList.setPredicate(s->s.generalStationInfo!=null); // change !!!
		} else {
			filteredStationList.setPredicate(s->true);			
		}	
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void collectData(RemoteTsDB tsdb) {
		TableColumn[] save = tableStation.getSortOrder().toArray(new TableColumn[0]);
		ObservableList<StationInfo> stationList = FXCollections.observableArrayList();
		filteredStationList = new FilteredList<StationInfo>(stationList);
		updateStationPredicate(null);

		try {
			StationInfo[] stations = tsdb.getStations();
			if(stations!=null)
				stationList.addAll(stations);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}

		SortedList<StationInfo> sorted = new SortedList<>(filteredStationList);//bugfix for FilteredList with TableView
		sorted.comparatorProperty().bind(tableStation.comparatorProperty());
		sorted.addListener(this::onVirtualPlotListInvalidation);
		tableStation.setItems(sorted);
		//tableStation.setItems(stationList);
		tableStation.sort();
		tableStation.getSortOrder().setAll(save);
	}

	public void selectStation(String name) {
		for(StationInfo item:tableStation.getItems()) {
			if(item.stationID.equals(name)) {
				tableStation.getSelectionModel().select(item);
				return;
			}
		}		
	}
	
	private void onVirtualPlotListInvalidation(Observable o) {
		lblStatus.setText(tableStation.getItems().size()+" entries");
	}
}
