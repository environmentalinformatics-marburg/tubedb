package tsdb.explorer.metadata;

import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.javafx.binding.StringConstant;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import tsdb.component.LoggerType;
import tsdb.explorer.FXUtil;
import tsdb.remote.RemoteTsDB;

/**
 * Overview of loggers
 * @author woellauer
 *
 */
public class LoggerView {
	private static final Logger log = LogManager.getLogger();	
	
	private TableView<LoggerType> tableLogger;
	
	private Node node;
	
	private final MetadataScene metadataScene;

	private Label lblStatus;
	
	public LoggerView (MetadataScene metadataScene) {
		this.metadataScene = metadataScene;
		node = createContent();
	}
	
	public Node getNode() {
		return node;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node createContent() {
		BorderPane borderPane = new BorderPane();

		tableLogger = new TableView<LoggerType>();
		tableLogger.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);		
		TableColumn<LoggerType,String> colName = new TableColumn<LoggerType,String>("name");
		colName.setCellValueFactory(cdf->StringConstant.valueOf(cdf.getValue().typeName));
		colName.setComparator(String.CASE_INSENSITIVE_ORDER);
		tableLogger.getColumns().addAll(colName);
		tableLogger.getSortOrder().clear();
		tableLogger.getSortOrder().add(colName);

		borderPane.setLeft(tableLogger);


		GridPane detailPane = new GridPane();
		detailPane.setStyle("-fx-border-style:solid;-fx-border-color: transparent;-fx-border-width: 20;");
		detailPane.setHgap(10);
		detailPane.setVgap(10);

		Label lblLogger = new Label();
		detailPane.add(new Label("Logger"), 0, 0);
		detailPane.add(lblLogger, 1, 0);

		TableView<String> tableLoggerSensor = new TableView<String>();
		tableLoggerSensor.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);		
		TableColumn<String,String> colLoggerSensorName = new TableColumn<String,String>("Sensor");
		colLoggerSensorName.setCellValueFactory(cdf->StringConstant.valueOf(cdf.getValue()));
		colLoggerSensorName.setMinWidth(180);
		colLoggerSensorName.setCellFactory(FXUtil.cellFactoryWithOnClicked(e->{
			String sensorName = tableLoggerSensor.getSelectionModel().selectedItemProperty().get();
			metadataScene.selectSensor(sensorName);
		}));
		colLoggerSensorName.setComparator(String.CASE_INSENSITIVE_ORDER);
		tableLoggerSensor.getColumns().addAll(colLoggerSensorName);
		tableLoggerSensor.getSortOrder().clear();
		tableLoggerSensor.getSortOrder().add(colLoggerSensorName);
		GridPane.setRowIndex(tableLoggerSensor, 1);
		GridPane.setColumnSpan(tableLoggerSensor, 2);
		detailPane.getChildren().add(tableLoggerSensor);


		TableView<Entry<String, String>> tableTranslation = new TableView<Entry<String, String>>();
		tableTranslation.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		TableColumn<Entry<String, String>,String> colTranslationHeader = new TableColumn<Entry<String, String>,String>("Header");
		colTranslationHeader.setCellValueFactory(cdf->StringConstant.valueOf(cdf.getValue().getKey()));
		colTranslationHeader.setMinWidth(180);
		colTranslationHeader.setComparator(String.CASE_INSENSITIVE_ORDER);
		TableColumn<Entry<String, String>,String> colTranslationSensor = new TableColumn<Entry<String, String>,String>("Translation");
		colTranslationSensor.setCellValueFactory(cdf->StringConstant.valueOf(cdf.getValue().getValue()));
		colTranslationSensor.setMinWidth(180);
		colTranslationSensor.setComparator(String.CASE_INSENSITIVE_ORDER);
		TableColumn<Entry<String, String>,String> colTop = new TableColumn<Entry<String, String>,String>("Sensor Name Translation");
		colTop.getColumns().setAll(colTranslationHeader,colTranslationSensor);
		tableTranslation.getColumns().addAll(colTop);
		tableTranslation.getSortOrder().setAll(colTranslationHeader,colTranslationSensor);

		GridPane.setRowIndex(tableTranslation, 1);
		GridPane.setColumnIndex(tableTranslation, 2);
		GridPane.setColumnSpan(tableTranslation, 2);
		detailPane.getChildren().add(tableTranslation);

		tableLogger.getSelectionModel().selectedItemProperty().addListener((s,o,logger)->{
			TableColumn[] save = tableLoggerSensor.getSortOrder().toArray(new TableColumn[0]);
			TableColumn[] saveTr = tableTranslation.getSortOrder().toArray(new TableColumn[0]);
			if(logger!=null) {
				lblLogger.setText(logger.typeName);
				tableLoggerSensor.setItems(FXCollections.observableArrayList(logger.sensorNames));

				ObservableList<Entry<String, String>> translationList = FXCollections.observableArrayList();
				if(logger.sensorNameTranlationMap!=null) {
					translationList.addAll(logger.sensorNameTranlationMap.entrySet());
				}
				tableTranslation.setItems(translationList);
				tableTranslation.sort();
			} else {
				tableTranslation.setItems(null);
			}
			tableLoggerSensor.getSortOrder().setAll(save);
			tableTranslation.getSortOrder().setAll(saveTr);
		});

		borderPane.setCenter(detailPane);
		
		HBox statusPane = new HBox();
		lblStatus = new Label("status");
		statusPane.getChildren().addAll(lblStatus);
		borderPane.setBottom(statusPane);

		return borderPane;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void collectData(RemoteTsDB tsdb) {
		TableColumn[] save = tableLogger.getSortOrder().toArray(new TableColumn[0]);
		ObservableList<LoggerType> loggerList = FXCollections.observableArrayList();
		try {
			LoggerType[] loggers = tsdb.getLoggerTypes();
			if(loggers!=null)
				loggerList.addAll(loggers);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
		loggerList.addListener(this::onVirtualPlotListInvalidation);
		tableLogger.setItems(loggerList);
		tableLogger.sort();
		tableLogger.getSortOrder().setAll(save);
	}

	public void selectLogger(String name) {
		for(LoggerType item:tableLogger.getItems()) {
			if(item.typeName.equals(name)) {
				tableLogger.getSelectionModel().select(item);
				return;
			}
		}
		tableLogger.getSelectionModel().clearSelection();
	}
	
	private void onVirtualPlotListInvalidation(Observable o) {
		lblStatus.setText(tableLogger.getItems().size()+" entries");
	}

}
