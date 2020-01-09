package tsdb.explorer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import tsdb.component.SourceEntry;
import tsdb.util.Pair;
import tsdb.util.StringPair;
import tsdb.util.TimeUtil;

/**
 * Detail view of one SourceItem
 */
public class SourceItemScene extends TsdbScene {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();
	
	private final SourceItem sourceItem;
	
	private PropertyGrid propertyGrid;
	private TableView<StringPair> tableView;
	
	public SourceItemScene(SourceItem sourceItem) {
		super("source item");
		this.sourceItem = sourceItem;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Parent createContent() {
		BorderPane mainBoderPane = new BorderPane();
		this.propertyGrid = new PropertyGrid();		
		mainBoderPane.setTop(propertyGrid.getRoot());
		tableView = new TableView<StringPair>();
		StringColumn<StringPair> colHeaderName = new StringColumn<StringPair>("header name", Pair::getA);
		StringColumn<StringPair> colSensorName = new StringColumn<StringPair>("sensor name", Pair::getB);
		tableView.getColumns().setAll(colHeaderName, colSensorName);
		mainBoderPane.setCenter(tableView);
		return mainBoderPane;
	}

	@Override
	protected void onShown() {
		SourceEntry sourceEntry = sourceItem.sourceEntry;
		propertyGrid.add(new PropertyView("region", sourceItem.regionName));
		propertyGrid.add(new PropertyView("general station", sourceItem.generalStationName));
		propertyGrid.add(new PropertyView("plot", sourceItem.plotid));
		propertyGrid.add(new PropertyView("station", sourceEntry.stationName));
		propertyGrid.add(new PropertyView("path", sourceEntry.path));		
		propertyGrid.add(new PropertyView("filename", sourceEntry.filename));		
		
		propertyGrid.add(new PropertyView("rows", ""+sourceEntry.rows));
		propertyGrid.add(new PropertyView("first", TimeUtil.oleMinutesToText(sourceEntry.firstTimestamp)));
		propertyGrid.add(new PropertyView("last", TimeUtil.oleMinutesToText(sourceEntry.lastTimestamp)));
		propertyGrid.add(new PropertyView("columns", ""+sourceEntry.headerNames.length));
		
		
		ObservableList<StringPair> list = FXCollections.observableArrayList();
		
		for (int i = 0; i < sourceEntry.headerNames.length; i++) {
			String translation = (i<sourceEntry.sensorNames.length)?sourceEntry.sensorNames[i]:"---";
			list.add(StringPair.of(sourceEntry.headerNames[i],(translation==null)?"-":translation));
		}
		
		tableView.setItems(list);		
	}
}
