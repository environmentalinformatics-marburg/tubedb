/* *** settings that may need to be changed at different runtime environments *** */
var url_base = "../";
var url_result_page = "export.html";
/* ***   *** */

var url_export_settings = url_base + "export/settings";
var url_export_apply_settings = url_base + "export/apply_settings";

var tasks = 0;

function getID(id) {
	return document.getElementById(id);
}

$.postJSON = function(url, data, callback) {
    return jQuery.ajax({
        'type': 'POST',
        'url': url,
        'contentType': 'application/json',
        'data': JSON.stringify(data),
        'success': callback
    });
};

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

var json_settings;

$(document).ready(function(){
	incTask();	
	document.getElementById("button_cancel").onclick = function() {
		window.location = url_result_page;
	}
	
	document.getElementById("button_apply").onclick = function() {
		incTask();
		
		json_settings.interpolate = document.getElementById("interpolate").checked;
		json_settings.desc_sensor = document.getElementById("desc_sensor").checked;
		json_settings.desc_plot = document.getElementById("desc_plot").checked;
		json_settings.desc_settings = document.getElementById("desc_settings").checked;
		json_settings.allinone = document.getElementById("allinone").checked;
		json_settings.quality = document.getElementById("choose_quality").value;
		json_settings.col_plotid = document.getElementById("col_plotid").checked;
		json_settings.col_timestamp = document.getElementById("col_timestamp").checked;
		json_settings.col_datetime = document.getElementById("col_datetime").checked;
		json_settings.col_year = document.getElementById("col_year").checked;
		json_settings.col_month = document.getElementById("col_month").checked;
		json_settings.col_day = document.getElementById("col_day").checked;
		json_settings.col_hour = document.getElementById("col_hour").checked;
		json_settings.col_day_of_year = document.getElementById("col_day_of_year").checked;
		json_settings.col_qualitycounter = document.getElementById("col_qualitycounter").checked;
		json_settings.write_header = document.getElementById("write_header").checked;
		json_settings.casted = document.getElementById("allinoneColumns").checked;

		$.postJSON(url_export_apply_settings,json_settings)
		 .done(function() {
			window.location = url_result_page;
			decTask();
		 })
		 .fail(function(jqXHR, textStatus, errorThrown) {alert("error sending settings data: "+textStatus+"  "+errorThrown);decTask();});	 
	}
	
	incTask();
	$.getJSON(url_export_settings).done(function( data ) {
		json_settings = data;
		console.log(json_settings);
		
		document.getElementById("interpolate").checked = json_settings.interpolate;
		document.getElementById("desc_sensor").checked = json_settings.desc_sensor;
		document.getElementById("desc_plot").checked = json_settings.desc_plot;
		document.getElementById("desc_settings").checked = json_settings.desc_settings;
		document.getElementById("allinone").checked = json_settings.allinone;
		document.getElementById("choose_quality").value = json_settings.quality;
		document.getElementById("col_plotid").checked = json_settings.col_plotid;
		document.getElementById("col_timestamp").checked = json_settings.col_timestamp;
		document.getElementById("col_datetime").checked = json_settings.col_datetime;
		document.getElementById("col_year").checked = json_settings.col_year;
		document.getElementById("col_month").checked = json_settings.col_month;
		document.getElementById("col_day").checked = json_settings.col_day;
		document.getElementById("col_hour").checked = json_settings.col_hour;
		document.getElementById("col_day_of_year").checked = json_settings.col_day_of_year;
		document.getElementById("col_qualitycounter").checked = json_settings.col_qualitycounter;
		document.getElementById("write_header").checked = json_settings.write_header;

		if(json_settings.spatial_aggregation === "aggregated") {
			document.getElementById("allinone").disabled = true;
		} else {
			document.getElementById("allinone").disabled = false;
		}

		document.getElementById("allinoneRows").checked = false;
		document.getElementById("allinoneColumns").checked = false;
		if(json_settings.casted !== undefined && json_settings.casted) {
			document.getElementById("allinoneColumns").checked = true;
		} else {
			document.getElementById("allinoneRows").checked = true;
		}
		
		var raw = false;
		if(json_settings.timestep=="raw") {
			raw = true;
			document.getElementById("interpolate").disabled = true;
			document.getElementById("div_interpolate").style.color = "silver";
			document.getElementById("choose_quality").disabled = true;
			document.getElementById("div_quality").style.color = "silver";
		}		
		
		update();
		
		decTask();
	})
	.fail(function(data) {alert("error getting settings data: "+data);decTask();});

	decTask();
});

function update() {
	if((!document.getElementById("allinone").disabled) && document.getElementById("allinone").checked) {
		document.getElementById("allinoneRows").disabled = false;
		document.getElementById("allinoneColumns").disabled = false;
	} else {
		document.getElementById("allinoneRows").disabled = true;
		document.getElementById("allinoneColumns").disabled = true;
	}
	
	if(document.getElementById("allinone").disabled || (document.getElementById("allinone").checked && document.getElementById("allinoneColumns").checked)) {
		document.getElementById("col_plotid").disabled = true;
	} else {
		document.getElementById("col_plotid").disabled = false;
	}
}