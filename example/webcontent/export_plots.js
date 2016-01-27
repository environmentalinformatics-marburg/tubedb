var url_base = "../";

var url_generalstation_list = url_base + "tsdb/generalstation_list";
var url_plot_list = url_base + "tsdb/plot_list";
var url_region_plot_list = url_base + "tsdb/region_plot_list";
var url_export_region = url_base + "export/region";
var url_export_plots = url_base + "export/plots";
var url_export_apply_plots = url_base + "export/apply_plots";
var url_result_page = "export.html";

var chosen_select;
var available_select;

var chosen_plots = [];
var available_plots = [];

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
	for(i in chosen_plots) {
		chosen_select.add(new Option(chosen_plots[i],i));
	}
}

function update_available_select() {
	available_select.innerHTML = "";
	for(r in available_plots) {
		var plot = available_plots[r];
		var available = true;	
		for(i in chosen_plots) {
			if(plot==chosen_plots[i]) {
				available = false;
				break;
			}			
		}
		if(available) {
			available_select.add(new Option(plot,r));
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

function on_choose_selected_button() {
	$.each(available_select.selectedOptions, function(i,option) {
		chosen_plots.push(available_plots[option.value]);
	});
	update_chosen_select();
	update_available_select();
}

function on_remove_selected_button() {
	var removed_count = 0;
	$.each(chosen_select.selectedOptions, function(i,option) {
		chosen_plots.splice(option.value-removed_count,1);
		removed_count++;
	});	
	update_chosen_select();
	update_available_select();
}

function on_choose_all_button() {
	$.each(available_select, function(i,option) {
		chosen_plots.push(available_plots[option.value]);
	});
	update_chosen_select();
	update_available_select();
}

function on_remove_all_button() {
	chosen_plots = [];
	update_chosen_select();
	update_available_select();
}

$(document).ready(function(){
	
	chosen_select = document.getElementById("chosen_select");
	available_select = document.getElementById("available_select");	
	
	chosen_select.ondblclick = function(){
		chosen_plots.splice(chosen_select.value,1);
		update_chosen_select();
		update_available_select();
	};
	
	available_select.ondblclick = function(){
		chosen_plots.push(available_plots[available_select.value]);
		update_chosen_select();
		update_available_select();
	}
	
	document.getElementById("button_cancel").onclick = function() {
		window.location = url_result_page;
	}
	
	document.getElementById("button_apply").onclick = function() {	
		$.post(url_export_apply_plots,arrayToText(chosen_plots))
		 .done(function() {
			window.location = url_result_page;
		 })
		 .fail(function() {alert("error sending data");})		 
	}
	
	getID("choose_selected_button").onclick = on_choose_selected_button;
	getID("remove_selected_button").onclick = on_remove_selected_button;
	getID("choose_all_button").onclick = on_choose_all_button;
	getID("remove_all_button").onclick = on_remove_all_button;	
	
	$.get(url_export_plots).done(function(data) {
		chosen_plots = [];
		rows = splitData(data);
		for(i in rows) {
			chosen_plots.push(rows[i][0]);
		}
		update_chosen_select();
		update_available_select();
	}).fail(function() {alert("error getting data");});
	
	function update_filtered_available_select_from_regionName(regionName) {
		$.get(url_region_plot_list+"?region="+regionName).done(function(data) {
		var rows = splitData(data);
		for(i in rows) {
			available_plots.push(rows[i][0]);
		}
		update_chosen_select();
		update_available_select();
		}).fail(function() {plot_input.append(new Option("[error]","[error]"));});	
	}
	
	function update_filtered_available_select_from_region() {
		incTask();
		$.get(url_export_region).done(function(data) {
			var rows = splitData(data);	
			var region = rows[0][0];
			update_filtered_available_select_from_regionName(region);
			decTask();
		}).fail(function() {alert("error getting data");decTask();});		
	}
	
	function update_filtered_available_select_from_generalstation(generalstationName) {
		$.get(url_plot_list+"?generalstation="+generalstationName).done(function(data) {
			var rows = splitData(data);
			for(i in rows) {
				available_plots.push(rows[i][0]);
			}
			update_chosen_select();
			update_available_select();
		}).fail(function() {plot_input.append(new Option("[error]","[error]"));});	
	}	
	
	function update_filtered_available_select() {
		var generalstationName = getID("general_select").value;
		available_plots = [];
		if(generalstationName==="all") {
			update_filtered_available_select_from_region();			
		} else {
			update_filtered_available_select_from_generalstation(generalstationName);
		}
	}	
	
	function update_general_select(regionName) {
		incTask();
		var general_select = $("#general_select");
		general_select.empty();
		$.get(url_generalstation_list+"?region="+regionName).done(function(data) {
			var rows = splitData(data);
			general_select.append(new Option("[all]","all"));
			$.each(rows, function(i,row) {general_select.append(new Option(row[1],row[0]));})
			update_filtered_available_select();
			decTask();		
		}).fail(function() {general_select.append(new Option("[error]","[error]"));decTask();});		
	}
	
	incTask();
	$.get(url_export_region).done(function(data) {
		var rows = splitData(data);	
		var region = rows[0][0];
		update_general_select(region);
		decTask();
	}).fail(function() {alert("error getting data");decTask();});	
	
	getID("general_select").onchange = update_filtered_available_select;	

	update_chosen_select();
	update_available_select();
});