package tsdb.explorer.metadata;

import static tsdb.util.AssumptionCheck.throwNull;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.explorer.TsdbScene;
import tsdb.remote.RemoteTsDB;

/**
 * Central window for all types of metadata
 * @author woellauer
 *
 */
public class MetadataScene extends TsdbScene {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();	
	private final RemoteTsDB tsdb;
	
	private VirtualPlotView virtualPlotView;
	private StationView stationView;
	private LoggerView loggerView;
	private SensorView sensorView;
	private GeneralStationView generalStationView;
	private RegionView regionView;
	
	private TabPane tabPane;
	Tab tabVirtualPlot;
	Tab tabStation;
	Tab tabLogger;
	Tab tabSensor;
	Tab tabGeneralStation;
	Tab tabRegion;

	public MetadataScene(RemoteTsDB tsdb) {
		super("time series databaes metadata view");		
		throwNull(tsdb);
		this.tsdb = tsdb;
	}

	@Override
	protected Parent createContent() {
		tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		tabVirtualPlot = new Tab("Virtual Plot");
		virtualPlotView = new VirtualPlotView(this);
		tabVirtualPlot.setContent(virtualPlotView.getNode());
		tabPane.getTabs().add(tabVirtualPlot);

		tabStation = new Tab("Station");
		stationView = new StationView(this);
		tabStation.setContent(stationView.getNode());
		tabPane.getTabs().add(tabStation);

		tabLogger = new Tab("Logger");
		loggerView = new LoggerView(this);
		tabLogger.setContent(loggerView.getNode());
		tabPane.getTabs().add(tabLogger);

		tabSensor = new Tab("Sensor");
		sensorView = new SensorView();
		tabSensor.setContent(sensorView.getNode());
		tabPane.getTabs().add(tabSensor);

		tabGeneralStation = new Tab("General Station");
		generalStationView = new GeneralStationView(this);
		tabGeneralStation.setContent(generalStationView.getNode());
		tabPane.getTabs().add(tabGeneralStation);
		
		tabRegion = new Tab("Region");
		regionView = new RegionView();
		tabRegion.setContent(regionView.getNode());
		tabPane.getTabs().add(tabRegion);

		return tabPane;
	}	

	@Override
	protected void onShown() {
		sensorView.collectData(tsdb);
		stationView.collectData(tsdb);
		loggerView.collectData(tsdb);
		virtualPlotView.collectData(tsdb);
		generalStationView.collectData(tsdb);
		regionView.collectData(tsdb);
	}
	
	public void selectGeneralStation(String name) {
		generalStationView.selectGeneralStation(name);
		tabPane.selectionModelProperty().get().select(tabGeneralStation);
	}

	public void selectLogger(String name) {
		loggerView.selectLogger(name);
		tabPane.selectionModelProperty().get().select(tabLogger);
	}

	public void selectRegion(String name) {
		regionView.selectRegion(name);
		tabPane.selectionModelProperty().get().select(tabRegion);
		
	}
	
	public void selectStation(String name) {
		stationView.selectStation(name);
		tabPane.selectionModelProperty().get().select(tabStation);
		
	}
	
	public void selectSensor(String name) {
		sensorView.selectSensor(name);
		tabPane.selectionModelProperty().get().select(tabSensor);
		
	}
}