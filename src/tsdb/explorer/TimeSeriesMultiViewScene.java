package tsdb.explorer;

import static tsdb.util.AssumptionCheck.throwNull;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.component.Region;
import tsdb.component.Sensor;
import tsdb.remote.GeneralStationInfo;
import tsdb.remote.PlotInfo;
import tsdb.remote.RemoteTsDB;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.gui.TimeSeriesDiagram;
import tsdb.util.gui.TimeSeriesHeatMap;
import tsdb.util.gui.TimeSeriesPainterGraphics2D;
import tsdb.util.iterator.TimestampSeries;

/**
 * View of multiple time series
 * @author woellauer
 *
 */
public class TimeSeriesMultiViewScene extends TsdbScene {
	private static final Logger log = LogManager.getLogger();	
	private final RemoteTsDB tsdb;

	private static final Region regionAll = new Region("[all]","[all]");
	private static final GeneralStationInfo GeneralStationAll = new GeneralStationInfo("[all]", "[?]");
	private static final PlotInfo plotAll = new PlotInfo("[all]", "[?]", "[?]");
	private static final Sensor sensorAll = new Sensor("[all]");
	private static final String timeAll = "[all]";

	private ComboBox<Region> comboRegion;
	private ComboBox<GeneralStationInfo> comboGeneralStation;
	private ComboBox<PlotInfo> comboPlot;
	private ComboBox<Sensor> comboSensor;
	private SimpleStringProperty selectedCountProperty;

	private ArrayList<QueryEntry> queryList = new ArrayList<QueryEntry>();
	private ArrayList<QueryEntry> selectionQueryList = new ArrayList<QueryEntry>();
	private ArrayList<ScreenImageEntry> screenImageList = new ArrayList<ScreenImageEntry>();

	private ScrollBar scrollBar;
	private VBox vboxQueryImages;
	private BorderPane borderPaneDiagrams;


	private PriorityExecutor executor;

	private ComboBox<String> comboTime;
	private HashMap<String, Sensor> sensorMap;
	private ComboBox<DiagramType> comboView;

	protected TimeSeriesMultiViewScene(RemoteTsDB tsdb) {
		super("time series multi view");
		throwNull(tsdb);
		this.tsdb = tsdb;

		executor = new PriorityExecutor();

	}

