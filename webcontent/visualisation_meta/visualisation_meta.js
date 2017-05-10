"use strict";
document.addEventListener('DOMContentLoaded', function() {init();}, false);
Vue.config.productionTip = false;

var url_api_base = "../../";
var url_region_json = url_api_base + "tsdb/region.json";
var url_metadata_json = url_api_base + "tsdb/metadata.json";
var url_query_image = url_api_base + "tsdb/query_image";
var url_query_heatmap = url_api_base + "tsdb/query_heatmap";

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

function init() {
	
Vue.component('visualisation-interface', {
	
template: '#visualisation-template',

props: {
	project: Object,
}, //end props

data: function () {
	return {
		appMessage: "init view ...",		
		metadata: {},
		
		groupHover: false,
		groupHoverStay: false,
		groupID: "*",
		groupMap: {},
		
		plotHover: false,
		plotIDs: ["*"],
		plotMap: {},
		
		sensorHover: false,
		sensorIDs: ["*"],
		sensorMap: {},

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
		quality: "physical",
		
		interpolationHover: false,
		interpolation: false,
		
		viewTypeHover: false,
		viewTypeHoverStay: false,
		viewTypes: ["diagram", "heatmap", "boxplot", "sensors"],
		viewType: "diagram",
		
		views: [],
		viewsDone: 0,
		viewCycle: 0,
		viewPrecessingStart: 0,
		viewPrecessingEnd: 0,	
	}
}, //end data

computed: {
	plots: function() {
		var self = this;
		if(this.groupID == "*") {
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
		var self = this;
		if(this.plotIDs[0] == '*') {
			return this.metadata.sensors;
		} else {
			var map = {};
			this.plotIDs.forEach(function(plotID){
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
			return {};
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
}, //end computed

mounted: function () {
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
		
		if(self.viewType == 'sensors') {
			this.views = [];
			this.viewsDone = 0;
			this.viewPrecessingStart = 0;
			this.viewPrecessingEnd = 0;
			this.viewCycle++;
			return;
		}
		
		var plots = [];
		if(this.plotIDs[0] == '*') {
			plots = this.metadata.plots;
		} else {
			this.plotIDs.forEach(function(o){
				plots.push(self.plotMap[o]);
			});
		}
		var sensors = [];
		if(this.sensorIDs[0] == '*') {
			sensors = this.metadata.sensors;
		} else {
			this.sensorIDs.forEach(function(o){
				sensors.push(self.sensorMap[o]);
			});
		}
		//console.log(plots);
		//console.log(sensors);
		
		var container = document.getElementById("container");
		var width = container == null ? 100 : (container.clientWidth - 30 < 100 ? 100 : container.clientWidth - 30);
		
		var views = [];
		sensors.forEach(function(sensor){
			var innerMap = self.sensorNamePlotMap[sensor.id];
			plots.forEach(function(plot){
				if(innerMap[plot.id]) {
					var view = {status: "init", url: "no", type: self.viewType, plot: plot.id, sensor: sensor.id, aggregation: self.aggregation, quality: self.quality, interpolated: self.interpolation, width: width, height: 100};
					view.by_year = true;
					Object.assign(view, self.timeParameters);
					views.push(view);
				}
			});
		});
		//this.views = views;
		var maxViews = 500;
		var new_views = views.length <= maxViews ? views : views.slice(0, maxViews);
		if( !this.compareViews(this.views, new_views) ) {
			this.views = new_views;
			this.viewsDone = 0;
			this.viewPrecessingStart = performance.now();
			var currentCycle = this.viewCycle +1;
			this.viewCycle = currentCycle;
			var parallel = 1;
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
		if(a.month !== b.month) return false;
		if(a.day !== b.day) return false;
		if(a.quality !== b.quality) return false;
		if(a.interpolated !== b.interpolated) return false;
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
		
		var params = {plot: view.plot, sensor: view.sensor, aggregation: view.aggregation, interpolated: "false", quality: view.quality, interpolated: view.interpolated, width: view.width, height: view.height};		
		if(view.hasOwnProperty('year')) {
			params.year = view.year;
		}
		if(view.hasOwnProperty('month')) {
			params.month = view.month;
		}
		if(view.hasOwnProperty('day')) {
			params.day = view.day;
		}
		
		var url= 'unknown';
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
				if(true) {
					params.by_year = view.by_year;
				}
				break;
			default:
				url = 'error';
		}
		

		axios.get(url, {responseType: 'blob', params: params})
		.then(function(response) {
			//console.log(response.data);
			var url = URL.createObjectURL(response.data);
			view.url = url;
			view.status = "done";
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
	}
}, //end methods

watch: {
	project: function() {
		this.updateMetadata();
	},
	metadata: function() {
		this.groupID = "*";
		var values = {"*":{id:"*", name:"*"}};
		this.metadata.general_stations.forEach(function(o){
			values[o.id] = o;
		});
		this.groupMap = values;
		
		this.plotIDs = this.metadata.plots.length==0 ? ["*"] : [this.metadata.plots[0].id];
		values = {"*":{id:"*", name:"*"}};
		this.metadata.plots.forEach(function(o){
			values[o.id] = o;
		});
		this.plotMap = values;
		
		this.sensorIDs = ["*"];
		values = {"*":{id:"*", name:"*"}};
		this.metadata.sensors.forEach(function(o){
			values[o.id] = o;
		});
		this.sensorMap = values;
		
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
		//console.log(sensorNamePlotMap);		
		this.sensorNamePlotMap = sensorNamePlotMap;
		
		var startYear = this.metadata.region.view_year_range.start;
		var endYear = this.metadata.region.view_year_range.end;
		var y = [];
		for(var i=startYear; i<=endYear; i++) {
			y.push(i);
		}
		if(y.length==1) {
			this.timeframeYear = y[0];
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
	sensorIDs: function() {
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
}, //end watch

});	//end visualisation-interface
	
	

var app = new Vue({
	
el: '#app',

data: {
	appMessage: "init app ...",
	projectHover: false,
	projectHoverStay: false,
	projects: [],
	projectMap: {},
	projectID: "",
}, //end data

mounted: function () {
	var self = this;
	self.appMessage = "query projects...";
	axios.get(url_region_json)
	.then(function(response) {
		self.projects = response.data;
		var values = {};
		self.projects.forEach(function(o){
			values[o.id] = o;
		})
		self.projectMap = values;
		self.projectID = self.projects[0].id;
		self.appMessage = null;		
	})
	.catch(function(error) {
		self.appMessage = "ERROR: "+error;
	});
}, //end mounted
	
}); //end app

} //end init