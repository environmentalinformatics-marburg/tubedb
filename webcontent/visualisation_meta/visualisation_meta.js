"use strict";
document.addEventListener('DOMContentLoaded', function() {init();}, false);
Vue.config.productionTip = false;

var url_api_base = "../../";
var url_region_json = url_api_base + "tsdb/region.json";
var url_metadata_json = url_api_base + "tsdb/metadata.json";
var url_query_image = url_api_base + "tsdb/query_image";
var url_query_heatmap = url_api_base + "tsdb/query_heatmap";
var url_query_csv = url_api_base + "tsdb/query_csv";

Array.prototype.contains = function (e) {
   for (var i in this) {
       if (this[i] == e) return true;
   }
   return false;
}

//Polyfill Object.assign https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/assign
if (typeof Object.assign != 'function') {
  Object.assign = function(target, varArgs) { // .length of function is 2
    'use strict';
    if (target == null) { // TypeError if undefined or null
      throw new TypeError('Cannot convert undefined or null to object');
    }

    var to = Object(target);

    for (var index = 1; index < arguments.length; index++) {
      var nextSource = arguments[index];

      if (nextSource != null) { // Skip over if undefined or null
        for (var nextKey in nextSource) {
          // Avoid bugs when hasOwnProperty is shadowed
          if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
            to[nextKey] = nextSource[nextKey];
          }
        }
      }
    }
    return to;
  };
}

function getUrlQueryParams(qs) { // based on http://stackoverflow.com/a/1099670
    qs = qs.split('+').join(' ');
    var params = {};
    var tokens;
    var re = /[?&]?([^=]+)=([^&]*)/g;
    while (tokens = re.exec(qs)) {
        params[decodeURIComponent(tokens[1])] = decodeURIComponent(tokens[2]);
    }
    return params;
}