	@Override
	protected Parent createContent() {
		BorderPane borderPaneMain = new BorderPane();


		Label labelRegion = new Label("Region");
		labelRegion.setAlignment(Pos.CENTER);
		labelRegion.setMaxHeight(100d);
		comboRegion = new ComboBox<Region>();
		StringConverter<Region> regionConverter = new StringConverter<Region>() {			
			@Override
			public String toString(Region region) {
				return region.longName;
			}			
			@Override
			public Region fromString(String string) {
				return null;
			}
		};
		comboRegion.setConverter(regionConverter);
		comboRegion.valueProperty().addListener(this::onRegionChanged);

		Label labelGeneralStation = new Label("General");
		labelGeneralStation.setAlignment(Pos.CENTER);
		labelGeneralStation.setMaxHeight(100d);
		comboGeneralStation = new ComboBox<GeneralStationInfo>();
		StringConverter<GeneralStationInfo> generalStationConverter = new StringConverter<GeneralStationInfo>() {			
			@Override
			public String toString(GeneralStationInfo general) {
				return general.longName;
			}			
			@Override
			public GeneralStationInfo fromString(String string) {
				return null;
			}
		};
		comboGeneralStation.setConverter(generalStationConverter);
		comboGeneralStation.valueProperty().addListener(this::onGeneralStationChanged);

		Label labelPlot = new Label("Plot");
		labelPlot.setAlignment(Pos.CENTER);
		labelPlot.setMaxHeight(100d);
		comboPlot = new ComboBox<PlotInfo>();
		StringConverter<PlotInfo> plotConverter = new StringConverter<PlotInfo>() {			
			@Override
			public String toString(PlotInfo plot) {
				return plot.name;
			}			
			@Override
			public PlotInfo fromString(String string) {
				return null;
			}
		};
		comboPlot.setConverter(plotConverter);
		comboPlot.valueProperty().addListener(this::onPlotChanged);

		Label labelSensor = new Label("Sensor");
		labelSensor.setAlignment(Pos.CENTER);
		labelSensor.setMaxHeight(100d);
		comboSensor = new ComboBox<Sensor>();
		StringConverter<Sensor> sensorConverter = new StringConverter<Sensor>() {			
			@Override
			public String toString(Sensor sensor) {
				return sensor.name;
			}			
			@Override
			public Sensor fromString(String string) {
				return null;
			}
		};
		comboSensor.setConverter(sensorConverter);
		comboSensor.valueProperty().addListener(this::onSensorChanged);


		Label labelTime = new Label("Time");
		labelTime.setAlignment(Pos.CENTER);
		labelTime.setMaxHeight(100d);
		comboTime = new ComboBox<String>();
		comboTime.valueProperty().addListener(this::onTimeChanged);


		Label labelView = new Label("View");
		labelView.setAlignment(Pos.CENTER);
		labelView.setMaxHeight(100d);
		comboView = new ComboBox<DiagramType>();
		comboView.valueProperty().addListener(this::onDiagramTypeChanged);




		FlowPane controlPane = new FlowPane(10d,10d);

		controlPane.getChildren().add(new HBox(10d,labelRegion,comboRegion,new Separator(Orientation.VERTICAL)));
		controlPane.getChildren().add(new HBox(10d,labelGeneralStation,comboGeneralStation,new Separator(Orientation.VERTICAL)));
		controlPane.getChildren().add(new HBox(10d,labelPlot,comboPlot,new Separator(Orientation.VERTICAL)));
		controlPane.getChildren().add(new HBox(10d,labelSensor,comboSensor,new Separator(Orientation.VERTICAL)));
		controlPane.getChildren().add(new HBox(10d,labelTime,comboTime,new Separator(Orientation.VERTICAL)));
		controlPane.getChildren().add(new HBox(10d,labelView,comboView,new Separator(Orientation.VERTICAL)));

		Label labelSelectedCount = new Label();
		selectedCountProperty = new SimpleStringProperty();
		selectedCountProperty.set("?");
		labelSelectedCount.textProperty().bind(selectedCountProperty);
		Button buttonSetCurrent = new Button("set to current");

		buttonSetCurrent.setOnAction(this::onSetCurrent);

		Button buttonAddCurrent = new Button("add to current");
		buttonAddCurrent.setDisable(true);
		controlPane.getChildren().addAll(labelSelectedCount, new Label("selected"),new Separator(Orientation.VERTICAL),new Label(" --> selection"),buttonSetCurrent,buttonAddCurrent);
		borderPaneMain.setTop(controlPane);
		/*vboxQueryImages = new VBox();
		vboxQueryImages.setAlignment(Pos.TOP_CENTER);
		scrollPane = new ScrollPane();
		scrollPane.setContent(vboxQueryImages);
		borderPaneMain.setCenter(scrollPane);*/

		borderPaneDiagrams = new BorderPane();
		scrollBar = new ScrollBar();
		scrollBar.setOrientation(Orientation.VERTICAL);
		scrollBar.valueProperty().addListener(this::updateScreen);
		borderPaneDiagrams.setRight(scrollBar);
		vboxQueryImages = new VBox();

		vboxQueryImages.widthProperty().addListener(this::onResize);
		vboxQueryImages.heightProperty().addListener(this::onResize);


		borderPaneDiagrams.setCenter(vboxQueryImages);
		borderPaneMain.setCenter(borderPaneDiagrams);




		return borderPaneMain;
	}

	private static final double imageHeight = 100;
	private static final int imageviewCount = 11;

