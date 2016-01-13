package tsdb.explorer;

import static tsdb.util.AssumptionCheck.throwNull;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
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
import tsdb.util.Interval;
import tsdb.util.TimeUtil;
import tsdb.util.TimeSeriesMask;
import tsdb.util.gui.TimeSeriesDiagram;
import tsdb.util.gui.TimeSeriesPainterGraphics2D;
import tsdb.util.iterator.TimestampSeries;

/**
 * View of one time series
 * @author woellauer
 *
 */
public class TimeSeriesViewScene extends TsdbScene {
	private static final Logger log = LogManager.getLogger();	
	private final RemoteTsDB tsdb;

	private ImageView imageView;
	//private Canvas canvas;
	private ContextMenu imageViewContextMenu;

	private StackPane stackPane;

	private ObjectProperty<TimeSeriesDiagram> timeSeriesDiagramProperty;
	private ObjectProperty<Image> imageProperty;

	private static final Region regionAll = new Region("[all]","[all]");
	private static final GeneralStationInfo GeneralStationAll = new GeneralStationInfo("[all]", "[?]");
	private static final String timeAll = "[all]";

	private float zoomFactorTime = 1;
	private float zoomFactorValue = 1;

	private ComboBox<Region> comboRegion;
	private ComboBox<GeneralStationInfo> comboGeneralStation;
	private ComboBox<PlotInfo> comboPlot;
	private ComboBox<Sensor> comboSensor;

	private Group overlayGroup;
	private Group overlayMaskGroup;

	private Double mouseStartMovePosX = null;
	private Double mouseStartMovePosY = null;
	private ComboBox<String> comboTime;
	private ComboBox<AggregationInterval> comboAggregation;
	private ComboBox<DataQuality> comboQuality;
	//private Line line;

	private SimpleBooleanProperty autoFitProperty;


	private enum SelectionState {NO,SELECT_START,SELECT_END};
	private ObjectProperty<SelectionState> selectionStateProperty;
	private Rectangle selectionRect;
	private long selectionStartTimestamp;
	private long selectionEndTimestamp;

	private static class MaskEntry {
		public final Interval interval;
		public final Rectangle maskRect;
		public MaskEntry(Interval interval, Rectangle maskRect) {
			this.interval = interval;
			this.maskRect = maskRect;
		}

		@Override
		public String toString() {
			return interval+"  "+maskRect;
		}
	}

	private TimeSeriesMask timeSeriesMask;
	private ArrayList<MaskEntry> maskRectList;

	public TimeSeriesViewScene(RemoteTsDB tsdb) {
		super("time series view");		
		throwNull(tsdb);
		this.tsdb = tsdb;
		this.timeSeriesMask = new TimeSeriesMask();
		this.maskRectList = new ArrayList<MaskEntry>();
	}

	private void recreateMaskRectList() {		
		Color b = Color.CHARTREUSE;
		Color maskColor = new Color(b.getRed(), b.getGreen(), b.getBlue(), 0.5d);
		overlayMaskGroup.getChildren().clear();
		for(Interval interval:timeSeriesMask.getIntervals()) {
			Rectangle maskRect = new Rectangle();
			maskRect.setFill(maskColor);
			overlayMaskGroup.getChildren().add(maskRect);			
			maskRectList.add(new MaskEntry(interval, maskRect));
		}		
		updateMaskOverlay();		
	}

