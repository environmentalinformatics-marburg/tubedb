package tsdb.explorer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.javafx.binding.ObjectConstant;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import tsdb.StationProperties;
import tsdb.component.Region;
import tsdb.component.SourceEntry;
import tsdb.remote.RemoteTsDB;
import tsdb.remote.StationInfo;
import tsdb.remote.VirtualPlotInfo;
import tsdb.util.Interval;
import tsdb.util.TimeUtil;
import tsdb.util.TimestampInterval;
import tsdb.util.TsSchema;

/**
 * View of SourceCatalog
 * @author woellauer
 *
 */
public class SourceCatalogScene extends TsdbScene {
	private static final Logger log = LogManager.getLogger();

	private final RemoteTsDB tsdb;

	private ArrayList<SourceItem> sourceItemList;
	private FilteredList<SourceItem> filteredList;
	private Region[] regions;

	private ComboBox<Region> comboRegion;
	private ComboBox<String> comboGeneralStation;
	private ComboBox<String> comboPlot;
	private ComboBox<Integer> comboYear;
	private Label labelMonth;
	private ComboBox<Integer> comboMonth;
	private TextField txtSensor;

	private TableView<SourceItem> table;
	private Label lblPlaceHolder;

	private Label lblStatus;

	private final Region regionAll = new Region("[all]","[all]");

	public SourceCatalogScene(RemoteTsDB tsdb) {
		super("source catalog");
		this.tsdb = tsdb;
	}

	private static <S, T> Callback<TableColumn<S,T>, TableCell<S,T>> createCellFactory(Callback<T,String> converter) {
		return param -> new TableCell<S, T>(){
			@Override
			protected void updateItem(T item, boolean empty) {
				super.updateItem(item, empty);
				if(empty) {
					setText(null);
				} else {
					setText(converter.call(item));
				}
			}
		};
	}