	private void onResize(Observable observable) {
		double width = vboxQueryImages.getWidth();
		double height = vboxQueryImages.getHeight();
		System.out.println("resize vboxQueryImages "+width+"  "+height);

		vboxQueryImages.getChildren().clear();
		for(ScreenImageEntry s:screenImageList) {
			s.setQueryEntry(null);
		}
		screenImageList.clear();

		int imageCount = imageviewCount;//(int) Math.ceil(height/imageHeight);
		for(int i=0;i<imageCount;i++) {

			ScreenImageEntry sie = new ScreenImageEntry(new ImageView());
			screenImageList.add(sie);
			vboxQueryImages.getChildren().add(sie.imageView);
			sie.imageView.setOnMouseClicked(e->{
				if(e.getButton()==MouseButton.PRIMARY) {
					if(e.getClickCount()==2) {
						QueryEntry queryEntry = sie.queryEntry;
						if(queryEntry!=null) {
							TimeSeriesViewScene tsvs = new TimeSeriesViewScene(tsdb);
							tsvs.show();
							tsvs.setSelection(sie.queryEntry.plotID, sie.queryEntry.sensor.name);
						}
					}
				}
			});
		}

	}

	private void updateScreen(Observable observable) {
		int screenPos = (int) scrollBar.getValue();
		for(int i=0;i<screenImageList.size();i++) {
			int entryPos = screenPos+i;
			if(entryPos<queryList.size()) {				
				QueryEntry queryEntry = queryList.get(entryPos);
				screenImageList.get(i).setQueryEntry(queryEntry);
			} else {
				screenImageList.get(i).setQueryEntry(null);
			}
		}
	}

