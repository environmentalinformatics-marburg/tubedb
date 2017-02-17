"use strict";
document.addEventListener('DOMContentLoaded', function() {init();}, false);

var url_api_base = "../../";
var url_region_json = url_api_base + "tsdb/region.json";
var url_generalstation_list = url_api_base + "tsdb/generalstation_list";
var url_plot_list = url_api_base + "tsdb/plot_list";
var url_sensor_list = url_api_base + "tsdb/sensor_list";
var url_query_image = url_api_base + "tsdb/query_image";
var url_query_heatmap = url_api_base + "tsdb/query_heatmap";
var url_query_csv = url_api_base + "tsdb/query_csv";


function init() {
	
Vue.component('visualisation-interface', {
	
template: '#visualisation-template',

props: {
	project: Object,
}, //end props

data: function () {
	return {
		appMessage: "init ...",
		groupHover: false,
		groupHoverStay: false,
		groups: [],
		groupID: "*",
		plotHover: false,
		plots: [],
		plotIDs: [],
		sensorHover: false,
		sensors: [],
		sensorIDs: ["*"],
		sensorMap: {},
		timeframeHover: false,
		timeframeHoverStay: false,
		timeframeYear: "*",
		timeframeMonths: ["jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec"],
		timeframeMonthsNumber: {"*":0, "jan":1, "feb":2, "mar":3, "apr":4, "may":5, "jun":6, "jul":7, "aug":8, "sep":9, "oct":10 ,"nov":11, "dec":12},
		timeframeMonth: "*",
		aggregations: ["raw","hour","day","week","month","year"],
		aggregationsText: {raw:"raw data", hour:"hours", day:"days", week:"weeks", month:"months", year:"years"},
		aggregation: "hour",
		qualityHover: false,
		qualityHoverStay: false,
		qualities: ["no", "physical", "step", "empirical"],
		qualitiesText: {no:"0: no", physical:"1: physical", step:"2: physical,step", empirical:"3: physical,step,empirical"},
		quality: "physical",
		interpolated: false,
		settingsHover: false,
		settingsHoverStay: false,
		viewTypes: ["diagram", "heatmap", "boxplot", "table"],
		viewType: "diagram",
		widthText: "auto",
		widthCustom: 1000,
		heightTexts: ["small", "normal", "large", "custom"],
		heightTextsMap: {small:100, normal:400, large:800},
		heightText: "small",
		heightCustom: 100,
		hideNoData: true,
		views: [],
		max_display_views: 500,
		loadedImageCount: 0,
		errorImage: [],
		tableView: undefined,
		tableData: undefined,
		tableDataFilteredStart: 0,
		tableDataFilteredRange: 100,
	}
}, //end data

computed: {
	groupMap: function() {
		var values = {"*": {id: "*", name: "*"}};
		this.groups.forEach(function(o){
			values[o.id] = o;
		})
		return values;
	},
	timeframeYears: function() {
		var start = this.project.view_year_range.start;
		var end = this.project.view_year_range.end;
		var y = [];
		for(var i=start; i<=end; i++) {
			y.push(i);
		}
		if(y.length==1) {
			this.timeframeYear = y[0];
		}
		return y;
	},
	display_views: function() {	
		return this.views.length <= this.max_display_views ? this.views : this.views.slice(0, this.max_display_views);
	},
	viewHeight: function(){
		if(this.heightText=="custom") {
			var v = this.heightCustom;
			return this.isValidSize(v) ? v : 100;
		} else {
			return this.heightTextsMap[this.heightText];			
		}
	},
	filteredSensors: function() {
		var self = this;
		return this.sensors.filter(function(sensor) {
			return self.aggregation=="raw" || sensor.aggregation != "NONE";
		});
	},
	filteredTableData: function() {
		var data = this.tableData.data;
		var len = data.length;
		var rowRange = this.tableDataFilteredRange;
		var start = this.tableDataFilteredStart;
		if(len > rowRange) {
			console.log("check1");
			if(this.tableDataFilteredStart < 0) {
				this.tableDataFilteredStart = 0;
				console.log("check2");
			} else if(this.tableDataFilteredStart > len-1) {
				console.log("check3");
				this.tableDataFilteredStart = len-1;
			}
			if(this.tableDataFilteredStart + rowRange > len) {
				rowRange = len - this.tableDataFilteredStart;
				console.log("check4");
			}			
			data = data.slice(this.tableDataFilteredStart, this.tableDataFilteredStart + rowRange);
		} else {
			this.tableDataFilteredStart = 0;
		}
		return {header: this.tableData.header, data: data};
	}
},

mounted: function () {
	this.updateGroups();
}, //end mounted

methods: {
	updateGroups: function() {
		var self = this;
		self.appMessage = "query groups...";	
		axios.get(url_generalstation_list, {params: {region: self.project.id}})
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
		var params = self.groupID=="*" ? {region: self.project.id} : {generalstation: self.groupID}; 
		axios.get(url_plot_list, {params: params})
		.then(function(response) {
			self.plots = response.data.split('\n').map(function(row) {
				var cols = row.split(';');
				return {id: cols[0], a: cols[1], b:cols[2]};
			});
			self.plotIDs = [self.plots[0].id];
			self.appMessage = null;
		})
		.catch(function(error) {
			self.appMessage = "ERROR: "+error;
		});
	},
	updateSensors: function() {
		var self = this;
		self.appMessage = "query sensors...";
		var params;
		if(this.plotIDs[0] == '*' || this.plotIDs.length>1 ) {
			params = self.groupID=="*" ? {region: self.project.id} : {general_station: self.groupID};
		} else {
			params = {plot: self.plotIDs[0]}; 
		}
		params.raw = true;
		axios.get(url_sensor_list, {params: params})
		.then(function(response) {
			var prevIDs = self.sensorIDs;
			self.sensors = response.data.split('\n').map(function(row) {
				var cols = row.split(';');
				return {id: cols[0], description: cols[1], unitDescription:cols[2], aggregation:cols[3], internal:cols[4]};
			});
			
			var newIDs = [];
			if(prevIDs[0]=='*') {
				newIDs = ['*'];
			} else {
				newIDs = prevIDs.filter(function(id) {return self.sensors.find(function(s) {return s.id==id;}) != undefined;});
			}
			
			if(newIDs.length == 0) {
				newIDs.push('*');
			}
			
			self.sensorIDs = newIDs;
			
			if(JSON.stringify(prevIDs) === JSON.stringify(newIDs)) {
				self.updateViews();
			}

			
			console.log("prev "+prevIDs+"  now "+self.sensorIDs);
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
		var query = "plot="+view.plot+"&"+"sensor="+view.sensor+"&"+"width="+view.width+"&"+"height="+view.height+"&"+"aggregation="+view.aggregation+"&"+"quality="+view.quality+"&"+"interpolated="+view.interpolated+"&"+"boxplot="+view.boxplot;
		if(view.year!='*') {
			query += '&year='+view.year;
			if(view.month>0) {
				query += '&month='+view.month;
			}
		}
		return url_query_image+"?"+query;
	},
	getHeatmapURL: function(view) {
		var query = "plot="+view.plot+"&"+"sensor="+view.sensor+"&"+"quality="+view.quality+"&"+"interpolated="+view.interpolated;
		if(view.year!='*') {
			query += '&year='+view.year;
			if(view.month>0) {
				query += '&month='+view.month;
			}
		}
		return url_query_heatmap+"?"+query;
	},
	updateViews: function() {
		
		var flatMap = function(array, callback) { // source: http://obscurejavascript.tumblr.com/post/144412398726/flatmap-in-javascript
			return [].concat.apply([], array.map(callback));
		};
		
		var self = this;
		var container = document.getElementById("container");
		var width = this.widthText=="auto" ? (container.clientWidth - 30 < 100? 100 : container.clientWidth - 30) : (this.isValidSize(this.widthCustom) ? this.widthCustom : 1000);
		var height = 100; 
		var sensors = this.sensorIDs[0]=="*" ? this.filteredSensors : this.sensorIDs.map(function(sensorID){return self.sensorMap[sensorID]});
		/*this.views = sensors.map(function(sensor) {
			return {type: "diagram", 
			        plot: self.plotIDs[0], 
					sensor: sensor.id, 
					width: width, 
					height: height, 
					year: self.timeframeYear, 
					month: self.timeframeMonthsNumber[self.timeframeMonth], 
					aggregation: self.aggregation,
					quality: self.quality,
					interpolated: self.interpolated}; 
		});*/
		
		/*this.views = flatMap(self.plotIDs, function(plotID) {
			return sensors.map(function(sensor) {
				return {type: "diagram", 
						plot: self.plotIDs[0], 
						sensor: sensor.id, 
						width: width, 
						height: height, 
						year: self.timeframeYear, 
						month: self.timeframeMonthsNumber[self.timeframeMonth], 
						aggregation: self.aggregation,
						quality: self.quality,
						interpolated: self.interpolated}; 
			});
		});*/
		
		var plotIDs = self.plotIDs[0]=='*' ? self.plots.map(function(p) {return p.id;}) : self.plotIDs;
		
		this.views = flatMap(sensors, function(sensor) {
			return plotIDs.map(function(plotID) {
				return {type: self.viewType, 
						plot: plotID, 
						sensor: sensor.id, 
						width: width, 
						height: self.viewHeight, 
						year: self.timeframeYear, 
						month: self.timeframeMonthsNumber[self.timeframeMonth], 
						aggregation: self.aggregation,
						quality: self.quality,
						interpolated: self.interpolated,
						boxplot: self.viewType=="boxplot"}; 
			});
		});
		
		if(this.viewType == 'table') {
			this.tableView = {};
		} else {
			this.tableView = undefined;
		}
		
		this.errorImage = [];
		this.loadedImageCount = 0;
	},
	
	isValidSize: function(v) {
		return v>=10 && v<=4096;
	},
	
	onImageLoad: function(event) {
		console.log(event);
		this.loadedImageCount++;
	},
	
	onImageError: function(event) {
		console.log(event);
		event.target.alt = 'no data';
		var i = parseInt(event.target.id.substring(3));
		console.log(i);
		Vue.set(this.errorImage, i, true);
		//this.errorImage.splice(i, 1, true); // this.errorImage[i] = true;
		console.log(this.errorImage);
		this.loadedImageCount++;
	},
	
	updateTableData: function() {
		var self = this;
		var plotIDs = self.plotIDs[0]=='*' ? self.plots.map(function(p) {return p.id;}) : self.plotIDs;
		var sensors = this.sensorIDs[0]=="*" ? this.filteredSensors : this.sensorIDs.map(function(sensorID){return self.sensorMap[sensorID]});
		
		var params = new URLSearchParams();
		params.append('plot', plotIDs[0]);
		sensors.map(function(s) {params.append('sensor', s.id)});
		params.append('aggregation', self.aggregation);
		params.append('quality', self.quality);
		params.append('interpolated', self.interpolated);		
		if(self.timeframeYear!='*') {
			params.append('year', self.timeframeYear);
			if(self.timeframeMonthsNumber[self.timeframeMonth]>0) {
				params.append('month', self.timeframeMonthsNumber[self.timeframeMonth]);
			}
		}	
		
		//var params = {plot: plotIDs[0], sensor: [sensors[0].id, "TEST"], aggregation: self.aggregation, quality: self.quality}; 
		axios.get(url_query_csv, {params: params})
		.then(function(response) {
			var data = response.data.split('\r\n');
			var header = data.shift();
			data.pop(); //remove last empty line
			self.tableData = {header: header, data: data};
		})
		.catch(function(error) {
			//self.appMessage = "ERROR: "+error;
		});
	},
	
	onButtonTableToStart: function() {
		this.tableDataFilteredStart = 0;
	},
	
	onButtonTableToPrev: function() {
		this.tableDataFilteredStart -= this.tableDataFilteredRange;
	},
	
	onButtonTableToNext: function() {
		this.tableDataFilteredStart += this.tableDataFilteredRange;		
	},
	
	onButtonTableToEnd: function() {
		if(this.tableData != undefined) {
			console.log(this.tableDataFilteredStart);
			this.tableDataFilteredStart = this.tableData.data.length - this.tableDataFilteredRange;
			console.log(this.tableDataFilteredStart);
		}
	},
	
}, //end methods

watch: {
	project: function () {
		this.updateGroups();
	},
	groupID: function() {
		this.groupHover = false;
		this.updatePlots();
	},
	plotIDs: function() {
		this.plotHover = false;
		this.updateSensors();
		
	},
	sensorIDs: function() {
		this.updateViews();
	},
	timeframeYear: function() {
		this.updateViews();
	},
	timeframeMonth: function() {
		this.updateViews();
	},
	aggregation: function() {
		this.updateViews();
	},
	quality: function() {
		this.updateViews();
	},
	interpolated: function() {
		this.updateViews();
	},
	viewType: function() {
		this.updateViews();
	},
	viewWidth: function() {
		this.updateViews();
	},
	widthCustom: function() {
		this.updateViews();
	},
	viewHeight: function() {
		this.updateViews();
	},
	tableView: function() {
		this.updateTableData();
	}
}, //end watch

});	//end visualisation-interface
	
	

var app = new Vue({
	
el: '#app',

data: {
	appMessage: "init ...",
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