"use strict";
document.addEventListener('DOMContentLoaded', function() {init();}, false);

var url_api_base = "../../";
var url_region_json = url_api_base + "tsdb/region.json";
var url_metadata_json = url_api_base + "tsdb/metadata.json";
var url_query_image = url_api_base + "tsdb/query_image";

Array.prototype.contains = function (e) {
   for (var i in this) {
       if (this[i] == e) return true;
   }
   return false;
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
}, //end computed

mounted: function () {
	this.updateMetadata();
}, //end mounted

methods: {
	updateMetadata: function() {
		var self = this;
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
		var views = [];
		sensors.forEach(function(sensor){
			var innerMap = self.sensorNamePlotMap[sensor.id];
			plots.forEach(function(plot){
				if(innerMap[plot.id]) {
					var view = {status: "init", url: "no", type: "diagram", plot: plot.id, sensor: sensor.id, width: 1000, height: 100};
					views.push(view);
				}
			});
		});
		//this.views = views;
		var maxViews = 500;
		this.views = views.length <= maxViews ? views : views.slice(0, maxViews);
		this.viewsDone = 0;
		this.viewPrecessingStart = performance.now();
		var currentCycle = this.viewCycle +1;
		this.viewCycle = currentCycle;
		var parallel = 6;
		for(var i=0; i<parallel; i++) {
			self.taskRunner(currentCycle);
		}
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
		
		var params = {plot: view.plot, sensor: view.sensor, aggregation: "hour", interpolated: "true", width: view.width, height: view.height};
		axios.get(url_query_image, {responseType: 'blob', params: params})
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
		this.updateViews();
	},
	plots: function() {
		this.plotIDs = this.metadata.plots.length==0 ? ["*"] : [this.metadata.plots[0].id];
		this.updateViews();
	},
	plotIDs: function() {
		this.updateViews();		
	},
	sensorIDs: function() {
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