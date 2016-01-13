var url_base = "../";

var url_region_list = url_base + "tsdb/region_list";
var url_export_region = url_base + "export/region";
var url_export_applay_region = url_base + "export/apply_region";
var url_result_page = "export.html";

var tasks = 0;

var region_radios = [];

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

function getRegion() {
	incTask();
	$.get(url_export_region).done(function(data) {
		var rows = splitData(data);	
		var region = rows[0][0];
		getID(region).checked = true;
		decTask();
	}).fail(function() {alert("error getting data");decTask();});
}

function updataRegions() {
	incTask();
	var region_div = getID("region_div");
	region_div.innerHTML = "";
	$.get(url_region_list).done(function(data) {
		var rows = splitData(data);
		$.each(rows, function(i,row) {
			var radio = document.createElement("input");
			radio.setAttribute("type", "radio");
			radio.name = "reg";
			radio.id = row[0];
			var p = region_div.appendChild(document.createElement("p"));
			p.appendChild(radio);
			p.appendChild(document.createTextNode(row[1]));
			region_radios.push(radio);
		});
		getRegion();
	    decTask();
	}).fail(function() {region_select.append(new Option("[error]","[error]"));decTask();});
}

function on_cancel() {
	window.location = url_result_page;
}

function on_apply() {
	var region = "[region]";
	for(i in region_radios) {
		if(region_radios[i].checked) {
			region = region_radios[i].id;
		}
	}
	incTask();	
	$.post(url_export_applay_region+"?region="+region,region)
		 .done(function() {
			window.location = url_result_page;
			decTask();
		 })
		 .fail(function(jqXHR, textStatus, errorThrown) {
			alert("error sending settings data: "+textStatus+"  "+errorThrown);
			decTask();
		 });	
}

function ready() {
	incTask();
	updataRegions();
	document.getElementById("button_cancel").onclick = on_cancel;
	document.getElementById("button_apply").onclick = on_apply;
	decTask();
}

$(document).ready(ready);