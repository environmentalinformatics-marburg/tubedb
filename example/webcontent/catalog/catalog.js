"use strict";
document.addEventListener('DOMContentLoaded', function() {init();}, false);

var url_api_base = "../../";
var url_region_list = url_api_base + "tsdb/region_list";
var url_generalstation_list = url_api_base + "tsdb/generalstation_list";
var url_plot_list = url_api_base + "tsdb/plot_list";
var url_source_catalog = url_api_base + "tsdb/source_catalog.csv";

function splitData(data) {
	var lines = data.split(/\n/);
	var rows = [];
	for (var i in lines) {
		if(lines[i].length>0) {
			rows.push(lines[i].split(';'));
		}
	}
	return rows;
}

function splitCSV(data) {
	var lines = data.split(/\n/);
	var rows = [];
	for (var i in lines) {
		if(lines[i].length>0) {
			rows.push(lines[i].split(','));
		}
	}
	return rows;
}

function init() {
		
var app = new Vue({
	
el: '#app',
 
data: {
	appMessage: "init...",
	projects: [],
	project: undefined,
	plotGroups: [],
	plotGroup: undefined,
	plots: [],
	plot: undefined,
	years: ['*','2008','2009','2010','2011','2012','2013','2014','2015','2016'],
	year: '*',
	tableHeader: [],
	tableData: [],
	tableMessage: "init...",
	tableFilterKey: "",
},
 
created: function () {
	var self = this;
	this.appMessage = "query projects...";
	 
	Helper.getText(url_region_list, 
		function(text) {
			self.projects = splitData(text);
			self.project = self.projects[0][0];
			self.appMessage = undefined;
		},
		function(error) {
			self.appMessage = "ERROR "+error;
		}			
	);
},
	
watch: {
	project: function(project) {		
		var self = this;
		self.tableMessage = "load plotGroups of "+project+" ...";
		self.plotGroups = [];
		self.plotGroup = undefined;
		Helper.getText(url_generalstation_list+"?region="+project, 
			function(text) {
				self.tableMessage = "ok";
				var plotgroups = splitData(text);
				plotgroups.unshift(['*', '*']);
				self.plotGroups = plotgroups;
				self.plotGroup = self.plotGroups[0][0];
			},
			function(error) {
				self.tableMessage = "ERROR no plot group data loaded   "+error;
		});
    	/*self.tableMessage = "load catalog of "+project+" ...";
		Helper.getText(url_source_catalog, 
		function(text) {
			var data = splitCSV(text);
			var header = data[0];
			data.shift();
			//console.log(header);
			//console.log(data);
			self.tableHeader = header;
			self.tableData = data;
			self.tableMessage = undefined;
		},
		function(error) {
			self.tableMessage = "ERROR loading catalog of "+project+"  "+error;
		}			
	);*/
	},
	plotGroup: function(plotGroup) {
		var self = this;
		self.tableMessage = "load plots of "+this.project+" - "+plotGroup+" ...";
		self.plots = [];
		self.plot = undefined;
		if(plotGroup!=undefined) {
			var url_query = plotGroup=='*'?"region="+this.project:"generalstation="+plotGroup;
			Helper.getText(url_plot_list+"?"+url_query, 
				function(text) {
					self.tableMessage = "ok";
					var plots = splitData(text);
					self.plots = plots;
					self.plot = plots[0][0];
				},
				function(error) {
					self.tableMessage = "ERROR no plot group data loaded   "+error;
			});
		}
	},
	plot: function(plot) {
		var self = this;
		this.tableMessage = "query data of "+plot+" ...";
		
		Helper.getText(url_source_catalog+"?plot="+plot, 
			function(text) {
				var data = splitCSV(text);
				var header = data[0];
				data.shift();
				//console.log(header);
				//console.log(data);
				self.tableHeader = header;
				self.tableData = data;
				self.tableMessage = undefined;
			},
			function(error) {
				self.tableMessage = "ERROR loading catalog of "+self.project+"  "+error;
			}
		);
	},		
},

}); //end Vaue app
	
} //end init func