	@Override
	protected Parent createContent() {
		autoFitProperty = new SimpleBooleanProperty();

		selectionStateProperty = new SimpleObjectProperty<SelectionState>(SelectionState.NO); 

		MenuItem menuItemResetView = new MenuItem("reset view");
		menuItemResetView.setOnAction(e->{
			zoomFactorTime = 1;
			zoomFactorValue = 1;
			TimeSeriesDiagram tsd = timeSeriesDiagramProperty.getValue();
			if(tsd!=null) {
				tsd.setDiagramTimestampRange(tsd.getDataMinTimestamp(), tsd.getDataMaxTimestamp());
				tsd.setDiagramValueRange(tsd.getDataMinValue(), tsd.getDataMaxValue());
				createImage();
			}
		});
		MenuItem menuItemFitValues = new MenuItem("fit values");
		menuItemFitValues.setOnAction(e->{
			TimeSeriesDiagram tsd = timeSeriesDiagramProperty.getValue();
			tsd.fitDiagramValueRangeToDiagramTimestampRange();
			createImage();
		});
		CheckMenuItem menuItemAutoFitValue = new CheckMenuItem("auto fit values");
		//log.info("cc autoFitProperty: "+autoFitProperty);
		autoFitProperty.bind(menuItemAutoFitValue.selectedProperty());	

		menuItemFitValues.disableProperty().bind(autoFitProperty);
		autoFitProperty.addListener((s,o,autoFit)->{
			if(autoFit) {
				TimeSeriesDiagram tsd = timeSeriesDiagramProperty.getValue();
				if(tsd!=null) {
					tsd.fitDiagramValueRangeToDiagramTimestampRange();
				}
				createImage();
			}
		});

		MenuItem menuItemSave = new MenuItem("save time series to file");
		menuItemSave.setOnAction(e->{
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("save time series to file");
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("csv file", "*.csv"));
			File selectedFile = fileChooser.showSaveDialog(stage);
			if (selectedFile != null) {
				try {
					TimeSeriesDiagram tsd = timeSeriesDiagramProperty.get();
					if(tsd!=null) {
						tsd.getTimeStampSeries().tsIterator().writeCSV(selectedFile.getPath());
					}
				} catch(Exception exception) {
					log.error(exception);
				}
				//mainStage.display(selectedFile);
			}
		});
		
		MenuItem menuItemSaveMask = new MenuItem("save mask");
		menuItemSaveMask.setOnAction(e->{
			TimeSeriesDiagram ts = timeSeriesDiagramProperty.get();
			if(ts!=null&&timeSeriesMask!=null&&!timeSeriesMask.isEmpty()) {			
				try {
				tsdb.setTimeSeriesMask(ts.getTimeStampSeries().name, ts.getTimeStampSeries().sensorNames[0], timeSeriesMask);
				} catch(Exception exception) {
					log.error(exception);
				}
			}
		});
		
		

		imageViewContextMenu = new ContextMenu(menuItemResetView,menuItemFitValues,menuItemAutoFitValue,menuItemSave,menuItemSaveMask);
		imageViewContextMenu.setAutoHide(true);

		stackPane = new StackPane();
		stackPane.setAlignment(Pos.TOP_LEFT);
		imageView = new ImageView();
		imageView.setCursor(Cursor.HAND);
		stackPane.getChildren().add(imageView);
		Line line0 = new Line(0,0,100,100);
		line0.setStroke(Color.BEIGE);
		Line line1 = new Line(100,100,200,200);
		line1.setStroke(Color.AQUA);

		Color b = Color.BURLYWOOD;
		Color selectionColor = new Color(b.getRed(), b.getGreen(), b.getBlue(), 0.5d);
		selectionRect = new Rectangle();
		selectionRect.setFill(selectionColor);

		//stackPane.getChildren().add(line);


		overlayGroup = new Group();
		//group.setLayoutX(0);
		//group.setLayoutY(0);
		///group.getChildren().addAll(line0,line1,selectionLineStart,selectionRect);
		overlayMaskGroup = new Group();
		overlayGroup.getChildren().addAll(new Line(0,0,0,0),selectionRect,overlayMaskGroup);
		stackPane.getChildren().add(overlayGroup);

		/*Line line2 = new Line(0,0,0,0);
		line2.setStroke(Color.ALICEBLUE);
		group.getChildren().add(line2);*/


		//group.getChildren().add(line);
		//stackPane.getChildren().add(line);*/


		stackPane.widthProperty().addListener(x->createImage());
		stackPane.heightProperty().addListener(x->createImage());
		stackPane.setMinSize(0, 0); //!! for auto resize
		stackPane.setOnScroll(this::onScroll);
		stackPane.setOnMouseMoved(this::onMouseMoved);
		stackPane.setOnMouseDragged(this::onMouseDragged);
		stackPane.setOnMousePressed(this::onMousePressed);
		stackPane.setOnMouseClicked(this::onMouseClicked);





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


