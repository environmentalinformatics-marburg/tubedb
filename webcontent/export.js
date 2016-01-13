var url_base = "../";

var url_region_list = url_base + "tsdb/region_list";
var url_export_reset = url_base + "export/reset";
var url_export_region = url_base + "export/region";
var url_export_plots = url_base + "export/plots";
var url_export_sensors = url_base + "export/sensors";
var url_export_settings = url_base + "export/settings";

var tasks = 0;

function getID(id) {
	return document.getElementById(id);
}

var clear = function(element) {
	element.textContent = "";
}

var println = function(element,text) {
	element.appendChild(document.createElement("p")).appendChild(document.createTextNode(text));
}

var clear_println = function(element,text) {
	element.textContent = "";
	element.appendChild(document.createElement("p")).appendChild(document.createTextNode(text));
}

var array_to_html_list = function(element,array) {
	var list = element.appendChild(document.createElement("ul"));
	for(i in array) {
		list.appendChild(document.createElement("li")).appendChild(document.createTextNode(array[i]));
	}
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

function incTask() {
	tasks++;	
	getID("status").innerHTML = "busy ("+tasks+")...";
}

function decTask() {
	tasks--;
	if(tasks===0) {
		getID("status").innerHTML = "ready";
	} else if(tasks<0){
		getID("status").innerHTML = "error";
	} else {
		getID("status").innerHTML = "busy ("+tasks+")...";
	}
}

$(document).ready(function(){
incTask();
var plots_output = getID("plots_output");
var sensors_output = getID("sensors_output");
var settings_output = getID("settings_output");
var region_output = getID("region_output");
var timespan_output = getID("timespan_output");
var aggregation_output = getID("aggregation_output");

clear_println(region_output,"query data...");
incTask();
$.get(url_export_region).done(function(data) {
		var rows = splitData(data);
		clear(region_output);		
		region_output.innerHTML = rows[0][1];
		decTask();
	}).fail(function() {clear_println(region_output,"error getting data");decTask();});
	
clear_println(timespan_output,"query data...");

clear_println(plots_output,"query data...");
incTask();
$.get(url_export_plots).done(function(data) {
		clear(plots_output);
		var rows = splitData(data);
		for(i in rows) {
			println(plots_output, rows[i]);
		}
		decTask();		
	}).fail(function() {clear_println(plots_output,"error getting data");decTask();});

clear_println(sensors_output,"query data...");
incTask();	
$.get(url_export_sensors).done(function(data) {
		clear(sensors_output);
		var rows = splitData(data);
		for(i in rows) {
			println(sensors_output, rows[i]);
		}
		decTask();
	}).fail(function() {clear_println(sensors_output,"error getting data");decTask();});
	
clear_println(settings_output,"query data...");
incTask();
$.getJSON(url_export_settings).done(function( data ) {
		var json_settings = data;
		console.log(JSON.stringify(json_settings));		
		clear(settings_output);
		var settings_array = [];
		//{"col_plotid":true,"col_timestamp":false,"col_datetime":false}
		
		var timespan_type = json_settings.timespan_type;
		
		var timespanText;
		if(timespan_type=="all") {
			timespanText = "[all available data]";
		} else if(timespan_type=="year"){
			timespanText = "year "+json_settings.timespan_year;
		} else if(timespan_type=="years"){
			timespanText = "years "+json_settings.timespan_years_from+" to "+json_settings.timespan_years_to;
		} else if(timespan_type=="dates"){
			timespanText = "dates from "+json_settings.timespan_dates_from+" to "+json_settings.timespan_dates_to;			
			
		} else {
			timespanText = "[not valid timespan]";
		}		
		
		/*var value = +json_settings.timespan;
		var timespanText = "[not valid timespan]";
		if(value===0) {
			timespanText = "[all]";
		} else {
			timespanText = "year "+value;
		}*/		
		timespan_output.innerHTML = timespanText;
		aggregation_output.innerHTML = json_settings.timestep;
		var raw = false;
		if(json_settings.timestep=="raw") {
			aggregation_output.innerHTML = "collect raw measured values:  no quality checks or interpolation can be performed";
			raw = true;
		}		
		if(!raw) {
			settings_array.push("quality check: "+json_settings.quality);		
			if(json_settings.interpolate) {
				settings_array.push("interpolate missing values");
			}
		}		
		if(json_settings.desc_sensor) {
			settings_array.push("include sensor description");
		}
		if(json_settings.desc_plot) {
			settings_array.push("include plot description");
		}		
		if(json_settings.desc_settings) {
			settings_array.push("include settings description");
		}
		if(json_settings.allinone) {
			settings_array.push("all plots in one file");
		} else {
			settings_array.push("one file per plot");
		}
		if(!json_settings.write_header) {
			settings_array.push("not write CSV header");
		}		
		array_to_html_list(settings_output,settings_array);
		decTask();
	})
	.fail(function(data) {clear_println(settings_output,"error getting data");decTask();});	

document.getElementById("choose_region").onclick = function() {
	window.location = "export_region.html";
}	
	
document.getElementById("choose_sensors").onclick = function() {
	window.location = "export_sensors.html";
}		
	
document.getElementById("choose_plots").onclick = function() {
	window.location = "export_plots.html";
}

document.getElementById("choose_time").onclick = function() {
	window.location = "export_time.html";
}

//$("#download").button();
/*document.getElementById("download").onclick = function() {
	incTask();
	window.location = "../export/result.zip";
	decTask();
}*/

document.getElementById("create").onclick = function() {
	window.location = "export_create.html";
}

document.getElementById("button_reset").onclick = function() {
	incTask();
	$.post(url_export_reset)
		 .done(function() {
			document.location.reload();
			decTask();
		 })
		 .fail(function(jqXHR, textStatus, errorThrown) {
			alert("error sending reset: "+textStatus+"  "+errorThrown);
			decTask();
	});
}



document.getElementById("button_settings").onclick = function() {window.location = "export_settings.html";};	
decTask();

//incTask();	
$.get(url_region_list).done(function(data) {
		var rows = splitData(data);
		if(rows.length<2) {
			$("#choose_region_div")[0].style.display="none";
			$("#div_region").hide();
		}
		//decTask();
	}).fail(function() {/*clear_println(sensors_output,"error getting data");decTask();*/});

});