function init() {
	
Vue.component('visualisation-interface', {
	
template: '#visualisation-template',

props: {
	project: Object,
}, //end props

data: function () {
	return {
		appMessage: "init view ...",
		back_link: true,		
		metadata: {},
		
		groupHover: false,
		groupHoverStay: false,
		groupID: "*",
		groupMap: {},
		
		plotHover: false,
		plotIDs: ["*"],
		plotMap: {},
		stationMap: {},
		plotstations: [],
		pinned_plot: undefined,
		
		sensorHover: false,
		sensorIDs: [],
		sensorMap: {},
		sensorNamePlotMap: {},
		sensorNameStationMap: {},
		default_sensor_name: "Ta_200",

		aggregationHover: false,
		aggregationHoverStay: false,
		
		aggregations: ["raw","hour","day","week","month","year"],
		aggregationsText: {raw:"raw", hour:"hours", day:"days", week:"weeks", month:"months", year:"years"},
		aggregation: "hour",
		
		timeHover: false,
		timeHoverStay: false,
		timeYear: "*",
		timeYears: [2000],
		timeMonths: ["jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec"],
		timeMonthsNumber: {"*":0, "jan":1, "feb":2, "mar":3, "apr":4, "may":5, "jun":6, "jul":7, "aug":8, "sep":9, "oct":10 ,"nov":11, "dec":12},
		timeMonth: "*",
		timeDays: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"],
		timeDay: "*",
		
		qualityHover: false,
		qualityHoverStay: false,
		qualities: ["no", "physical", "step", "empirical"],
		qualitiesText: {no: "0: no", physical: "1: physical", step: "2: step", empirical: "3: empirical"},
		quality: "step",
		
		interpolationHover: false,
		interpolation: false,
		
		viewTypeHover: false,
		viewTypeHoverStay: false,
		viewTypes: ["diagram", "heatmap", "boxplot", "table", "csv-file", "sensors", "plots"],
		viewType: "diagram",
		widthTexts: ["auto", "large", "maximum", "custom"],
		widthTextMap: {"large": 4096, "maximum": 65535},
		widthText: "auto",
		widthCustom: 1000,
		heightTexts: ["small", "medium", "large", "custom"],
		heightTextMap: {"small": 100, "medium": 400, "large": 800},
		heightText: "small",
		heightCustom: 100,
		byYear: true,
		
		views: [],
		viewsDone: 0,
		viewCycle: 0,
		viewPrecessingStart: 0,
		viewPrecessingEnd: 0,
		viewsLimited: false,	
		divContainer: undefined,
		
		table: undefined,
		filteredtableStartRow: 0,
		filteredtableMaxRowCountTexts: ["10", "20", "50", "100"],
		filteredtableMaxRowCountText: "20",
		filteredtableMaxRowCountMap: {"10": 10, "20": 20, "50": 50, "100": 100},
		filteredtableFastMoveFactor: 20,
	}
}, //end data

computed: {
	filteredtableMaxRowCount: function() {
		return this.filteredtableMaxRowCountMap[this.filteredtableMaxRowCountText];
	},
	plots: function() {
		var self = this;
		if(this.groupID === '*') {
			return this.metadata.plots;
		}
		var values = [];
		this.metadata.plots.forEach(function(o){
			if(o.general_station == self.groupID) {
				values.push(o);
			}
		});
		return values;
	},
	sensors: function() {
		console.log("update sensors");
		var self = this;
		if(this.plotIDs[0] == '*' && this.groupID == '*') {
			return this.metadata.sensors;
		} else {
			var plotIDs = [];
			if(this.plotIDs[0] == '*') {
				this.metadata.plots.forEach(function(o){
					if(o.general_station == self.groupID) {
						plotIDs.push(o.id);
					}
				});
			} else {
				plotIDs = this.plotIDs;
			}			
			var map = {};
			plotIDs.forEach(function(plotID){
				var plot = self.plotMap[plotID];
				plot.sensor_names.forEach(function(sensorName){
					map[sensorName] = true;
				});
			});
			var s = [];
				Object.keys(map).forEach(function(sensorName) {
					//console.log(sensorName+" ?");
					var sen = self.sensorMap[sensorName];
					if(sen != undefined) {
						//console.log(sen.id);
						s.push(sen);
					}
					//console.log(sensorName+" !");					
				});	
			return s;
		}
	},
	visibleSensors: function() {
		var self = this;
		var sensors;
		if(this.aggregation === "raw") {
			sensors = this.sensors;
		} else {
			sensors = this.sensors.filter(function(s){return !s.raw});
		}
		var sensorIDs;
		if(this.sensorIDs[0] === '*') {
			sensorIDs = ['*'];
		} else if(this.sensorIDs[0] === 'all_measurements') {
			sensorIDs = ['all_measurements'];
		} else if(this.sensorIDs[0] === 'all_derived') {
			sensorIDs = ['all_derived'];
		} else {
			if(sensors.length === 0) {
				sensorIDs = [];
			} else {
				sensorIDs = [];
				this.sensorIDs.forEach(function(id){
					for(var i in sensors) {
						if(sensors[i].id == id) {
							sensorIDs.push(id);
						}
					}
				});
				if(sensorIDs.length === 0) {
					if(sensors.some(function(s){return s.id===self.default_sensor_name;})) {
						sensorIDs = [self.default_sensor_name];
					} else {
						sensorIDs = [sensors[0].id];
					}
				}
			}
		}
		console.log("update sensorIDs");
		this.sensorIDs = sensorIDs;
		return sensors;
	},
	visibleNotDerivedSensors: function() { // not internal
		return this.visibleSensors.filter(function(s){return !s.internal && !s.derived});
	},
	visibleDerivedSensors: function() {  // not internal
		return this.visibleSensors.filter(function(s){return !s.internal && s.derived});
	},
	visibleInternalSensors: function() {
		return this.visibleSensors.filter(function(s){return s.internal});
	},
	timeText: function() {
		var s = this.timeYear;
		if(this.timeYear != '*' && this.timeMonth != '*') {
			s += '-'+this.timeMonth;
			if(this.timeDay != '*') {
			s += '-'+this.timeDay;
		}
		}
		return s;
	},
	timeParameters: function() {		
		if(this.timeYear === '*') {
			if(this.groupViewYearRange === undefined) {
				return {};
			} else {
				return {year: this.groupViewYearRange.start, end_year: this.groupViewYearRange.end};
			}
		}
		if(this.timeMonth === '*') {
			return {year: this.timeYear};
		}
		if(this.timeDay === '*') {
			return {year: this.timeYear, month: this.timeMonthsNumber[this.timeMonth]};
		}
		console.log("day "+this.timeDay);
		return {year: this.timeYear, month: this.timeMonthsNumber[this.timeMonth], day: this.timeDay};
	},
	groupViewYearRange: function() {
		if(this.groupID === '*') {
			if(this.metadata.region === undefined || this.metadata.region.view_year_range === undefined) {
				return undefined;
			} else {
				return this.metadata.region.view_year_range;
			}
		} else {
			var group = this.groupMap[this.groupID];
			if(group.view_year_range === undefined) {
				if(this.metadata.region === undefined || this.metadata.region.view_year_range === undefined) {
					return undefined;
				} else {
					return this.metadata.region.view_year_range;
				}
			} else {
				return group.view_year_range;
			}
		}		
	},
	isMessageRawWarning: function() {
		var self = this;
		if(self.aggregation === 'raw' || self.sensorIDs[0] === '*') {
			return false;
		}
		var result = false;
		this.sensorIDs.forEach(function(o){
			console.log(o);
			var s = self.sensorMap[o];
			console.log(s);
			if(s.raw) {
				result = true;
			}
		});
		return result;
	},
	plotstations_internal: function() {
		var self = this;
		var s = [];
		var ids = this.plotIDs;
		if(this.plots == undefined) {
			ids = [];
		} else if(ids[0] === '*') {
			ids = this.plots.map(function(plot){return plot.id});
		}		
		ids.forEach(function(plotID){
			var plot = self.plotMap[plotID];
			if(plot.plot_stations == undefined) {
				s.push({plot: plot.id, type: "plot", important: true, selected: true, logger_type: plot.logger_type, full_plot: true});
			} else  {
				if(plot.plot_stations.length == 1) {
					var station = plot.plot_stations[0];
					s.push({plot: plot.id, type: "station", important: true, station: station, selected: true, logger_type: self.stationMap[station].logger_type, full_plot: true});
				} else {
					s.push({plot: plot.id, type: "merged", important: true, selected: true, full_plot: true});
					plot.plot_stations.forEach(function(station){
						s.push({plot: plot.id, type: "station", station: station, selected: false, logger_type: self.stationMap[station].logger_type, full_plot: false});
					});
				}
			}
		});
		return s;
	},
	validWidthCustom: function() {
		return this.widthCustom >= 200 && this.widthCustom <= 65535;
	},
	validHeightCustom: function() {
		return this.heightCustom > 0 && this.heightCustom < 2059;
	},
	widthValue: function() {
		if(this.widthText === "custom" && this.validWidthCustom) {
			return this.widthCustom; 
		} else if(this.widthText === "auto"){
			if(this.divContainer == undefined) {
				return 1000;
			} else {
				return this.divContainer.clientWidth - 30 < 100 ? 100 : this.divContainer.clientWidth - 30;
			}
		} else {
			return this.widthTextMap[this.widthText];
		}
	},
	heightValue: function() {
		if(this.heightText === "custom" && this.validHeightCustom) {
			return this.heightCustom; 
		} else {
			return this.heightTextMap[this.heightText];
		}
	},
	filteredTableData: function() {
		var data = this.table.data;
		var len = data.length;
		var start = this.filteredtableStartRow;
		var rowCount = this.filteredtableMaxRowCount;
		if(start < 0 || len <= rowCount) {
			start = 0;
			this.filteredtableStartRow = start;
		}
		if(len <= start) {
			start = len - rowCount;
			this.filteredtableStartRow = start;
		}
		if(start + rowCount > len) {
			rowCount = len - start;
		}
		var result = data.slice(start, start + rowCount);
		return result;
	},
	processingSensors: function() {
		var self = this;
		var sensors = [];
		if(this.sensorIDs[0] == '*') {
			this.metadata.sensors.forEach(function(s){
				if( !s.raw || (s.raw && self.aggregation === 'raw')) {
					sensors.push(s);
				}
			});
		} else if(this.sensorIDs[0] == 'all_measurements') {
			this.metadata.sensors.forEach(function(s){
				if( !s.internal && !s.derived && ( !s.raw || (s.raw && self.aggregation === 'raw') )) {
					sensors.push(s);
				}
			});
		} else if(this.sensorIDs[0] == 'all_derived') {
			this.metadata.sensors.forEach(function(s){
				if( !s.internal && s.derived && ( !s.raw || (s.raw && self.aggregation === 'raw') )) {
					sensors.push(s);
				}
			});
		} else {
			this.sensorIDs.forEach(function(o){
				var s = self.sensorMap[o];
				if( !s.raw || (s.raw && self.aggregation === 'raw')) {
					sensors.push(s);
				}
			});
		}
		return sensors;
	},
}, //end computed

mounted: function () {
	var self = this;
	var pageParams = getUrlQueryParams(document.location.search);
	if(pageParams.pinned_plot !== undefined) {
		self.pinned_plot = pageParams.pinned_plot;
	}
	if(pageParams.back_link !== undefined) {
		self.back_link = pageParams.back_link === 'false' ? false : true;
	}

	this.divContainer = document.getElementById("container");
	this.updateMetadata();	
}, //end mounted

methods: {
	updateMetadata: function() {
		var self = this;
		this.viewCycle++;
		this.appMessage = "query metadata of "+ this.project.id +" ...";
		var params = {region:this.project.id};
		axios.get(url_metadata_json, {params: params})
		.then(function(response) {
			self.metadata = response.data;
			//console.log(self.metadata);
			self.appMessage = null;		
		})
		.catch(function(error) {
			self.appMessage = "ERROR: "+error;
		});		
	},
	updateViews: function() {
		var self = this;
		
		if(self.viewType == 'sensors' || self.viewType == 'plots') {
			this.views = [];
			this.viewsDone = 0;
			this.viewPrecessingStart = 0;
			this.viewPrecessingEnd = 0;
			this.viewCycle++;
			return;
		}
		
		var plotstations = self.plotstations.filter(function(o){return o.selected;});
		
		/*var container = document.getElementById("container");
		var width = container == null ? 100 : (container.clientWidth - 30 < 100 ? 100 : container.clientWidth - 30);*/
		var width = this.widthValue;
		var height = this.heightValue;
		
		var views = [];
		var sensorNames = this.processingSensors.map(function(sensor) {return sensor.id;});
		var plotStationNames = plotstations.map(function(plotstation) {return plotstation.full_plot ? plotstation.plot : plotstation.plot + ':' + plotstation.station});
		this.processingSensors.forEach(function(sensor){
			var innerPlotMap = self.sensorNamePlotMap[sensor.id];
			if(innerPlotMap === undefined) {
				innerPlotMap = {};
			}
			var innerStationMap = self.sensorNameStationMap[sensor.id];
			if(innerStationMap === undefined) {
				innerStationMap = {};
			}
			/*plots.forEach(function(plot){
				if(innerMap[plot.id]) {
					var view = {status: "init", url: "no", type: self.viewType, plot: plot.id, sensor: sensor.id, aggregation: self.aggregation, quality: self.quality, interpolated: self.interpolation, width: width, height: 100};
					view.by_year = true;
					Object.assign(view, self.timeParameters);
					views.push(view);
				}
			});*/
			plotstations.forEach(function(plotstation){
				if(plotstation.full_plot ? innerPlotMap[plotstation.plot] : innerStationMap[plotstation.station] ) {
					var plotStationName = plotstation.full_plot ? plotstation.plot : plotstation.plot + ':' + plotstation.station;
					var view = {status: "init", url: "no", type: self.viewType, plot: plotStationName, sensor: sensor.id, aggregation: self.aggregation, quality: self.quality, interpolated: self.interpolation, width: width, height: height, byYear: self.byYear};
					view.by_year = true;
					Object.assign(view, self.timeParameters);
					if(self.viewType == 'table' || self.viewType == 'csv-file') {
						view.sensor = sensorNames;
					}
					if(self.viewType == 'csv-file') {
						view.plot = plotStationNames;
					}
					views.push(view);
				}
			});
		});
		//this.views = views;
		var maxViews = 500;
		if(self.viewType === 'table' || self.viewType == 'csv-file') {
			maxViews = 1;
		}
		
		if(views.length <= maxViews) {
			this.viewsLimited = false;
			var new_views = views;
		} else {
			this.viewsLimited = true;
			var new_views = views.slice(0, maxViews);
		}		
		if( !this.compareViews(this.views, new_views) ) {
			this.views = new_views;
			this.viewsDone = 0;
			this.viewPrecessingStart = performance.now();
			var currentCycle = this.viewCycle +1;
			this.viewCycle = currentCycle;
			var parallel = 4;
			for(var i=0; i<parallel; i++) {
				self.taskRunner(currentCycle);
			}
		}		
	},
	compareView: function(a, b) {
		if(a.type !== b.type) return false;
		if(a.plot !== b.plot) return false;
		if(a.sensor !== b.sensor) return false;
		if(a.aggregation !== b.aggregation) return false;
		if(a.width !== b.width) return false;
		if(a.height !== b.height) return false;
		if(a.year !== b.year) return false;
		if(a.end_year !== b.end_year) return false;
		if(a.month !== b.month) return false;
		if(a.day !== b.day) return false;
		if(a.quality !== b.quality) return false;
		if(a.interpolated !== b.interpolated) return false;
		if(a.byYear !== b.byYear) return false;
		return true;
	},
	compareViews: function(va, vb) {
		var len = va.length;
		if(len != vb.length) {
			return false;
		}
		for(var i=0; i<len; i++) {
			if(!this.compareView(va[i], vb[i])) {
				return false;
			}
		}
		return true;
	},
	taskRunner: function(currentCycle) {
		if(currentCycle != this.viewCycle) {
			return;
		}
		for(var i in this.views) {
			var view = this.views[i];
			if(view.status == "init") {
				this.taskRun(currentCycle, view);
				return;
			}
		}
	},
	taskRun: function(currentCycle, view) {
		var self = this;
		if(currentCycle != this.viewCycle) {
			return;
		}
		view.status = "running";
		//console.log(view);
		
		var params = {plot: view.plot, sensor: view.sensor, aggregation: view.aggregation, interpolated: "false", quality: view.quality, interpolated: view.interpolated, width: view.width, height: view.height, by_year: view.byYear};		
		if(view.hasOwnProperty('year')) {
			params.year = view.year;
		}
		if(view.hasOwnProperty('end_year')) {
			params.end_year = view.end_year;
		}
		if(view.hasOwnProperty('month')) {
			params.month = view.month;
		}
		if(view.hasOwnProperty('day')) {
			params.day = view.day;
		}
		
		var paramsSerializer = function(params) {
			return Qs.stringify(params, {arrayFormat: 'repeat'})
		};
		
		var url= 'unknown';
		var responseType = 'blob';
		switch(view.type) {
			case 'diagram':
				url = url_query_image;
				break;
			case 'boxplot':
				url = url_query_image;
				params.boxplot = true;
				break;			
			case 'heatmap':
				url = url_query_heatmap;
				break;
			case 'table':
				url = url_query_csv;
				responseType = 'text';			
				break;
			case 'csv-file':
				params.col_plot = true;
				url = url_query_csv + '?' + paramsSerializer(params);
				responseType = 'text';			
				break;				
			default:
				url = 'error';
		}
		
		if(self.viewType == 'csv-file') {
			view.url = url;
			view.status = "done";
			self.taskEnd(currentCycle);
			self.taskRunner(currentCycle);
			return;
		}

		axios.get(url, {responseType: responseType, params: params, paramsSerializer: paramsSerializer})
		.then(function(response) {
			switch(view.type) {
			case 'diagram':
			case 'boxplot':
			case 'heatmap': {
				var url = URL.createObjectURL(response.data);
				view.url = url;
				view.status = "done";
				break;
			}
			case 'table': {
				var data = response.data.split('\r\n');
				var header = data.shift();
				data.pop(); //remove last empty line
				self.table = {header: header, data: data};
				view.status = "done";
				break;
			}
			default: {
				view.status = "error";
			}
			}
			self.taskEnd(currentCycle);
			self.taskRunner(currentCycle);
		})
		.catch(function(error) {
			view.status = "error";
			self.taskEnd(currentCycle);
			self.taskRunner(currentCycle);
		});		
	},
	taskEnd: function(currentCycle) {
		if(currentCycle != this.viewCycle) {
			return;
		}
		this.viewsDone++;
		if(this.viewsDone == this.views.length) {
			this.viewPrecessingEnd = performance.now();
		}
	},
	onClickPlotstation: function(plotstation) {
		plotstation.selected = !plotstation.selected;
		console.log(plotstation);
		this.updateViews();
	},
}, //end methods

watch: {
	project: function() {
		this.updateMetadata();
	},
	metadata: function() {
		var self = this;
		console.log("metadata update");
		this.groupID = "*";
		var defaultGroup = self.pinned_plot === undefined ? this.metadata.region.default_general_station : '*';
		var values = {"*":{id:"*", name:"*"}};
		this.metadata.general_stations.forEach(function(o){
			values[o.id] = o;
			if(defaultGroup !== undefined && defaultGroup === o.id) {
				self.groupID = o.id;
			}
		});
		this.groupMap = values;
		
		if(self.pinned_plot !== undefined) {
			this.metadata.plots = this.metadata.plots.filter(function(o) {
				if(self.pinned_plot === o.id) {
					if(o.general_station !== undefined) {
						self.groupID = o.general_station; // set groupID to plot group for correct plot group time ranges	
					}
					return true;
				} else {
					return false;
				}
			});
		}
		this.plotIDs = this.metadata.plots.length==0 ? ["*"] : [this.metadata.plots[0].id];
		values = {"*":{id:"*", name:"*"}};
		this.metadata.plots.forEach(function(o){
			values[o.id] = o;
		});
		this.plotMap = values;

		if(this.metadata.plots.length==0) {
			self.appMessage = self.pinned_plot === undefined ? "no plots" : "plot not found: " + self.pinned_plot;
		}
		
		values = {};
		this.metadata.stations.forEach(function(o){
			values[o.id] = o;
		});
		this.stationMap = values;

		values = {"*":{id:"*", name:"*"}, "all_measurements":{id:"all_measurements", name:"all_measurements"}, "all_derived":{id:"all_derived", name:"all_derived"}};
		this.metadata.sensors.forEach(function(o){
			values[o.id] = o;
		});
		this.sensorMap = values;
		console.log("this.sensorIDs");
		console.log(this.sensorIDs);
		
		var sensorNamePlotMap = {};	
		this.metadata.plots.forEach(function(plot){
			plot.sensor_names.forEach(function(sensor_name){
				var innerMap = sensorNamePlotMap[sensor_name];
				if(innerMap == undefined) {
					innerMap = {};
					sensorNamePlotMap[sensor_name] = innerMap;
				}
				innerMap[plot.id] = true;
			});
		});
		this.sensorNamePlotMap = sensorNamePlotMap;
		
		var sensorNameStationMap = {};	
		this.metadata.stations.forEach(function(station){
			station.sensor_names.forEach(function(sensor_name){
				var innerMap = sensorNameStationMap[sensor_name];
				if(innerMap == undefined) {
					innerMap = {};
					sensorNameStationMap[sensor_name] = innerMap;
				}
				innerMap[station.id] = true;
			});
		});
		this.sensorNameStationMap = sensorNameStationMap;
		
		var startYear = this.metadata.region.view_year_range.start;
		var endYear = this.metadata.region.view_year_range.end;
		var y = [];
		for(var i=startYear; i<=endYear; i++) {
			y.push(i);
		}
		this.timeYears = y;
		
		
		this.updateViews();
	},
	plots: function() {
		this.plotIDs = this.metadata.plots.length==0 ? ["*"] : [this.plots[0].id];
		this.updateViews();
	},
	plotIDs: function() {
		this.updateViews();		
	},
	sensorIDs: function(e) {
		console.log("sensorIDs changed: ");
		console.log(e);
		console.log(this.sensorIDs);
		this.updateViews();
	},
	aggregation: function() {
		this.updateViews();
	},
	timeParameters: function() {
		this.updateViews();
	},
	quality: function() {
		this.updateViews();
	},
	interpolation: function() {
		this.updateViews();
	},
	viewType: function() {
		this.updateViews();
	},
	plotstations_internal: function() {
		this.plotstations = this.plotstations_internal;
	},
	plotstations: function() {
		this.updateViews();
	},
	widthValue: function() {
		if(this.metadata !== undefined && this.metadata.sensors !== undefined) {
			this.updateViews();
		}
	},
	heightValue: function() {
		if(this.metadata !== undefined && this.metadata.sensors !== undefined) {
			this.updateViews();
		}
	},
	byYear: function() {
		this.updateViews();
	},
	groupViewYearRange: function() {
		if(this.groupViewYearRange != undefined) {
			var startYear = this.groupViewYearRange.start;
			var endYear = this.groupViewYearRange.end;
			var y = [];
			var validYear = false;
			for(var i=startYear; i<=endYear; i++) {
				y.push(i);
				if(i == this.timeYear) {
					validYear = true;
				}
			}
			this.timeYears = y;		
			if(!validYear) {
				this.timeYear = '*';
			}
		}
	},
}, //end watch

});	//end visualisation-interface
	

Vue.component('help-interface', {
	
template: '#help-template',

data: function () {
	return {

	visible: false,

	};
},

mounted: function() {
	var self = this;
	window.addEventListener('keyup', function (e) {
		if (e.keyCode == 27) {
			console.log(e);
			self.visible = false;
		}
	});
},

methods: {
	show() {
		this.visible = true;
	}
},

});


var app = new Vue({
	
el: '#app',

data: {
	appMessage: "init app ...",
	projectHover: false,
	projectHoverStay: false,
	projects: [],
	projectMap: {},
	projectID: "",
	pinned_project: undefined,
}, //end data

mounted: function () {
	var self = this;
	self.appMessage = "query projects...";

	var pageParams = getUrlQueryParams(document.location.search);
	if(pageParams.pinned_project !== undefined) {
		self.pinned_project = pageParams.pinned_project;
	}

	axios.get(url_region_json)
	.then(function(response) {
		self.projects = response.data;
		if(self.pinned_project !== undefined) {
			self.projects = self.projects.filter(function(o) {
				return self.pinned_project === o.id;
			});
		}
		var values = {};
		self.projects.forEach(function(o){
			values[o.id] = o;
		})
		self.projectMap = values;
		if(self.projects.length > 0) {
			self.projectID = self.projects[0].id;
			self.appMessage = null;
		} else {
			self.projectID = "";
			self.appMessage = self.pinned_project === undefined ? "no projects" : "project not found: " + self.pinned_project;
		}		
	})
	.catch(function(error) {
		self.appMessage = "ERROR: "+error;
	});
}, //end mounted
	
}); //end app

} //end init