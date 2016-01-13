var url_base = "../";

var url_sensor_list = url_base + "tsdb/sensor_list";
var url_export_region = url_base + "export/region";
var url_export_sensors = url_base + "export/sensors";
var url_export_apply_sensors = url_base + "export/apply_sensors";
var url_result_page = "export.html";

var chosen_select;
var available_select;

var chosen_sensors = [];
var available_sensors = [];

var tasks = 0;

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

function update_chosen_select() {
	chosen_select.innerHTML = "";
	for(i in chosen_sensors) {
		chosen_select.add(new Option(chosen_sensors[i],i));
	}
}

function update_available_select() {
	available_select.innerHTML = "";
	for(r in available_sensors) {
		var sensor = available_sensors[r];
		var available = true;	
		for(i in chosen_sensors) {
			if(sensor==chosen_sensors[i]) {
				available = false;
				break;
			}			
		}
		if(available) {
			available_select.add(new Option(sensor,r));
		}
	}
}

function arrayToText(a) {
	var s = "";
	for(i in a) {
		if(i>0) {
			s+="\n";
		}
		s+=a[i];
	}
	return s;
}

var busy_count = 0;

function setStateBusy() {
	busy_count++;
	document.getElementById("busy").style.visibility = "visible";
}

function setStateReady() {
	busy_count--;
	if(busy_count<1) {
		document.getElementById("busy").style.visibility = "hidden";
	}
}

function on_choose_selected_button() {
	setStateBusy();
	$.each(available_select.selectedOptions, function(i,option) {
		chosen_sensors.push(available_sensors[option.value]);
	});
	update_chosen_select();
	update_available_select();	
	setStateReady();
}

function on_remove_selected_button() {
	setStateBusy();
	var removed_count = 0;
	$.each(chosen_select.selectedOptions, function(i,option) {
		chosen_sensors.splice(option.value-removed_count,1);
		removed_count++;
	});		
	update_chosen_select();
	update_available_select();
	setStateReady();
}

function on_choose_all_button() {
	setStateBusy();
	$.each(available_select, function(i,option) {
		chosen_sensors.push(available_sensors[option.value]);
	});
	update_chosen_select();
	update_available_select();	
	setStateReady();
}

function on_remove_all_button() {
	setStateBusy();
	chosen_sensors = [];	
	update_chosen_select();
	update_available_select();
	setStateReady();
	/*chosen_plots = [];
	update_chosen_select();
	update_available_select();*/
}

$(document).ready(function(){
	setStateBusy();
	
	chosen_select = document.getElementById("chosen_select");
	available_select = document.getElementById("available_select");

	chosen_select.ondblclick = function(){
		setStateBusy();
		chosen_sensors.splice(chosen_select.value,1);
		update_chosen_select();
		update_available_select();
		setStateReady();
	};
	
	available_select.ondblclick = function(){
		setStateBusy();
		chosen_sensors.push(available_sensors[available_select.value]);
		update_chosen_select();
		update_available_select();
		setStateReady();
	}
	
	//$("#button_cancel").button();
	document.getElementById("button_cancel").onclick = function() {
		setStateBusy();
		window.location = url_result_page;
		setStateReady();
	}
	
	//$("#button_apply").button();
	document.getElementById("button_apply").onclick = function() {	
		setStateBusy();
		$.post(url_export_apply_sensors,arrayToText(chosen_sensors))
		 .done(function() {
			window.location = url_result_page;
			setStateReady();
		 })
		 .fail(function() {alert("error sending data");setStateReady();})		 
	}
	
	getID("choose_selected_button").onclick = on_choose_selected_button;
	getID("remove_selected_button").onclick = on_remove_selected_button;
	getID("choose_all_button").onclick = on_choose_all_button;
	getID("remove_all_button").onclick = on_remove_all_button;	
	
	setStateBusy();
	$.get(url_export_sensors).done(function(data) {		
		chosen_sensors = [];
		console.log("data: ["+data+"]");
		rows = splitData(data);
		for(i in rows) {
			console.log("row: ["+rows[i]+"]");
			chosen_sensors.push(rows[i][0]);
		}
		update_chosen_select();
		update_available_select();
		setStateReady();
	}).fail(function() {alert("error getting data");setStateReady();});
	
	function get_sensor_list(region) {
		setStateBusy();
		$.get(url_sensor_list+"?raw=true&region="+region).done(function(data) {
			available_sensors = [];
			rows = splitData(data);
			for(i in rows) {
				available_sensors.push(rows[i][0]);
			}
			update_chosen_select();
			update_available_select();
			setStateReady();
		}).fail(function() {alert("error getting data");setStateReady();});
	}

	function get_region() {
		incTask();
		$.get(url_export_region).done(function(data) {
			var rows = splitData(data);	
			var region = rows[0][0];
			get_sensor_list(region);
			decTask();
		}).fail(function() {alert("error getting data");decTask();});	
	}

	get_region();
	update_chosen_select();
	update_available_select();
	
	setStateReady();
})