		Label labelAggregation = new Label("Aggregation");
		labelAggregation.setAlignment(Pos.CENTER);
		labelAggregation.setMaxHeight(100d);
		comboAggregation = new ComboBox<AggregationInterval>();
		comboAggregation.valueProperty().addListener(this::onAggregationChanged);


		Label labelQuality = new Label("Quality");
		labelQuality.setAlignment(Pos.CENTER);
		labelQuality.setMaxHeight(100d);
		comboQuality = new ComboBox<DataQuality>();
		comboQuality.valueProperty().addListener(this::onQualityChanged);
		//comboQuality.disableProperty().bind(comboAggregation.valueProperty().isEqualTo(AggregationInterval.RAW));


		FlowPane top = new FlowPane();
		top.setHgap(30d);
		top.setVgap(3d);
		top.getChildren().add(new HBox(10d,labelRegion,comboRegion,new Separator(Orientation.VERTICAL)));
		top.getChildren().add(new HBox(10d,labelGeneralStation,comboGeneralStation,new Separator(Orientation.VERTICAL)));
		top.getChildren().add(new HBox(10d,labelPlot,comboPlot,new Separator(Orientation.VERTICAL)));
		top.getChildren().add(new HBox(10d,labelSensor,comboSensor,new Separator(Orientation.VERTICAL)));
		top.getChildren().add(new HBox(10d,labelTime,comboTime,new Separator(Orientation.VERTICAL)));
		top.getChildren().add(new HBox(10d,labelAggregation,comboAggregation,new Separator(Orientation.VERTICAL)));
		HBox boxQuality = new HBox(10d,labelQuality,comboQuality,new Separator(Orientation.VERTICAL));
		boxQuality .disableProperty().bind(comboAggregation.valueProperty().isEqualTo(AggregationInterval.RAW));
		top.getChildren().add(boxQuality);
		Node center = stackPane;
		Node bottom = new Label("ready");
		Node left = null;
		Node right = null;
		ScrollPane sc = new ScrollPane(top);
		//sc.setFitToHeight(true); // no
		sc.setFitToWidth(true);
		sc.prefHeightProperty().bind(top.heightProperty().add(10));

		timeSeriesDiagramProperty = new SimpleObjectProperty<TimeSeriesDiagram>();
		timeSeriesDiagramProperty.addListener(o->{
			zoomFactorTime=1;
			createImage();
			recreateMaskRectList();
		});

		imageProperty = new SimpleObjectProperty<Image>();
		imageProperty.addListener(o->{
			repaint();
		});

		//log.info("overlayGroup "+overlayGroup.getChildren().size());