	private void onTableClick(MouseEvent e) {
		if(e.getClickCount()==2) {
			SourceItem item = table.getSelectionModel().getSelectedItem();
			if(item==null) {
				return;
			}
			SourceEntry entry = item.sourceEntry;
			String s = entry.stationName;
			s += ", "+TimeUtil.oleMinutesToText(entry.firstTimestamp);
			s += ", "+TimeUtil.oleMinutesToText(entry.lastTimestamp);
			s += ", "+entry.getFullPath();
			s += ", "+entry.getTranslation();
			//table.getFocusModel().getFocusedCell().getColumn()			
			ClipboardContent content = new ClipboardContent();
			content.putString(s);
			Clipboard.getSystemClipboard().setContent(content);
			new SourceItemScene(item).show();	
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Parent createContent() {

		table = new TableView<SourceItem>();
		lblPlaceHolder = new Label("loading content...");
		table.setPlaceholder(lblPlaceHolder);
		table.setOnMouseClicked(this::onTableClick);
		//table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<SourceItem,String> colPlot = new TableColumn<SourceItem,String>("plot");		
		colPlot.setCellValueFactory(param->ObjectConstant.valueOf(param.getValue().plotid));
		colPlot.setComparator(String.CASE_INSENSITIVE_ORDER);

		TableColumn<SourceItem,Long> colFirst = new TableColumn<SourceItem,Long>("first");		
		colFirst.setCellValueFactory(param->ObjectConstant.valueOf(param.getValue().sourceEntry.firstTimestamp));
		colFirst.setCellFactory(createCellFactory(t->TimeUtil.oleMinutesToText(t)));
		colFirst.setMinWidth(110);

		TableColumn<SourceItem,Long> colLast = new TableColumn<SourceItem,Long>("last");		
		colLast.setCellValueFactory(param->ObjectConstant.valueOf(param.getValue().sourceEntry.lastTimestamp));
		colLast.setCellFactory(createCellFactory(t->TimeUtil.oleMinutesToText(t)));
		colLast.setMinWidth(110);

		TableColumn<SourceItem,String> colStation = new TableColumn<SourceItem,String>("station");		
		colStation.setCellValueFactory(param->ObjectConstant.valueOf(param.getValue().sourceEntry.stationName));
		colStation.setComparator(String.CASE_INSENSITIVE_ORDER);
		colStation.setMinWidth(80);

		TableColumn<SourceItem,String> colPath = new TableColumn<SourceItem,String>("path");		
		colPath.setCellValueFactory(param->ObjectConstant.valueOf(param.getValue().sourceEntry.path));
		colPath.setComparator(String.CASE_INSENSITIVE_ORDER);

		TableColumn<SourceItem,String> colFilename = new TableColumn<SourceItem,String>("filename");		
		colFilename.setCellValueFactory(param->ObjectConstant.valueOf(param.getValue().sourceEntry.filename));
		colFilename.setMinWidth(256);
		colFilename.setComparator(String.CASE_INSENSITIVE_ORDER);

		TableColumn<SourceItem,Integer> colRows = new TableColumn<SourceItem,Integer>("rows");		
		colRows.setCellValueFactory(param->ObjectConstant.valueOf(param.getValue().sourceEntry.rows));

		TableColumn<SourceItem,Integer> colTimeStep = new TableColumn<SourceItem,Integer>("time-step");		
		colTimeStep.setCellValueFactory(param->ObjectConstant.valueOf(param.getValue().sourceEntry.timeStep));
		colTimeStep.setCellFactory(createCellFactory(timestep->timestep==null||timestep==TsSchema.NO_CONSTANT_TIMESTEP?null:timestep.toString()));

		TableColumn<SourceItem,Integer> colColumns = new TableColumn<SourceItem,Integer>("columns");		
		colColumns.setCellValueFactory(param->ObjectConstant.valueOf(param.getValue().sourceEntry.headerNames.length));
		colColumns.setMinWidth(10);

		TableColumn<SourceItem,String> colHeader = new TableColumn<SourceItem,String>("header");		
		colHeader.setCellValueFactory(param->ObjectConstant.valueOf(Arrays.toString(param.getValue().sourceEntry.headerNames)));
		colHeader.setComparator(String.CASE_INSENSITIVE_ORDER);
		colHeader.setMinWidth(160);


		TableColumn<SourceItem,String> colSensors = new TableColumn<SourceItem,String>("sensors");		
		colSensors.setCellValueFactory(param->ObjectConstant.valueOf(Arrays.toString(param.getValue().sourceEntry.sensorNames)));
		colSensors.setComparator(String.CASE_INSENSITIVE_ORDER);
		colSensors.setMinWidth(160);


		TableColumn<SourceItem,String> colGeneralStation = new TableColumn<SourceItem,String>("general");		
		colGeneralStation.setCellValueFactory(param->ObjectConstant.valueOf(param.getValue().generalStationName));
		colPlot.setComparator(String.CASE_INSENSITIVE_ORDER);

		table.getColumns().setAll(colGeneralStation,colPlot,colStation,colFirst,colLast,colRows,colTimeStep,colFilename,colPath,colColumns,colHeader,colSensors);

		BorderPane mainBoderPane = new BorderPane();		
		mainBoderPane.setCenter(table);

		lblStatus = new Label("loading...");
		mainBoderPane.setBottom(lblStatus);

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


		comboGeneralStation = new ComboBox<String>();
		comboGeneralStation.valueProperty().addListener(this::onGeneralStationChanged);

		comboPlot = new ComboBox<String>();
		comboPlot.valueProperty().addListener(this::onPlotChanged);

		comboYear = new ComboBox<Integer>();
		StringConverter<Integer> yearConverter = new StringConverter<Integer>() {			
			@Override
			public String toString(Integer year) {
				return year==yearAll?"[all]":Integer.toString(year);
			}			
			@Override
			public Integer fromString(String string) {
				return null;
			}
		};
		comboYear.setConverter(yearConverter);
		comboYear.valueProperty().addListener(this::onYearChanged);

		comboMonth = new ComboBox<Integer>();
		StringConverter<Integer> monthConverter = new StringConverter<Integer>() {			
			@Override
			public String toString(Integer month) {
				switch(month) {
				case 0:
					return "[all]";
				case 1:
					return "jan";
				case 2:
					return "feb";
				case 3:
					return "mar";
				case 4:
					return "apr";
				case 5:
					return "may";
				case 6:
					return "jun";
				case 7:
					return "jul";
				case 8:
					return "aug";
				case 9:
					return "sep";
				case 10:
					return "oct";
				case 11:
					return "nov";
				case 12:
					return "dec";
				default:
					return "???";
				}

			}			
			@Override
			public Integer fromString(String string) {
				return null;
			}
		};
		comboMonth.setConverter(monthConverter);
		comboMonth.valueProperty().addListener(this::onMonthChanged);
		
		txtSensor = new TextField();
		txtSensor.setPromptText("all sensors");
		txtSensor.textProperty().addListener(this::onSensorChanged);

		HBox hBoxControl = new HBox(10d);
		hBoxControl.getChildren().add(new Label("Region"));
		hBoxControl.getChildren().add(comboRegion);
		hBoxControl.getChildren().add(new Label("General"));
		hBoxControl.getChildren().add(comboGeneralStation);
		hBoxControl.getChildren().add(new Label("Plot"));
		hBoxControl.getChildren().add(comboPlot);
		hBoxControl.getChildren().add(new Label("Year"));
		hBoxControl.getChildren().add(comboYear);
		labelMonth = new Label("Month");
		hBoxControl.getChildren().add(labelMonth);
		hBoxControl.getChildren().add(comboMonth);
		hBoxControl.getChildren().add(new Label("Sensor"));
		hBoxControl.getChildren().add(txtSensor);
		mainBoderPane.setTop(hBoxControl);
		return mainBoderPane;
	}

	@Override
	protected void onShown() {
		sourceItemList = new ArrayList<SourceItem>();

		try {			
			HashMap<String, ArrayList<SourceEntry>> stationCatalogEntryMap = new HashMap<String, ArrayList<SourceEntry>>();
			for(SourceEntry sourceEntry:tsdb.getSourceCatalogEntries()) {
				ArrayList<SourceEntry> list = stationCatalogEntryMap.get(sourceEntry.stationName);
				if(list==null) {
					list = new ArrayList<SourceEntry>();
					stationCatalogEntryMap.put(sourceEntry.stationName, list);
				}
				list.add(sourceEntry);			
			}			

			for(StationInfo stationInfo:tsdb.getStations()) {
				ArrayList<SourceEntry> sourceEntryList = stationCatalogEntryMap.get(stationInfo.stationID);
				if(sourceEntryList!=null) {
					if(stationInfo.generalStationInfo!=null) {
						for(SourceEntry sourceEntry:sourceEntryList) {
							SourceItem sourceItem = new SourceItem(sourceEntry);
							sourceItem.generalStationName = stationInfo.generalStationInfo.name;
							sourceItem.regionName = stationInfo.generalStationInfo.region.name;
							sourceItem.plotid = stationInfo.stationID;
							sourceItemList.add(sourceItem);
						}
					}				
				}
			}

			for(VirtualPlotInfo virtualPlotInfo:tsdb.getVirtualPlots()) {
				for(TimestampInterval<StationProperties> interval:virtualPlotInfo.intervalList) {
					ArrayList<SourceEntry> sourceEntryList = stationCatalogEntryMap.get(interval.value.get_serial());
					if(sourceEntryList!=null) {
						for(SourceEntry sourceEntry:sourceEntryList) {
							if(interval.contains(sourceEntry.firstTimestamp, sourceEntry.lastTimestamp)) {
								SourceItem sourceItem = new SourceItem(sourceEntry);
								sourceItem.generalStationName = virtualPlotInfo.generalStationInfo.name;
								sourceItem.regionName = virtualPlotInfo.generalStationInfo.region.name;
								sourceItem.plotid = virtualPlotInfo.plotID;
								sourceItemList.add(sourceItem);
							}
						}
					}
				}
			}
			regions = tsdb.getRegions();


			filteredList = new FilteredList<SourceItem>(FXCollections.observableArrayList(sourceItemList));
			filteredList.addListener(this::updateStatus);
			SortedList<SourceItem> sortedList = new SortedList<SourceItem>(filteredList);
			sortedList.comparatorProperty().bind(table.comparatorProperty());
			table.setItems(sortedList);

			setRegions(regions);
			setYears();
			setMonths();

			lblPlaceHolder.setText("no content");
			updateStatus(null);

		} catch (RemoteException e) {
			e.printStackTrace();
			log.error(e);
			regions = new Region[0];
		}
	}

	private void updateStatus(Observable observable) {
		lblStatus.setText(""+filteredList.size()+" entries");
	}

	private static final Integer yearAll = 0;
	private static final Integer monthAll = 0;

	private void setYears() {
		ObservableList<Integer> yearList = FXCollections.observableArrayList();
		yearList.add(yearAll);
		for(int y=2008;y<=2016;y++) {
			yearList.addAll(y);
		}
		comboYear.setItems(yearList);
		comboYear.setValue(yearAll);		
	}

	private void setMonths() {
		ObservableList<Integer> monthList = FXCollections.observableArrayList();
		monthList.add(monthAll);
		for(int y=1;y<=12;y++) {
			monthList.addAll(y);
		}
		comboMonth.setItems(monthList);
		comboMonth.setValue(monthAll);		
	}

	private void setRegions(Region[] regions) {
		ObservableList<Region> regionList = FXCollections.observableArrayList();
		regionList.add(regionAll);
		regionList.addAll(regions);
		comboRegion.setItems(regionList);
		comboRegion.setValue(regionAll);		
	}

	private void onRegionChanged(ObservableValue<? extends Region> observable, Region oldValue, Region newValue) {
		TreeSet<String> generalSet = new TreeSet<String>();
		Region region = newValue;		
		if(region==null||region.name.equals("[all]")) {
			for(SourceItem sourceItem:sourceItemList) {
				generalSet.add(sourceItem.generalStationName);
			}
		} else {		
			for(SourceItem sourceItem:sourceItemList) {
				if(sourceItem.regionName.equals(region.name)) {
					generalSet.add(sourceItem.generalStationName);
				};
			}
		}
		ObservableList<String> generals = FXCollections.observableArrayList();
		String generalAll = "[all]";
		generals.add(generalAll);
		generals.addAll(generalSet);
		comboGeneralStation.setItems(generals);
		comboGeneralStation.setValue(generalAll);

		//updateComboPlot();		
	}

	private void onGeneralStationChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		Region region = comboRegion.getValue();
		TreeSet<String> plotSet = new TreeSet<String>();
		String general = newValue;
		if(general==null||general.equals("[all]")) {
			for(SourceItem sourceItem:sourceItemList) {
				if(region==null||region.name.equals("[all]")||sourceItem.regionName.equals(region.name)) {
					plotSet.add(sourceItem.plotid);
				}
			}		
		} else {
			for(SourceItem sourceItem:sourceItemList) {
				if(sourceItem.generalStationName.equals(general)) {
					plotSet.add(sourceItem.plotid);
				}
			}	
		}

		ObservableList<String> plots = FXCollections.observableArrayList();
		String plotAll = "[all]";
		plots.add(plotAll);
		plots.addAll(plotSet);
		comboPlot.setItems(plots);
		comboPlot.setValue(plotAll);
	}

	private void onPlotChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		updateListFilter();
	}



