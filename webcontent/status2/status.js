"use strict";
document.addEventListener('DOMContentLoaded', function() {init();}, false);

var url_base = "../../";

var url_generalstation_list = url_base + "tsdb/generalstation_list";
var url_region_list = url_base + "tsdb/region_list";
var url_plot_status = url_base + "tsdb/status";

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

function init() {

Vue.component('demo-grid', {
  template: '#grid-template',
  replace: true,
  props: {
    data: Array,
    columns: Array,
	columnTitles: Array,
    filterKey: String
  },
  data: function () {
    var sortOrders = {}
    this.columns.forEach(function (key) {
      //sortOrders[key] = (key=='elapsed')?-1:1;
	  sortOrders[key] = 1;
    })
    return {
      sortKey: 'last_datetime',
      sortOrders: sortOrders
    }
  },
  computed: {
    filteredData: function () {
      var sortKey = this.sortKey
      var filterKey = this.filterKey && this.filterKey.toLowerCase()
      var order = this.sortOrders[sortKey] || 1
      var data = this.data
      if (filterKey) {
        data = data.filter(function (row) {
          return Object.keys(row).some(function (key) {
            return String(row[key]).toLowerCase().indexOf(filterKey) > -1
          })
        })
      }
      if (sortKey) {
		switch(sortKey) {
		case 'message':
			data = data.slice().sort(function (a, b) {
			  a = a[sortKey];
			  b = b[sortKey];
			  if(a==undefined) {
				  if(b==undefined) {
					return 0;
				  } else {
					return 1;
				  }
			  } else if(b==undefined) {
				  return -1;
			  }
			  if(a=='OK') {
				  if(b=='OK') {
					return 0;
				  } else {
					return -order;
				  }
			  } else if(b=='OK') {
				  return order;
			  }
			  return (a === b ? 0 : a > b ? 1 : -1) * order;
			});
			break;
		default:
			data = data.slice().sort(function (a, b) {
			  a = a[sortKey];
			  b = b[sortKey];
			  if(a==undefined) {
				  if(b==undefined) {
					return 0;
				  } else {
					return 1;
				  }
			  } else if(b==undefined) {
				  return -1;
			  }
			  return (a === b ? 0 : a > b ? 1 : -1) * order;
			});
		}
      }
      return data
    },
  },	

  filters: {
    capitalize: function (str) {
      return str.charAt(0).toUpperCase() + str.slice(1)
    }
  },
  methods: {
    sortBy: function (key) {
      if(this.sortKey == key) {	  
		this.sortOrders[key] = this.sortOrders[key] * -1;
	  }
	  this.sortKey = key;
    },
	
	elapsedStyle: function(days) {
		var timeMark = "timeMarkOneMonth";
		if(days>365) {
			timeMark = "timeMarkLost";
		} else if(days>7*4) {
			timeMark = "timeMarkOneMonth";
		} else if(days>7*2) {
			timeMark = "timeMarkTwoWeeks";
		} else if(days>7) {
			timeMark = "timeMarkOneWeek";
		} else {
			timeMark = "timeMarkNow";
		}
		return timeMark; 
	},
	
	voltageStyle: function(voltage) {
		var voltageMark = "voltageMarkNaN";
		if(voltage!=undefined) {
			if(voltage>15) {
				voltageMark = "voltageMarkNaN";
			} else if(voltage>=12.2) {
				voltageMark = "voltageMarkOK"; 
			}else if(voltage>=11.8) {
				voltageMark = "volageMarkWARN"; 
			}else if(voltage>=0) {
				voltageMark = "volageMarkCRITICAL"; 
			}				
		}
		return voltageMark;
	},
	
	isOutdated: function(entry) {
		if(entry.message_date != undefined) {
				return (entry.message_date<entry.last_datetime);
		}
		return false;
	},
	
  },
  
  watch: {

  },
})

var statusApp = new Vue({
  el: '#statusApp',
  
  data: {
	appReady: false,
	appMessage: "init...",
	tableReady: false,
	tableMessage: "init...",
	projects: [],
	project: "",
	plotgroups: [],
	plotgroup: "",
    searchQuery: '',
    gridColumns: ['plot', 'first_datetime', 'last_datetime', 'elapsed', 'voltage', 'message_date', 'message'],
	gridColumnTitles: ['Plot', 'First Timestamp', 'Last Timestamp', 'elapsed days', 'latest voltage', 'reception date', 'reception message'],
    gridData: [
      {"plot":"AEG01","first_timestamp":57498540,"last_timestamp":61455420,"first_datetime":"2009-04-26T13:00","last_datetime":"2016-11-03T09:00","voltage":15.31,"message_date":"2016-11-03T09:12","message":"OK"},
	  {"plot":"AEG02","first_timestamp":57495330,"last_timestamp":61456560,"first_datetime":"2009-04-24T07:30","last_datetime":"2016-11-04T04:00","voltage":12.67,"message_date":"2016-11-04T04:06:23","message":"OK"},
	  {"plot":"AEG03","first_timestamp":57494190,"last_timestamp":61448040,"first_datetime":"2009-04-23T12:30","last_datetime":"2016-10-29T06:00","voltage":12.98,"message_date":"2016-10-29T07:02","message":"OK"},
	  {"plot":"AEW47","first_timestamp":57495570,"last_timestamp":61427400,"first_datetime":"2009-04-24T11:30","last_datetime":"2016-10-14T22:00","voltage":12.1,"message_date":"2016-10-28T22:22","message":"AEW47: error[678] --> Der Remotecomputer hat nicht geantwortet. Führen Sie für den Remotecomputer einen Ping-Befehl aus, um sicherzustellen, dass der Server erreichbar ist."}
    ]
  },
  
  created: function () {
	this.appReady = false;  
	this.appMessage = "query projects...";
	
    this.projects = [];
    console.log('a is: ' + this.projects);
	var self = this;
	Helper.getText(url_region_list, 
	function(text) {
		self.projects = splitData(text);
		self.project = self.projects[0][0];
		self.appReady = true;
	},
	function(error) {
		this.appReady = false;
		self.appMessage = "ERROR "+error;	
	});
  },
  
  watch: {
    project: function (newProject) {
	  this.tableMessage = "query plot groups of "+this.project;
	  this.tableReady = false;
	  this.plotgroups = [];
	  this.plotgroup = undefined;
	  var self = this;
	  Helper.getText(url_generalstation_list+"?region="+newProject, 
	  function(text) {
		self.tableMessage = "ok";
	    var plotgroups = splitData(text);
		plotgroups.unshift(['*', '*']);
		self.plotgroups = plotgroups;
		self.plotgroup = self.plotgroups[0][0];
	  },
	  function(error) {
	    self.tableMessage = "ERROR no plot group data loaded   "+error;
	  });		
      console.log(newProject);
    },
	
	plotgroup: function(newPlotgroup) {
		this.gridData = [];
		
		if(newPlotgroup!=undefined) {
		
		this.tableMessage = "query data of "+this.project+" - "+newPlotgroup;
		this.tableReady = false;
		console.log("newPlotgroup "+newPlotgroup);
		
		var self = this;
		var url = url_plot_status+"?"+((newPlotgroup=='*')?('region='+this.project):('generalstation='+newPlotgroup));
		console.log(url);
	    Helper.getJSON(url, 
	    function(json) {
			console.log(json);

			var max_last = 0;
			for(var i in json) {
				if(max_last<json[i].last_timestamp) {
					max_last = json[i].last_timestamp;
				}			
			}
			for(var i in json) {
				var diff = max_last - json[i].last_timestamp;
				json[i].elapsed = parseInt(diff/(60*24));				
			}
			
			self.gridData = json;
			
			self.tableReady = true;
			self.tableMessage = "ok";
	    },
	    function(error) {
	       self.tableMessage = "ERROR no data loaded   "+error;
	    });

		}
	},
  },
  
})

}