	private void onSetCurrent(ActionEvent event) {

		queryList = selectionQueryList;
		scrollBar.setValue(0);
		scrollBar.setMin(0);
		int scrollMax = queryList.size()-imageviewCount;
		if(scrollMax<0) {
			scrollMax = 0;
		}
		scrollBar.setMax(scrollMax);
		scrollBar.setVisibleAmount(imageviewCount);

		updateScreen(null);
		executor.clear();


		int seqCounter = 0;
		for(QueryEntry queryEntry:queryList) {

			final int sq = seqCounter;

			queryEntry.timestampSeriesProperty.addListener((s,o,ts)->{
				if(ts!=null) {



					//System.out.println("************************** add task create image "+queryEntry.plotID+"  "+queryEntry.sensor.name);

					final int width = (int)borderPaneDiagrams.getWidth()-30;
					final int height = (int)imageHeight;

					Runnable drawImageTask = ()->{
						log.info("EXECUTE create image "+queryEntry.plotID+"  "+queryEntry.sensor.name);


						BufferedImage bufferedImage = new BufferedImage( width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
						Graphics2D gc = bufferedImage.createGraphics();
						gc.setBackground(new java.awt.Color(255, 255, 255));
						gc.setColor(new java.awt.Color(0, 0, 0));
						gc.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
						gc.dispose();
						TimeSeriesPainterGraphics2D tsp = new TimeSeriesPainterGraphics2D(bufferedImage);

						if(queryEntry.diagramType==DiagramType.STANDARD) {						
							TimeSeriesDiagram tsd = new TimeSeriesDiagram(ts,AggregationInterval.HOUR,queryEntry.sensor.category, false);						
							tsd.draw(tsp);
						}

						if(queryEntry.diagramType==DiagramType.HEATMAP) {
							TimeSeriesHeatMap tshm = new TimeSeriesHeatMap(ts);
							tshm.draw(tsp, queryEntry.sensor.name,0);
						}

						gc = bufferedImage.createGraphics();					
						gc.setColor(java.awt.Color.LIGHT_GRAY);
						gc.drawString(queryEntry.plotID+" : "+queryEntry.sensor.name, 42, 20);
						gc.dispose();

						WritableImage image = SwingFXUtils.toFXImage(bufferedImage, null);

						try {
							Platform.runLater(()->{log.info("RUN_LATER imageProperty.set");queryEntry.imageProperty.set(image);});
						} catch(Exception e) {
							log.error(e);
						}
						log.info("STOP create image "+queryEntry.plotID+"  "+queryEntry.sensor.name);
					};

					try {
						executor.addTask(drawImageTask,sq,0);
					} catch(Exception e) {
						log.error(e);
					}
				}
			});



			//System.out.println("************************** add task query TimestampSeries"+queryEntry.plotID+"  "+queryEntry.sensor.name);

			Runnable queryTimeSeriesTask = ()->{
				log.info("EXECUTE query TimestampSeries"+queryEntry.plotID+"  "+queryEntry.sensor.name);
				try {
					TimestampSeries ts = tsdb.plot(null, queryEntry.plotID, new String[]{queryEntry.sensor.name}, AggregationInterval.HOUR, DataQuality.STEP, false, queryEntry.startTimestamp, queryEntry.endTimestamp);
					if(ts!=null) {
						Platform.runLater(()->{log.info("RUN_LATER timestampSeriesProperty.set");queryEntry.timestampSeriesProperty.set(ts);});
					}
				} catch (Exception e) {
					log.error(e);
				}
				log.info("STOP query TimestampSeries"+queryEntry.plotID+"  "+queryEntry.sensor.name);
			};

			try {
				executor.addTask(queryTimeSeriesTask,seqCounter,1);
			} catch(Exception e) {
				log.error(e);
			}

			seqCounter++;
		}

	}



	@Override
	protected void onShown() {
		ObservableList<String> timeList = FXCollections.observableArrayList();		
		timeList.add(timeAll);
		timeList.add("2008");
		timeList.add("2009");
		timeList.add("2010");
		timeList.add("2011");
		timeList.add("2012");
		timeList.add("2013");
		timeList.add("2014");		
		comboTime.setItems(timeList);
		comboTime.setValue(timeAll);

		ObservableList<DiagramType> viewList = FXCollections.observableArrayList();
		viewList.add(DiagramType.STANDARD);
		viewList.add(DiagramType.HEATMAP);
		comboView.setItems(viewList);
		comboView.setValue(DiagramType.STANDARD);

		try {
			Sensor[] sensors = tsdb.getSensors();
			sensorMap = new HashMap<String,Sensor>();
			for(Sensor sensor:sensors) {
				sensorMap.put(sensor.name,sensor);
			}			
		} catch (RemoteException e) {
			log.error(e);
		}

		setRegions();
	}

	private void setRegions() {
		ObservableList<Region> regionList = FXCollections.observableArrayList();
		try {
			Region[] regions = tsdb.getRegions();
			regionList.add(regionAll);
			regionList.addAll(regions);
		} catch (RemoteException e) {
			log.error(e);
		}

		comboRegion.setItems(regionList);
		comboRegion.setValue(regionAll);		
	}

	private void onRegionChanged(ObservableValue<? extends Region> observable, Region oldValue, Region region) {
		ObservableList<GeneralStationInfo> generalStationList = FXCollections.observableArrayList();

		try {
			GeneralStationInfo[] g = tsdb.getGeneralStations();
			if(region==null||region.name.equals("[all]")) {
				generalStationList.add(GeneralStationAll);
				generalStationList.addAll(g);
			} else {
				generalStationList.add(GeneralStationAll);
				for(GeneralStationInfo s:g) {
					if(s.region.name.equals(region.name)) {
						generalStationList.add(s);
					}
				}
			}
		} catch (Exception e) {
			log.error(e);
		}



		comboGeneralStation.setItems(generalStationList);
		comboGeneralStation.setValue(GeneralStationAll);
	}

	private void onGeneralStationChanged(ObservableValue<? extends GeneralStationInfo> observable, GeneralStationInfo oldValue, GeneralStationInfo general) {
		ObservableList<PlotInfo> plotList = FXCollections.observableArrayList();
		try {
			PlotInfo[] plotInfos = tsdb.getPlots();
			if(general==null||general.name.equals("[all]")) {
				Region region = comboRegion.getValue();
				if(region==null||region.name.equals("[all]")) {
					plotList.add(plotAll);
					plotList.addAll(plotInfos);
				} else {
					plotList.add(plotAll);
					for(PlotInfo plot:plotInfos) {
						if(plot.generalStationInfo.region.name.equals(region.name)) {
							plotList.add(plot);
						}
					}
				}
			} else {
				plotList.add(plotAll);
				for(PlotInfo plot:plotInfos) {
					if(plot.generalStationInfo.name.equals(general.name)) {
						plotList.add(plot);
					}
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		comboPlot.setItems(plotList);
		/*if(plotList.isEmpty()) {
			comboPlot.setValue(null);
		} else {
			comboPlot.setValue(plotList.get(0));
		}*/
		comboPlot.setValue(plotAll);
	}



	private void onPlotChanged(ObservableValue<? extends PlotInfo> observable, PlotInfo oldValue, PlotInfo plot) {
		ObservableList<Sensor> sensorList = FXCollections.observableArrayList();
		System.out.println("plot");
		try {
			Sensor[] sensors = tsdb.getSensors();
			Map<String,Sensor> sensorMap = new HashMap<String,Sensor>();
			for(Sensor sensor:sensors) {
				sensorMap.put(sensor.name, sensor);
			}

			if(plot==null||plot.name.equals(plotAll.name)) {
				if(comboPlot.getItems()!=null) {
					TreeSet<String> sensorSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
					for(PlotInfo item:comboPlot.getItems()) {
						if(!item.name.equals(plotAll.name)) {
							String[] plotSensorNames = tsdb.getSensorNamesOfPlot(item.name);
							if(plotSensorNames!=null) {
								sensorSet.addAll(Arrays.asList(plotSensorNames));
							}
						}
					}
					sensorList.add(sensorAll);
					for(String sensorName:sensorSet) {
						Sensor sensor = sensorMap.get(sensorName);
						if(sensor.isAggregable()) {
							sensorList.add(sensor);
						}
					}	
				}
			} else {
				String[] sensorNames = tsdb.getSensorNamesOfPlot(plot.name);
				sensorList.add(sensorAll);
				for(String sensorName:sensorNames) {
					Sensor sensor = sensorMap.get(sensorName);
					if(sensor.isAggregable()) {
						sensorList.add(sensor);
					}
				}	
			}
		} catch (Exception e) {
			log.error(e);
		}

		/*comboSensor.setItems(sensorList);
		if(sensorList.isEmpty()) {
			comboSensor.setValue(null);
		} else {
			comboSensor.setValue(sensorList.get(0));
		}*/
		comboSensor.setItems(sensorList);
		comboSensor.setValue(sensorAll);		
	}

	private enum DiagramType {
		STANDARD,
		HEATMAP
	}

	private static class QueryEntry {
		public final String plotID;
		public final Sensor sensor;
		public final Long startTimestamp;
		public final Long endTimestamp;
		private final DiagramType diagramType;
		public final ObjectProperty<TimestampSeries> timestampSeriesProperty;		
		public final ObjectProperty<Image>  imageProperty;
		public QueryEntry(String plotID,Sensor sensor,Long startTimestamp,Long endTimestamp, DiagramType diagramType) {
			this.plotID = plotID;
			this.sensor = sensor;
			this.timestampSeriesProperty = new SimpleObjectProperty<TimestampSeries>();
			this.imageProperty = new SimpleObjectProperty<Image>();
			this.startTimestamp = startTimestamp;
			this.endTimestamp = endTimestamp;
			this.diagramType = diagramType;
		}
	}

	private static class ScreenImageEntry {

		public final ImageView imageView;
		private QueryEntry queryEntry; 
		private ChangeListener<Image> imageChangeListener;

		public ScreenImageEntry(ImageView imageView) {
			this.imageView = imageView;
			this.queryEntry = null;
			this.imageChangeListener = null;
		}

		public void setQueryEntry(QueryEntry queryEntryNew) {
			if(queryEntry!=null) {
				if(imageChangeListener!=null) {
					queryEntry.imageProperty.removeListener(imageChangeListener);
				}
				imageChangeListener = null;
			}
			this.queryEntry = queryEntryNew;
			if(queryEntryNew!=null) {
				Image check = queryEntryNew.imageProperty.get();
				if(check!=null) {
					imageView.setImage(check);
				} else {
					imageChangeListener = (s,o,image)->{
						System.out.println("************************** set image "+queryEntry.plotID+"  "+queryEntry.sensor.name);
						imageView.setImage(image);
					};			
					queryEntryNew.imageProperty.addListener(imageChangeListener);
					imageView.setImage(queryEntryNew.imageProperty.get());
				}
			} else {
				imageView.setImage(null);
			}

		}
	}

	private void onSensorChanged(ObservableValue<? extends Sensor> observable, Sensor oldValue, Sensor sensor) {
		updateSelectionQueryList();
	}

	private void onTimeChanged(ObservableValue<? extends String> observable, String oldValue, String sensor) {
		updateSelectionQueryList();
	}

	private void onDiagramTypeChanged(ObservableValue<? extends DiagramType> observable, DiagramType oldValue, DiagramType sensor) {
		updateSelectionQueryList();
	}

	@Override
	protected void onClose() {
		executor.clear();
	}

	void updateSelectionQueryList() {
		selectionQueryList.clear();

		PlotInfo selectionPlot = comboPlot.getValue();
		Sensor sensor = comboSensor.getValue();
		String timeText = comboTime.getValue();
		if(selectionPlot==null||sensor==null||timeText==null) {
			selectedCountProperty.set("?");	
			return;
		}
		DiagramType diagramType = comboView.getValue();


		try {
			ArrayList<PlotInfo> selectedPlotList = new ArrayList<PlotInfo>();

			if(selectionPlot.name.equals(plotAll.name)) {
				for(PlotInfo item:comboPlot.getItems()) {
					if(!item.name.equals(plotAll.name)) {
						selectedPlotList.add(item);
					}
				}			
			} else {
				selectedPlotList.add(selectionPlot);
			}

			HashMap<String,Sensor> selectedSensorMap = new HashMap<String,Sensor>();

			if(sensor.name.equals(sensorAll.name)) {
				for(Sensor item:comboSensor.getItems()) {
					if(!item.name.equals(sensorAll.name)) {
						selectedSensorMap.put(item.name, item);
					}
				}
			} else {
				selectedSensorMap.put(sensor.name, sensor);
			}

			Long startTimestamp = null;
			Long endTimestamp = null;


			if(!timeText.equals(timeAll)) {
				int year = Integer.parseInt(timeText);
				startTimestamp = TimeUtil.ofDateStartHour(year);
				endTimestamp = TimeUtil.ofDateEndHour(year);
			}


			for(PlotInfo plot:selectedPlotList) {
				String[] plotSensorNames;

				plotSensorNames = tsdb.getSensorNamesOfPlot(plot.name);

				if(plotSensorNames!=null) {
					for(String sensorName:plotSensorNames) {
						if(selectedSensorMap.containsKey(sensorName)) {
							sensor = sensorMap.get(sensorName);
							selectionQueryList.add(new QueryEntry(plot.name, sensor,startTimestamp,endTimestamp,diagramType));
						}
					}
				}		
			}

		} catch (RemoteException e) {
			log.error(e);
		}

		selectedCountProperty.set(""+selectionQueryList.size());		
	}
}