		return new BorderPane(center, sc, right, bottom, left);
	}

	private void createImage() {
		//imageView.setImage(null);
		double width = stackPane.getWidth();
		double height = stackPane.getHeight();
		//System.out.println(width+"   "+height);
		if(width<10d||height<10d) {
			imageProperty.set(null);
			return;

		}
		if(timeSeriesDiagramProperty.get()==null) {
			imageProperty.set(null);
			return;
		}
		try {
			BufferedImage bufferedImage = new BufferedImage((int)width,(int)height,java.awt.image.BufferedImage.TYPE_INT_RGB);
			Graphics2D gc = bufferedImage.createGraphics();
			gc.setBackground(new java.awt.Color(255, 255, 255));
			gc.setColor(new java.awt.Color(0, 0, 0));
			gc.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
			gc.dispose();
			TimeSeriesPainterGraphics2D tsp = new TimeSeriesPainterGraphics2D(bufferedImage);
			timeSeriesDiagramProperty.get().draw(tsp);
			WritableImage image = SwingFXUtils.toFXImage(bufferedImage, null);
			imageProperty.set(image);
		} catch (Exception e) {
			log.error(e);
			imageProperty.set(null);
		}		
	}

	private void repaint() {
		imageView.setImage(imageProperty.get());
		updateMaskOverlay();
		updateSelectionOverlay();
	}

	private void updateMaskOverlay() {
		TimeSeriesDiagram tsd = timeSeriesDiagramProperty.get();
		if(tsd!=null) {
			float[] range = tsd.getDiagramXMinMax();
			for(MaskEntry maskEntry:maskRectList) {
				int startX = tsd.calcDiagramX(maskEntry.interval.start);
				int endX = tsd.calcDiagramX(maskEntry.interval.end+1)-1;

				if(endX<range[0] || range[1]<startX) {
					maskEntry.maskRect.setVisible(false);
				} else {
					if(startX<range[0]) {
						startX = (int) range[0];
					}
					if(range[1]<endX) {
						endX = (int) range[1];
					}
					maskEntry.maskRect.setX(startX);
					maskEntry.maskRect.setWidth(endX-startX);
					maskEntry.maskRect.setHeight(stackPane.getHeight());
					maskEntry.maskRect.setVisible(true);
				}
			}
		}
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
					plotList.addAll(plotInfos);
				} else {
					for(PlotInfo plot:plotInfos) {
						if(plot.generalStationInfo.region.name.equals(region.name)) {
							plotList.add(plot);
						}
					}
				}
			} else {
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
		if(plotList.isEmpty()) {
			comboPlot.setValue(null);
		} else {
			comboPlot.setValue(plotList.get(0));
		}
	}

	private void onUpdateTimestampSeries() {
		TimeSeriesDiagram  tsd = null;

		PlotInfo plot = comboPlot.getValue();

		if(plot==null) {
			timeSeriesDiagramProperty.setValue(null);
			return;
		}

		Sensor sensor = comboSensor.getValue();		
		if(sensor==null) {
			timeSeriesDiagramProperty.setValue(null);
			return;
		}
		

		Long startTimestamp = null;
		Long endTimestamp = null;		

		String timeText = comboTime.getValue();
		if(timeText==null) {
			timeSeriesDiagramProperty.setValue(null);
			return;
		}
		if(!timeText.equals(timeAll)) {
			int year = Integer.parseInt(timeText);
			startTimestamp = TimeUtil.ofDateStartHour(year);
			endTimestamp = TimeUtil.ofDateEndHour(year);
		}

		AggregationInterval agg = comboAggregation.getValue();
		if(agg==null) {
			timeSeriesDiagramProperty.setValue(null);
			return;
		}

		String[] sensorNames;
		if(sensor.name.equals("WD")&&agg!=AggregationInterval.RAW) {
			sensorNames = new String[]{sensor.name,"WV"};
		} else {
			sensorNames = new String[]{sensor.name};
		}

		DataQuality quality = comboQuality.getValue();
		if(quality==null) {
			timeSeriesDiagramProperty.setValue(null);
			return;
		}


		try {			
			TimestampSeries ts = tsdb.plot(null, plot.name, sensorNames, agg, quality, false, startTimestamp, endTimestamp);
			log.info(plot.name+"    update sensor "+sensor.name+"  "+ts);
			if(ts!=null) {
				tsd = new TimeSeriesDiagram(ts,agg,sensor.category,false);
				try {
					timeSeriesMask = tsdb.getTimeSeriesMask(plot.name, sensorNames[0]);
					if(timeSeriesMask==null) {
						timeSeriesMask = new TimeSeriesMask();
					}
				} catch (Exception e) {
					log.error(e);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}

		timeSeriesDiagramProperty.setValue(tsd);
	}

	private void onPlotChanged(ObservableValue<? extends PlotInfo> observable, PlotInfo oldValue, PlotInfo plot) {
		onUpdateSensor();

		onUpdateTimestampSeries();
	}

	private void onUpdateSensor() {
		ObservableList<Sensor> sensorList = FXCollections.observableArrayList();

		try {
			PlotInfo plot = comboPlot.getValue();
			AggregationInterval agg = comboAggregation.getValue();
			if(plot!=null && agg!=null) {

				Sensor[] sensors = tsdb.getSensors();
				Map<String,Sensor> sensorMap = new HashMap<String,Sensor>();
				for(Sensor sensor:sensors) {
					sensorMap.put(sensor.name, sensor);
				}

				String[] sensorNames = tsdb.getSensorNamesOfPlot(plot.name);
				log.info(plot.name+" : "+Arrays.toString(sensorNames));
				for(String sensorName:sensorNames) {
					Sensor sensor = sensorMap.get(sensorName);
					if(sensor==null) {
						sensor = new Sensor(sensorName);
					}
					if(sensor.isAggregable() || agg==AggregationInterval.RAW) {
						sensorList.addAll(sensor);
					}
				}
			}
		} catch (Exception e) {
			log.error(e);
		}

		comboSensor.setItems(sensorList);
		if(sensorList.isEmpty()) {
			comboSensor.setValue(null);
		} else {
			comboSensor.setValue(sensorList.get(0));
		}
	}

	private void onSensorChanged(ObservableValue<? extends Sensor> observable, Sensor oldValue, Sensor sensor) {
		onUpdateTimestampSeries();
	}

	private void onTimeChanged(ObservableValue<? extends String> observable, String oldValue, String sensor) {
		onUpdateTimestampSeries();
	}

	private void onAggregationChanged(ObservableValue<? extends AggregationInterval> observable, AggregationInterval oldValue, AggregationInterval sensor) {
		onUpdateSensor();
		onUpdateTimestampSeries();
	}

	private void onQualityChanged(ObservableValue<? extends DataQuality> observable, DataQuality oldValue, DataQuality quality) {
		onUpdateTimestampSeries();
	}

	private void onMousePressed(MouseEvent event) {
		mouseStartMovePosX  = event.getX();
		mouseStartMovePosY  = event.getY();
	}

	private void updateSelectionStartTimestamp(double startX) {
		TimeSeriesDiagram tsd = timeSeriesDiagramProperty.get();
		if(tsd!=null) {			
			selectionStartTimestamp = tsd.calcTimestamp(startX);
			selectionStartTimestamp -= selectionStartTimestamp%60;			
		} else {
			selectionStartTimestamp = -1;
		}
	}

	private void updateSelectionEndTimestamp(double endX) {
		TimeSeriesDiagram tsd = timeSeriesDiagramProperty.get();
		if(tsd!=null) {			
			selectionEndTimestamp = tsd.calcTimestamp(endX);
			selectionEndTimestamp -= selectionEndTimestamp%60;
			selectionEndTimestamp += 59;
		} else {
			selectionEndTimestamp = -1;
		}
		if(selectionEndTimestamp<selectionStartTimestamp) {
			selectionEndTimestamp = selectionStartTimestamp + 59;
		}
	}

	private void updateSelectionOverlay() {
		TimeSeriesDiagram tsd = timeSeriesDiagramProperty.get();
		if(tsd!=null) {			
			switch(selectionStateProperty.get()) {
			case NO:{
				selectionRect.setVisible(false);
				break;
			}
			case SELECT_START:{				
				int startX = tsd.calcDiagramX(selectionStartTimestamp);
				int endX = tsd.calcDiagramX(selectionStartTimestamp+59+1)-1;
				if(endX<0) {
					startX = 0;
					endX = 0;
				}
				if(startX<0) {
					startX=0;
				}
				int width = endX-startX;
				if(width<1) {
					width = 1;
				}
				selectionRect.setX(startX);
				selectionRect.setWidth(width);
				selectionRect.setY(0);
				selectionRect.setHeight(stackPane.getHeight());
				selectionRect.setVisible(true);
				break;
			}
			case SELECT_END:{
				int startX = tsd.calcDiagramX(selectionStartTimestamp);
				int endX = tsd.calcDiagramX(selectionEndTimestamp+1)-1;
				if(endX<0) {
					startX = 0;
					endX = 0;
				}
				if(startX<0) {
					startX=0;
				}
				int width = endX-startX;
				if(width<1) {
					width = 1;
				}
				selectionRect.setX(startX);
				selectionRect.setWidth(width);
				selectionRect.setY(0);
				selectionRect.setHeight(stackPane.getHeight());
				selectionRect.setVisible(true);				
				break;
			}
			default:{
				log.error("unknown modus: "+selectionStateProperty.get());
			}
			}
		} else {
			selectionRect.setVisible(false);
		}
	}

	private void onAlwaysMouseMoved(MouseEvent event) {
		switch(selectionStateProperty.get()) {
		case NO:{
			break;
		}
		case SELECT_START:{				
			updateSelectionStartTimestamp(event.getX());
			break;
		}
		case SELECT_END:{
			updateSelectionEndTimestamp(event.getX());
			break;
		}
		default:{
			log.error("unknown modus: "+selectionStateProperty.get());
		}
		}
		updateSelectionOverlay();		
	}

	private void onMouseMoved(MouseEvent event) {
		onAlwaysMouseMoved(event);
	}

	private void onMouseDragged(MouseEvent event) {
		onAlwaysMouseMoved(event);


		/*line.setStartX(event.getX());
		line.setStartY(event.getY());
		line.setEndX(event.getX()+100);
		line.setEndY(event.getY()+100);

		System.out.println("mouse "+event.getX()+"  "+event.getY());*/

		if(true) { // time line direction

			double currentMovePosX = event.getX();

			TimeSeriesDiagram tsd = timeSeriesDiagramProperty.getValue();
			long timestampOffset = tsd.calcTimestamp((float) (currentMovePosX-mouseStartMovePosX))-tsd.calcTimestamp(0f);;
			//log.info("MouseDragged "+(currentMovePosX-mouseStartMovePosX)+"    "+timestampOffset+"   "+TimeConverter.oleMinutesToText(timestampOffset));
			mouseStartMovePosX = currentMovePosX;
			double prevDiagramMin = tsd.getDiagramMinTimestamp();
			double prevDiagramMax = tsd.getDiagramMaxTimestamp();
			long diagramMin = (long) (prevDiagramMin - timestampOffset);
			long diagramMax = (long) (prevDiagramMax - timestampOffset);
			long min = tsd.getDataMinTimestamp();
			long max = tsd.getDataMaxTimestamp();
			if(diagramMin<min) {
				long corr = min-diagramMin;
				diagramMin += corr;
				diagramMax += corr;
				if(diagramMax>max) {
					diagramMax = max;
				}
			} else if(diagramMax>max) {
				long corr = diagramMax-max;
				diagramMin -= corr;
				diagramMax -= corr;
				if(diagramMin<min) {
					diagramMin = min;
				}
			}

			tsd.setDiagramTimestampRange(diagramMin, diagramMax);

		}


		if(autoFitProperty.get()) { // autofit
			TimeSeriesDiagram tsd = timeSeriesDiagramProperty.getValue();
			if(tsd!=null) {
				tsd.fitDiagramValueRangeToDiagramTimestampRange();
			}
		}

		if(!autoFitProperty.get()/*true*//*event.isShiftDown()*/) { // value line direction

			double currentMovePosY = event.getY();
			TimeSeriesDiagram tsd = timeSeriesDiagramProperty.getValue();
			double valueOffset = tsd.calcValue((double) (currentMovePosY-mouseStartMovePosY))-tsd.calcValue(0d);
			mouseStartMovePosY = currentMovePosY;
			double prevDiagramMin = tsd.getDiagramMinValue();
			double prevDiagramMax = tsd.getDiagramMaxValue();
			double diagramMin = prevDiagramMin - valueOffset;
			double diagramMax = prevDiagramMax - valueOffset;
			float min = tsd.getDataMinValue();
			float max = tsd.getDataMaxValue();
			if(diagramMin<min) {
				double corr = min-diagramMin;
				diagramMin += corr;
				diagramMax += corr;
				if(diagramMax>max) {
					diagramMax = max;
				}
			} else if(diagramMax>max) {
				double corr = diagramMax-max;
				diagramMin -= corr;
				diagramMax -= corr;
				if(diagramMin<min) {
					diagramMin = min;
				}
			}
			tsd.setDiagramValueRange(diagramMin, diagramMax);
		}

		createImage();		
	}

	private void onMouseClicked(MouseEvent event) {
		if(event.getButton()==MouseButton.SECONDARY) {
			imageViewContextMenu.show(imageView, event.getSceneX(), event.getSceneY());
		} else {
			imageViewContextMenu.hide();			
		}

		if(event.getButton()==MouseButton.MIDDLE) {
			onMouseMiddleClicked(event);
		}
	}

	private void onMouseMiddleClicked(MouseEvent event) {

		switch(selectionStateProperty.get()) {
		case NO:
			selectionStateProperty.set(SelectionState.SELECT_START);
			updateSelectionStartTimestamp(event.getX());
			updateSelectionOverlay();
			break;
		case SELECT_START:
			updateSelectionStartTimestamp(event.getX());
			selectionStateProperty.set(SelectionState.SELECT_END);
			updateSelectionEndTimestamp(event.getX());
			updateSelectionOverlay();
			break;
		case SELECT_END:
			updateSelectionEndTimestamp(event.getX());

			timeSeriesMask.addInterval(Interval.of((int)selectionStartTimestamp, (int)selectionEndTimestamp));

			/*Color b = Color.CHARTREUSE;
			Color maskColor = new Color(b.getRed(), b.getGreen(), b.getBlue(), 0.5d);
			Rectangle maskRect = new Rectangle();
			maskRect.setFill(maskColor);
			overlayGroup.getChildren().add(maskRect);			
			maskRectList.add(MaskEntry.of((int)selectionStartTimestamp, (int)selectionEndTimestamp, maskRect));			
			updateMaskOverlay();*/			

			selectionStateProperty.set(SelectionState.NO);
			//updateSelectionOverlay();
			recreateMaskRectList();
			break;
		default:
			log.error("error");
		}






		/*
		System.out.println("*********************");
		if(selectionStateProperty.get()==SelectionState.NO) {
			selectionStateProperty.set(SelectionState.SELECT_START);
			updateSelectionStartTimestamp(event.getX());
			updateSelectionOverlay();
		} else if(selectionStateProperty.get()==SelectionState.SELECT_START) {





			TimeSeriesDiagram tsd = timeSeriesDiagramProperty.get();
			if(tsd!=null) {
				selectionStartTimestamp = tsd.calcTimestamp(event.getX());
			}
			selectionStateProperty.set(SelectionState.SELECT_END);
			onAlwaysMouseMoved(event);
		}  else if(selectionStateProperty.get()==SelectionState.SELECT_END) {
			TimeSeriesDiagram tsd = timeSeriesDiagramProperty.get();
			if(tsd!=null) {
				selectionStateProperty.set(SelectionState.NO);

				Color b = Color.CHARTREUSE;
				Color maskColor = new Color(b.getRed(), b.getGreen(), b.getBlue(), 0.5d);

				Rectangle maskRect = new Rectangle(selectionRect.getX(),selectionRect.getY(),selectionRect.getWidth(),selectionRect.getHeight());
				System.out.println("maskRect "+maskRect);
				maskRect.setFill(maskColor);
				System.out.println("overlayGroup "+overlayGroup.getChildren().size());
				overlayGroup.getChildren().add(maskRect);
				System.out.println("overlayGroup "+overlayGroup.getChildren().size());

				long selectionEndTimestamp = tsd.calcTimestamp(event.getX());

				maskList.add(new MaskEntry(selectionStartTimestamp, selectionEndTimestamp, maskRect));			
				onAlwaysMouseMoved(event);
				updateMaskOverlay();
			}
		}
		System.out.println(selectionStateProperty.get());*/
	}

	private void onScroll(ScrollEvent event) {
		TimeSeriesDiagram tsd = timeSeriesDiagramProperty.getValue();
		if(tsd==null) {
			return;
		}

		if(!event.isShiftDown()) { // zoom time line
			double zoom = event.getDeltaY();
			if(zoom<0) {
				zoomFactorTime*=1.25;	
			} else {				
				zoomFactorTime/=1.25;
				if(zoomFactorTime<1) {
					zoomFactorTime = 1;
				}
			}

			long min = tsd.getDataMinTimestamp();
			long max = tsd.getDataMaxTimestamp();
			double prevDiagramMin = tsd.getDiagramMinTimestamp();
			double prevDiagramMax = tsd.getDiagramMaxTimestamp();
			long posTimestamp = tsd.calcTimestamp((float) event.getSceneX());
			if(posTimestamp<min) {
				posTimestamp = min;
			}
			if(posTimestamp>max) {
				posTimestamp = max;
			}

			float rangeFactor = ((float)(posTimestamp-prevDiagramMin))/((float)(prevDiagramMax-prevDiagramMin));

			long zoomedTimestampRange = (long) ((max-min)/zoomFactorTime);
			double zoomedMin = posTimestamp - zoomedTimestampRange*(rangeFactor);
			double zoomedMax = zoomedMin+zoomedTimestampRange;

			if(zoomedMin<min) {
				zoomedMin = min;
			}
			if(zoomedMax>max) {
				zoomedMax = max;
			}

			tsd.setDiagramTimestampRange(zoomedMin, zoomedMax);
			if(autoFitProperty.get()) {
				tsd.fitDiagramValueRangeToDiagramTimestampRange();
			}
		} else if(!autoFitProperty.get()){ //zoom value line
			double zoom = event.getDeltaY();
			if(zoom<0) {
				zoomFactorValue*=1.25;	
			} else {				
				zoomFactorValue/=1.25;
				if(zoomFactorValue<1) {
					zoomFactorValue = 1;
				}
			}

			float min = tsd.getDataMinValue();
			float max = tsd.getDataMaxValue();
			double prevDiagramMin = tsd.getDiagramMinValue();
			double prevDiagramMax = tsd.getDiagramMaxValue();
			double posValue = tsd.calcValue(event.getY());
			if(posValue<min) {
				posValue = min;
			}
			if(posValue>max) {
				posValue = max;
			}

			float rangeFactor = ((float)(posValue-prevDiagramMin))/((float)(prevDiagramMax-prevDiagramMin));
			float zoomedValueRange = (max-min)/zoomFactorValue;
			double zoomedMin = posValue - zoomedValueRange*(rangeFactor);
			double zoomedMax = zoomedMin+zoomedValueRange;

			if(zoomedMin<min) {
				zoomedMin = min;
			}
			if(zoomedMax>max) {
				zoomedMax = max;
			}

			tsd.setDiagramValueRange(zoomedMin, zoomedMax);
		}
		createImage();
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

		ObservableList<AggregationInterval> aggregationList = FXCollections.observableArrayList();
		aggregationList.add(AggregationInterval.RAW);
		aggregationList.add(AggregationInterval.HOUR);
		aggregationList.add(AggregationInterval.DAY);
		aggregationList.add(AggregationInterval.WEEK);
		aggregationList.add(AggregationInterval.MONTH);
		aggregationList.add(AggregationInterval.YEAR);
		comboAggregation.setItems(aggregationList);
		comboAggregation.setValue(AggregationInterval.HOUR);

		ObservableList<DataQuality> qualityList = FXCollections.observableArrayList();
		qualityList.add(DataQuality.NO);
		qualityList.add(DataQuality.PHYSICAL);
		qualityList.add(DataQuality.STEP);
		qualityList.add(DataQuality.EMPIRICAL);
		comboQuality.setItems(qualityList);
		comboQuality.setValue(DataQuality.STEP);

		setRegions();
	}

	public boolean setSelection(String plotID, String sensorName) {
		if(plotID==null) {
			return false;
		}
		if(sensorName==null) {
			return false;
		}
		ObservableList<PlotInfo> plotItems = comboPlot.getItems();
		if(plotItems==null) {
			return false;
		}		
		PlotInfo plotInfo = null;
		for(PlotInfo item:plotItems) {
			if(item.name.equals(plotID)) {
				plotInfo = item;
				break;
			}
		}
		if(plotInfo==null) {
			return false;
		}
		comboPlot.setValue(plotInfo);

		ObservableList<Sensor> sensorItems = comboSensor.getItems();
		if(sensorItems==null) {
			return false;
		}
		Sensor sensor = null;
		for(Sensor item:sensorItems) {
			if(item.name.equals(sensorName)) {
				sensor = item;
				break;
			}
		}
		if(sensor==null) {
			return false;
		}
		comboSensor.setValue(sensor);

		return true;
	}
}

