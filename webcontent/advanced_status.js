var url_base = "../";

var url_generalstation_list = url_base + "tsdb/generalstation_list";
var url_region_list = url_base + "tsdb/region_list";
var url_plot_status = url_base + "tsdb/status";
	

var region_select;
var generalstation_select;
var sort_by_select;

var tasks = 0;

var sort_by_text = ["first timestamp","last timestamp","plot","voltage"];

function getID(id) {
	return document.getElementById(id);
}

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

var incTask = function() {
	runDisabled(true);
	tasks++;	
	getID("status").innerHTML = "busy ("+tasks+")...";
	document.getElementById("busy_indicator").style.display = 'inline';
}

var decTask = function() {
	tasks--;
	if(tasks===0) {
		getID("status").innerHTML = "ready";
		document.getElementById("busy_indicator").style.display = 'none';
		runDisabled(false);
	} else if(tasks<0){
		getID("status").innerHTML = "error";
		document.getElementById("busy_indicator").style.display = 'none';
	} else {
		getID("status").innerHTML = "busy ("+tasks+")...";
		document.getElementById("busy_indicator").style.display = 'inline';
	}
}

function runDisabled(disabled) {
	$(".blockable").prop( "disabled",disabled);
}


$(document).ready(function(){
	incTask();
	region_select = $("#region_select");
	generalstation_select = $("#generalstation_select");
	sort_by_select = $("#sort_by_select");
	
	$.each(sort_by_text, function(i,row) {
		sort_by_select.append(new Option(row));
	});
	sort_by_select.val("last timestamp");
	
	getID("region_select").onchange = updateGeneralStations;
	getID("query_button").onclick = runQuery;
	
	updataRegions();
	decTask();
});

var updataRegions = function() {
	incTask();
	region_select.empty();
	$.get(url_region_list).done(function(data) {
		var rows = splitData(data);
		$.each(rows, function(i,row) {region_select.append(new Option(row[1],row[0]));});
		updateGeneralStations();
		decTask();
		if(rows.length==1) {
			$("#div_region_select").hide();
		} else {
			$("#div_region_select").show();
		}		
	}).fail(function() {region_select.append(new Option("[error]","[error]"));decTask();});
}

var updateGeneralStations = function() {
	incTask();
	var regionName = region_select.val();
	generalstation_select.empty();	
	$.get(url_generalstation_list+"?region="+regionName).done(function(data) {
		var rows = splitData(data);
		generalstation_select.append(new Option("[all]","[all]"));
		$.each(rows, function(i,row) {generalstation_select.append(new Option(row[1],row[0]));})
		decTask();	
	}).fail(function() {generalstation_select.append(new Option("[error]","[error]"));decTask();});
}

function sort_by_last_timestamp(a, b){return a.last_timestamp-b.last_timestamp;}
function sort_by_first_timestamp(a, b){return a.first_timestamp-b.first_timestamp;}
function sort_by_plot(a, b){
	if (a.plot < b.plot) return -1;
	if (a.plot > b.plot) return 1;
	return 0;
}
function sort_by_voltage(a, b){
	if(a.voltage==undefined) {
		return 1;
	}
	if(b.voltage==undefined) {
		return -1;
	}
	return a.voltage-b.voltage;
}


var runQuery = function() {
	getID("result").innerHTML = "Getting data from server. This may take some time...";
	generalStationName = generalstation_select.val();	
	var queryText = "";
	if(generalStationName==="[all]") {
		queryText = "region="+region_select.val();
	} else {
		queryText = "generalstation="+generalStationName;
	}
	
	incTask();
	$.getJSON(url_plot_status+"?"+queryText).done(function(interval) {
		getID("result").innerHTML = "";
		var min_last = 999999999;
		var max_last = 0;
		
		console.log(interval);
		
		var sort_func = sort_by_last_timestamp;
		
		switch(sort_by_select.val()) {
			case "first timestamp":
			sort_func = sort_by_first_timestamp;
			break;
			case "last timestamp":
			sort_func = sort_by_last_timestamp;
			break;
			case "plot":
			sort_func = sort_by_plot;
			break;
			case "voltage":
			sort_func = sort_by_voltage;
			break;			
			default:
			sort_func = sort_by_last_timestamp;
		}
		
		
		
		interval.sort(sort_func);
		console.log(interval);
		
		for(i in interval) {
			if(interval[i].last_timestamp<min_last) {
				min_last = interval[i].last_timestamp;
			}
			if(max_last<interval[i].last_timestamp) {
				max_last = interval[i].last_timestamp;
			}			
		}

		var info = "<table><tr><th>Plot</th><th>First Timestamp</th><th>Last Timestamp</th><th>elapsed days</th><th>latest voltage reading</th></tr>";
		
		for(i in interval) {
			var t = max_last - interval[i].last_timestamp;
			var timeMark = "timeMarkOneMonth";
			if(t>60*24*365) {
				timeMark = "timeMarkLost";
			} else if(t>60*24*7*4) {
				timeMark = "timeMarkOneMonth";
			} else if(t>60*24*7*2) {
				timeMark = "timeMarkTwoWeeks";
			} else if(t>60*24*7) {
				timeMark = "timeMarkOneWeek";
			} else {
				timeMark = "timeMarkNow";
			}
			
			info += "<tr>";
			info += "<td>"+interval[i].plot+"</td>";
			info += "<td>"+interval[i].first_datetime+"</td>";
			info += "<td>"+interval[i].last_datetime+"</td>";
			info += "<td id=\""+timeMark+"\">"+parseInt((max_last - interval[i].last_timestamp)/(60*24))+"</td>";
			var voltage = "";
			var voltageMark = "voltageMarkNaN";
			if(interval[i].voltage!=undefined) {
				voltage = interval[i].voltage;
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

			info += "<td id=\""+voltageMark+"\">"+voltage+"</td>";
			info += "</tr>";

		}
		
		info += "</table>";
		getID("result").innerHTML = info;
		decTask();
	}).fail(function() {getID("result").innerHTML = "error";decTask();});
}