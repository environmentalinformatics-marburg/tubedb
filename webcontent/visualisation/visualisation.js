"use strict";
document.addEventListener('DOMContentLoaded', function() {init();}, false);

var url_api_base = "../../";
var url_region_json = url_api_base + "tsdb/region.json";
var url_generalstation_list = url_api_base + "tsdb/generalstation_list";
var url_plot_list = url_api_base + "tsdb/plot_list";
var url_sensor_list = url_api_base + "tsdb/sensor_list";
var url_query_image = url_api_base + "tsdb/query_image";

function init() {
	
	
Vue.component('visualisation-interface', {
	
template: '#visualisation-template',

props: {
	projectID: String,
},

data: function () {
	return {
		appMessage: "init ...",
		groupHover: false,
		groups: [],
		groupID: "*",
		plotHover: false,
		plots: [],
		plotID: "",
		sensorHover: false,
		sensors: [],
		sensorIDs: ["*"],
		sensorMap: {},
		views: [],
	}
},

computed: {
	groupMap: function() {
		var values = {"*": {id: "*", name: "*"}};
		this.groups.forEach(function(o){
			values[o.id] = o;
		})
		return values;
	},
},

mounted: function () {
	this.updateGroups();
},

methods: {
	updateGroups: function() {
		var self = this;
		self.appMessage = "query groups...";	
		axios.get(url_generalstation_list, {params: {region: self.projectID}})
		.then(function(response) {
			self.groups = response.data.split('\n').map(function(row) {
				var cols = row.split(';');
				return {id: cols[0], name: cols[1]};
			});
			if(self.groupID == "*") {
				self.updatePlots();
			} else {
				self.groupID = "*"; // calls updatePlots() by watch groupID
			}
			self.appMessage = null;
		})
		.catch(function(error) {
			self.appMessage = "ERROR: "+error;
		});
	},
	updatePlots: function() {
		var self = this;
		self.appMessage = "query plots...";
		var params = self.groupID=="*" ? {region: self.projectID} : {generalstation: self.groupID}; 
		axios.get(url_plot_list, {params: params})
		.then(function(response) {
			self.plots = response.data.split('\n').map(function(row) {
				var cols = row.split(';');
				return {id: cols[0], a: cols[1], b:cols[2]};
			});
			self.plotID = self.plots[0].id;
			self.appMessage = null;
		})
		.catch(function(error) {
			self.appMessage = "ERROR: "+error;
		});
	},
	updateSensors: function() {
		var self = this;
		self.appMessage = "query sensors...";
		var params = {plot: this.plotID}; 
		axios.get(url_sensor_list, {params: params})
		.then(function(response) {
			self.sensors = response.data.split('\n').map(function(row) {
				var cols = row.split(';');
				return {id: cols[0], a: cols[1], b:cols[2]};
			});
			if(self.sensorIDs[0] == ["*"]) {
				self.updateViews();
			} else {
				self.sensorIDs = ["*"];
			}
			var values = {};
			self.sensors.forEach(function(o){
				values[o.id] = o;
			})
			self.sensorMap = values;
			self.appMessage = null;
		})
		.catch(function(error) {
			self.appMessage = "ERROR: "+error;
		});
	},
	getDiagramURL: function(view) {
		var query = "plot="+view.plot+"&"+"sensor="+view.sensor+"&"+"width="+view.width+"&"+"height="+view.height;
		return url_query_image+"?"+query;
	},
	updateViews: function() {
		var self = this;
		var container = document.getElementById("container");
		var width = container.clientWidth - 30 < 100? 100 : container.clientWidth - 30;
		var height = 100; 
		var sensors = this.sensorIDs[0]=="*" ? this.sensors : this.sensorIDs.map(function(sensorID){return self.sensorMap[sensorID]});
		this.views = sensors.map(function(sensor) {
			return {type: "diagram", plot:self.plotID, sensor:sensor.id, width:width, height:height}; 
		});
	},
},

watch: {
	projectID: function () {
		this.updateGroups();
	},
	groupID: function() {
		this.groupHover = false;
		this.updatePlots();
	},
	plotID: function() {
		this.plotHover = false;
		this.updateSensors();
		
	},
	sensorIDs: function() {
		this.updateViews();
	}
}, //end watch

});	//end visualisation-interface
	
	

var app = new Vue({
	
el: '#app',

data: {
	appMessage: "init ...",
	projectHover: false,
	projects: [],
	projectMap: {},
	projectID: "",
},

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