	private void onYearChanged(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
		if(comboYear.getValue()==yearAll) {
			labelMonth.setDisable(true);
			comboMonth.setDisable(true);
			comboMonth.setValue(monthAll);
		} else {
			labelMonth.setDisable(false);
			comboMonth.setDisable(false);
		}
		updateListFilter();
	}

	private void onMonthChanged(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
		updateListFilter();
	}
	
	private void onSensorChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		updateListFilter();
	}


	private static final Predicate<SourceItem> predicateTrue = (s)->true;

	private void updateListFilter() {
		Region region = comboRegion.getValue();
		String general = comboGeneralStation.getValue();
		String plot = comboPlot.getValue();
		Integer year = comboYear.getValue();
		Integer month = comboMonth.getValue();
		String sensorPrefix = txtSensor.getText();

		Predicate<SourceItem> predicate = predicateTrue;
		if(plot!=null && !plot.equals("[all]")) {
			predicate = sourceEntry->plot.equals(sourceEntry.plotid);
		} else {
			if(general!=null && !general.equals("[all]")) {
				predicate = sourceEntry->general.equals(sourceEntry.generalStationName);
			} else {
				if(region!=null && !region.name.equals("[all]")) {
					predicate = sourceEntry->region.name.equals(sourceEntry.regionName);
				}
			}
		}

		if(year!=null && year!=yearAll) {
			if(month!=null && month!=monthAll) {
				predicate = predicate.and(new PredicateYearMonth(year, month));
			} else {
				predicate = predicate.and(new PredicateYear(year));
			}
		}
		
		if(sensorPrefix!=null && !sensorPrefix.isEmpty()) {
			predicate = predicate.and(new PredicateSensorPrefix(sensorPrefix));
		}
		
		filteredList.setPredicate(predicate);
	}

	private static class PredicateYear implements Predicate<SourceItem> {
		private final Interval yearInterval;
		public PredicateYear(Integer year) {			
			long start = TimeUtil.ofDateStartMinute(year);
			long end = TimeUtil.ofDateEndMinute(year);			
			yearInterval = Interval.of((int)start, (int)end);
		}

		@Override
		public boolean test(SourceItem s) {
			int start = (int) s.sourceEntry.firstTimestamp;
			int end = (int) s.sourceEntry.lastTimestamp;
			return yearInterval.overlaps(start, end);
		}		
	}

	private static class PredicateYearMonth implements Predicate<SourceItem> {
		private final Interval yearMonthInterval;
		public PredicateYearMonth(Integer year, Integer month) {			
			long start = TimeUtil.ofDateStartMinute(year, month);
			long end = TimeUtil.ofDateEndMinute(year, month);			
			yearMonthInterval = Interval.of((int)start, (int)end);
		}

		@Override
		public boolean test(SourceItem s) {
			int start = (int) s.sourceEntry.firstTimestamp;
			int end = (int) s.sourceEntry.lastTimestamp;
			return yearMonthInterval.overlaps(start, end);
		}		
	}
	
	private static class PredicateSensorPrefix implements Predicate<SourceItem> {
		private final String sensorPrefix;		

		public PredicateSensorPrefix(String sensorPrefix) {
			this.sensorPrefix = sensorPrefix;
		}

		@Override
		public boolean test(SourceItem t) {			
			String[] sn = t.sourceEntry.sensorNames;			
			if(sn==null || sn.length==0) {
				return false;
			}			
			for(String s:sn) {
				if(s!=null && s.startsWith(sensorPrefix)) {
					return true;
				}
			}
			return false;
		}
		
	}
}
