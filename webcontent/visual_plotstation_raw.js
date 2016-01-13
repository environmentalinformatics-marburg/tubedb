var url_base = "../";

var url_region_list = url_base + "tsdb/region_list";
var url_generalstation_list = url_base + "tsdb/generalstation_list";
var url_plot_list = url_base + "tsdb/plot_list";
var url_plotstation_list = url_base + "tsdb/plotstation_list";
var url_sensor_list = url_base + "tsdb/sensor_list";
var url_query_diagram = url_base + "tsdb/query_image";
var url_query_heatmap = url_base + "tsdb/query_heatmap";
var url_heatmap_scale = url_base + "tsdb/heatmap_scale"; 
var url_query_csv = url_base + "tsdb/query_csv";

var time_year_text = ["[all]","2008","2009","2010","2011","2012","2013","2014","2015","2016"];
var time_month_text = ["year","jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec"];
var aggregation_text = ["raw","hour","day","week","month","year"];
var aggregation_raw_text = ["raw"];
var quality_name = ["no", "physical", "step", "empirical"];
var quality_text = ["0: no","1: physical","2: physical + step","3: physical + step + empirical"];
var type_raw_text = ["graph","table","csv file"];
var type_hour_text = ["graph","heatmap","table","csv file"];
var type_high_aggregated_text = ["graph","boxplot","table","csv file"];
var type_general_text = ["graph","table","csv file"];
var magnification_factors = [1,2,3,4];

var sensor_rows = [];

var tasks = 0;

var region_select;
var generalstation_select;
var plot_select;
var station_select;
var sensor_select;
var time_year_select;
var time_month_select;
var aggregation_select;
var quality_select;
var interpolation_checkbox;
var type_select;
var scale_checkbox;
var magnification_select;

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

function splitCsv(data) {
	var lines = data.split(/\n/);
	var rows = [];
	for (var i in lines) {
		if(lines[i].length>0) {
			rows.push(lines[i].split(','));
		}
	}
	return rows;
}

function ready_to_run(ready) {
	$(".blockable").prop( "disabled",!ready);
	updateStationVisibility();
}

function incTask() {
	ready_to_run(false);
	tasks++;	
	getID("status").innerHTML = "busy ("+tasks+")...";
	document.getElementById("busy_indicator").style.display = 'inline';
}

function decTask() {
	tasks--;
	if(tasks===0) {
		getID("status").innerHTML = "ready";
		document.getElementById("busy_indicator").style.display = 'none';
		ready_to_run(true);
	} else if(tasks<0){
		getID("status").innerHTML = "error";
		document.getElementById("busy_indicator").style.display = 'none';
	} else {
		getID("status").innerHTML = "busy ("+tasks+")...";
		document.getElementById("busy_indicator").style.display = 'inline';
	}
}

function document_ready() {
	region_select = $("#region_select");
	generalstation_select = $("#generalstation_select");
	plot_select = $("#plot_select");
	station_select = $("#station_select");
	sensor_select = $("#sensor_select");
	time_year_select = $("#time_year_select");
	time_month_select = $("#time_month_select");
	aggregation_select = $("#aggregation_select");
	quality_select = $("#quality_select");
	interpolation_checkbox = $("#interpolation_checkbox");
	type_select = $("#type_select");
	scale_checkbox = $("#scale_checkbox");
	magnification_select = $("#magnification_select");
	
	region_select[0].onchange = onRegionChange;
	generalstation_select[0].onchange = onGeneralstationChange;
	plot_select[0].onchange = onPlotChange;
	station_select[0].onchange = onStationChange;
	sensor_select[0].onchange = onSensorChange;
	time_year_select[0].onchange = onTimeYearChange;
	aggregation_select[0].onchange = onAggregationChange;
	type_select[0].onchange = onTypeChange;
	getID("button_visualise").onclick = onVisualiseClick;
	
	incTask();
	
	updateRegions();
	updateTimeYears();
	//updateAggregations();
	updateQualities();
	updateTypes();
	updateMagnification();
	
	onTypeChange();
	
	decTask();
}

function updateRegions() {
	incTask();
	region_select.empty();
	$.get(url_region_list).done(function(data) {
		var rows = splitData(data);
		$.each(rows, function(i,row) {region_select.append(new Option(row[1],row[0]));});
		onRegionChange();
		decTask();
		if(rows.length==1) {
			$("#div_region_select").hide();
		} else {
			$("#div_region_select").show();
		}			
	}).fail(function() {region_select.append(new Option("[error]","[error]"));decTask();});
}

function onRegionChange() {
	updateGeneralStations();
}

function updateGeneralStations() {
	incTask();
	var regionName = region_select.val();
	generalstation_select.empty();	
	$.get(url_generalstation_list+"?region="+regionName).done(function(data) {
		var rows = splitData(data);
		if(rows.length>1) {
			generalstation_select.append(new Option("[all]","[all]"));
		}
		$.each(rows, function(i,row) {
			generalstation_select.append(new Option(row[1],row[0]));
		})		
		onGeneralstationChange();
		decTask();	
	}).fail(function() {generalstation_select.append(new Option("[error]","[error]"));decTask();});
}

function onGeneralstationChange() {
	updatePlots();
}

function updatePlots() {
	incTask();
	var generalstationName = generalstation_select.val();
	plot_select.empty();
	var query = "?generalstation="+generalstationName;
	if(generalstationName=="[all]") {
		var regionName = region_select.val();
		query = "?region="+regionName;
	}
	$.get(url_plot_list+query).done(function(data) {
		var rows = splitData(data);
		$.each(rows, function(i,row) {plot_select.append(new Option(row[0]+(row[2]!="virtual"&&row[2]!="unknown"?" <"+row[2]+">":"")+(row[1]=="vip"?" (vip)":""),row[0]));})
		onPlotChange();
		decTask();
	}).fail(function() {plot_select.append(new Option("[error]","[error]"));decTask();});
}

function onPlotChange() {
	updateStations();
}

function updateStations() {
	incTask();
	var plotName = plot_select.val();
	station_select.empty();	
	$.get(url_plotstation_list+"?plot="+plotName).done(function(data) {
		var rows = splitData(data);
		if(rows.length>1) {
			station_select.append(new Option("[unified]","[unified]"));
		}
		$.each(rows, function(i,row) {station_select.append(new Option(row[0]+" <"+row[1]+">",row[0]));})
		onStationChange();
		decTask();
	}).fail(function() {station_select.append(new Option("[error]","[error]"));decTask();});
}

function onStationChange() {
	updateStationVisibility();
	updateSensors();
}

function updateStationVisibility() {
	if(station_select.val()!=undefined) {
		//getID("button_visualise").disabled = false;
		$("#div_station_select").show();
	} else {
		//getID("button_visualise").disabled = true;
		$("#div_station_select").hide();
	}	
}

function updateSensors() {
	incTask();
	
	var prevSensorName = "";
	if(sensor_select.val() != undefined && sensor_select.val() != -1) {
		prevSensorName = sensor_rows[sensor_select.val()][0];
	}
	var plotName = plot_select.val();
	if(plotName == undefined) {
		sensor_select.empty();
		decTask();
		return;
	}
	var sensorQuery = "plot="+plotName;
	if(station_select.val()!=undefined && station_select.val()!="[unified]") {
		var stationName = station_select.val();
		sensorQuery = "station="+stationName;
	}
	sensorQuery += "&raw=true";
	sensor_select.empty();	
	$.get(url_sensor_list+"?"+sensorQuery).done(function(data) {
		var rows = splitData(data);
		var curIndex = 0;
		if(rows.length>1) {
			sensor_select.append(new Option("[all]",-1));
			curIndex = -1;
		}
		
		$.each(rows, function(i,row) {
			var sensorTitle = row[0];
			if(row[4]=="true") {
				if(row[3]==="NONE") {
					//sensorTitle += " raw-internal";
					sensorTitle += " internal";
				} else {
					sensorTitle += " internal";
				}
			} else if(row[3]==="NONE") {
				sensorTitle += " raw";
			}
			sensor_select.append(new Option(sensorTitle,i));
			if(row[0]==prevSensorName) {
				curIndex = i;
			}
		});
		sensor_select.val(curIndex);
		sensor_rows = rows;
		onSensorChange();
		decTask();
	}).fail(function() {sensor_select.append(new Option("[error]","[error]"));decTask();});
}

function onSensorChange() {
	console.log("sensor changed");
	updateAggregations();
}

function getStationOrPlotName() {
	var name = station_select.val();
	if(name == undefined || name == "[unified]") {
		name = plot_select.val()
	}
	return name;
}

function onVisualiseClick() {
	incTask();
	getID("div_result").innerHTML = "query...";
	
	var width = getID("div_result").clientWidth - 30;
	var height = 400;
	
	var boxplot = false;
	var plotName = plot_select.val();
	var sensorQuery = "plot="+plotName;
	if(station_select.val()!=undefined && station_select.val()!="[unified]") {
		var stationName = station_select.val();
		sensorQuery = "station="+stationName;
		plotName += ":"+stationName;
	}
	var aggregationName = aggregation_select.val();
	var type = type_select[0].options[type_select.val()].text;
	var sensorIndex = sensor_select.val();
	if(sensorIndex == -1) {		
		getID("div_result").innerHTML = "";
		switch(type) {
			case "boxplot":
				boxplot = true;
			case "graph":
				$.each(sensor_rows, function(i,row) {
					if(aggregationName=="raw" || row[3]!="NONE") {
						addDiagram(plotName,row[0],row[1],row[2],boxplot,width,height);
					}
				});
				break;
			case "heatmap":
				$.each(sensor_rows, function(i,row) {
					if(row[3]!="NONE") {
						addHeatmap(plotName,row[0],row[1],row[2]);
					}
				});
				break;				
			case "table":
				var query_rows = [];
				console.log("start");
				for(i in sensor_rows) {
					var sensor_row = sensor_rows[i];
					if(aggregationName=="raw" || sensor_row[3]!="NONE") {
						query_rows.push(sensor_row);
					}
				}
				addTable(plotName,query_rows);
				break;
			case "csv file":
				var query_rows = [];
				console.log("start");
				for(i in sensor_rows) {
					var sensor_row = sensor_rows[i];
					if(aggregationName=="raw" || sensor_row[3]!="NONE") {
						query_rows.push(sensor_row);
					}
				}
				addCSVFile(plotName,query_rows);
				break;
			default:
				getID("div_result").innerHTML = "unknown query type";
				break;
		}
		decTask();
	} else if(sensorIndex != undefined){		
		getID("div_result").innerHTML = "";
		var row = sensor_rows[sensorIndex];
		switch(type) {
			case "boxplot":
				boxplot = true;			
			case "graph":
				addDiagram(plotName,row[0],row[1],row[2],boxplot,width,height);
				break;
			case "heatmap":
				addHeatmap(plotName,row[0],row[1],row[2]);
				break;				
			case "table":
				var sensors = [row];
				addTable(plotName,sensors);
				break;
			case "csv file":
				var sensors = [row];
				addCSVFile(plotName,sensors);
				break;				
			default:
				getID("div_result").innerHTML = "unknown query type";
				break;
		}
		decTask();
	} else {
		getID("div_result").innerHTML = "no query";
		decTask();
	}
}

function addDiagram(plotName, sensorName, sensorDesc, sensorUnit, boxplot, width, height) {
	incTask();
	var sensorResult = getID("div_result").appendChild(document.createElement("div"));
	var sensorResultTitle = sensorResult.appendChild(document.createElement("div"));
	sensorResultTitle.innerHTML += "query "+sensorName+"...";
	var aggregationName = aggregation_select.val();
	var qualityName = "step";
	var qualityName = quality_name[quality_select.val()];
	var interpolatedName = interpolation_checkbox[0].checked;
	var timeParameter = "";
	if(time_year_select.val()!=0) {
		timeParameter = "&year="+time_year_text[time_year_select.val()];
		var month = time_month_select.val();
		if(month!=0) {
			timeParameter += "&month="+month;
		}
	}
	incTask();
	var image = new Image();
	sensorResult.appendChild(image);
	image.onload = function() {
		sensorResultTitle.innerHTML = sensorName+": "+sensorDesc+" - "+sensorUnit;
		decTask();
	}
	image.onerror = function() {	
		sensorResultTitle.innerHTML = sensorName+": no data";
		sensorResultTitle.style.color = "grey";
		sensorResult.removeChild(image);
		decTask();
	}
	var bounds = "&width="+width+"&height="+height;
	image.src = url_query_diagram+"?plot="+plotName+"&sensor="+sensorName+"&aggregation="+aggregationName+"&quality="+qualityName+"&interpolated="+interpolatedName+timeParameter+"&boxplot="+boxplot+bounds;
	decTask();	
}

function addHeatmap(plotName, sensorName, sensorDesc, sensorUnit) {
	incTask();
	var sensorResult = getID("div_result").appendChild(document.createElement("div"));
	var sensorResultTitle = sensorResult.appendChild(document.createElement("div"));
	var sensorResultScale = sensorResult.appendChild(document.createElement("div"));
	sensorResultTitle.innerHTML += "query "+sensorName+"...";
	var aggregationName = aggregation_select.val();
	var qualityName = "step";
	var qualityName = quality_name[quality_select.val()];
	var interpolatedName = interpolation_checkbox[0].checked;
	var timeParameter = "";
	if(time_year_select.val()!=0) {
		timeParameter = "&year="+time_year_text[time_year_select.val()];
		var month = time_month_select.val();
		if(month!=0) {
			timeParameter += "&month="+month;
		}
	}
	var withScale = scale_checkbox[0].checked
	var magnificationFactor = magnification_select.val();
	incTask();
	var image = new Image();
	sensorResult.appendChild(image);
	image.onload = function() {
		image.width = image.naturalWidth*magnificationFactor;
		image.height = image.naturalHeight*magnificationFactor;
		sensorResultTitle.innerHTML = sensorName+": "+sensorDesc+" - "+sensorUnit;
		if(withScale) {
			incTask();
			var imageScale = new Image();
			sensorResultScale.appendChild(imageScale);
			imageScale.onload = function() {
				decTask();
			}
			imageScale.onerror = function() {	
				sensorResultScale.innerHTML = "no scale";
				sensorResultScale.style.color = "grey";
				sensorResultScale.removeChild(imageScale);
				decTask();
			}
			imageScale.src = url_heatmap_scale+"?sensor="+sensorName;
		}
		decTask();
	}
	image.onerror = function() {	
		sensorResultTitle.innerHTML = sensorName+": no data";
		sensorResultTitle.style.color = "grey";
		sensorResult.removeChild(image);
		decTask();
	}
	image.src = url_query_heatmap+"?plot="+plotName+"&sensor="+sensorName+"&aggregation="+aggregationName+"&quality="+qualityName+"&interpolated="+interpolatedName+timeParameter;
	decTask();	
}

function createTag(tag) {
	return document.createElement(tag);
}

function createText(text) {
	return document.createTextNode(text);
}

function addTag(root,tag) {
	return root.appendChild(document.createElement(tag));
}

function addText(root,text) {
	return root.appendChild(document.createTextNode(text));
}

function addTagText(root,tag,text) {
	//root.appendChild(document.createElement(tag)).appendChild(document.createTextNode(text));
	root.appendChild(document.createElement(tag)).innerHTML = text;
}

function getQueryCSV(plotName, sensors) {
	var query = "plot="+plotName;	
	for (var i = 0; i < sensors.length; i++) {
		query += "&sensor="+sensors[i][0];
	}
	query += "&aggregation="+aggregation_select.val();
	query += "&quality="+quality_name[quality_select.val()];
	query += "&interpolated="+interpolation_checkbox[0].checked;
	if(time_year_select.val()!=0) {
		query += "&year="+time_year_text[time_year_select.val()];
		var month = time_month_select.val();
		if(month!=0) {
			query += "&month="+month;
		}
	}
	return query;
}

function getSensorTable(sensors)  {
	var info = "<table><tr><th>Sensor</th><th>Description</th><th>Unit</th></tr>";
		for (var i = 0; i < sensors.length; i++) {
			info += "<tr><td>"+sensors[i][0]+"</td><td>"+sensors[i][1]+"</td><td>"+sensors[i][2]+"</td></tr>";
		}
		info += "</table><br>";
		return info;
}

function addTable(plotName, sensors) {
	incTask();
	getID("div_result").innerHTML = "query...";
	$.get(url_query_csv+"?"+getQueryCSV(plotName, sensors)).done(function(data) {
		getID("div_result").innerHTML = getSensorTable(sensors);
		var rows = splitCsv(data);
		console.log("now");
		var table = addTag(getID("div_result"),"table");
		$.each(rows, function(i,row) {
			if(i==0) {
				var tableRow = addTag(table,"tr");
				$.each(row, function(i,col) {addTagText(tableRow,"th",col);});
			} else {
				var tableRow = addTag(table,"tr");
				$.each(row, function(i,col) {addTagText(tableRow,"td",col);});
			}
		});
		decTask();
	}).fail(function() {getID("div_result").innerHTML = "no data";decTask();});	
}

function addCSVFile(plotName, sensors) {
	incTask();
	getID("div_result").innerHTML = "";
	
	var div_download = addTag(getID("div_result"),"div");
	div_download.style.textAlign = "center";
	addTagText(div_download,"p","<strong>Plot</strong> "+plotName);
	
	var s = "";
	for (var i = 0; i < sensors.length; i++) {
		s += " "+sensors[i][0];
	}
	
	addTagText(div_download,"p","<strong>Sensors</strong>"+s);
	addTagText(div_download,"p","<strong>Aggregation</strong> "+aggregation_select.val());
	addTagText(div_download,"p","<strong>Quality</strong> "+quality_name[quality_select.val()]);
	addTagText(div_download,"p","<strong>Interpolation</strong> "+interpolation_checkbox[0].checked);
	time = time_year_select.val();
	if(time == 0) {
		time = "all";
	} else {
		time = time_year_text[time];
		var month = time_month_select.val();
		if(month > 0) {
			time += "-";
			if(month < 10) {
				time += 0;
			}
			time += month;
		}
	}
	addTagText(div_download,"p","<strong>Time</strong> "+time);
	var anchor = addTag(div_download,"a");
	var e = addTagText(anchor,"h3","download (click to save)");
	anchor.href = url_query_csv+"?"+getQueryCSV(plotName, sensors);
	anchor.download = "plot_"+plotName+".csv";
	getID("div_result").innerHTML += getSensorTable(sensors);
	decTask();	
	
	/*getID("div_result").innerHTML = "query...";	
	$.get(url_query_csv+"?"+getQueryCSV(plotName, sensors)).done(function(data) {
		getID("div_result").innerHTML = getSensorTable(sensors);
		var anchor = addTag(getID("div_result"),"a");
		anchor.href = url_query_csv+"?"+getQueryCSV(plotName, sensors);
		anchor.download = "plot_"+plotName+".csv";
		anchor.textContent = "download (click to save)";
		addTag(getID("div_result"),"br");		
		addTag(getID("div_result"),"br");
		var table = addTag(getID("div_result"),"textarea");
		table.readOnly = true;
		table.style.width = "1200px";
		table.style.height = "600px";
		addText(table,data);
		decTask();
	}).fail(function() {getID("div_result").innerHTML = "no data";decTask();});*/	
}

function updateTimeYears() {
	$.each(time_year_text, function(i,text) {time_year_select.append(new Option(text,i));});
	$.each(time_month_text, function(i,text) {time_month_select.append(new Option(text,i));});
	onTimeYearChange();
}

function onTimeYearChange() {
	if(time_year_select.val()==0) {
		time_month_select.hide();
	} else {
		time_month_select.show();
	}
}

function updateAggregations() {
	var prev_agg = aggregation_select.val();
	aggregation_select.empty();
	var agg_text = [];
	var sensorIndex = sensor_select.val();
	if(sensorIndex == undefined){ // no sensor selected
		agg_text = [];
	} else if (sensorIndex == -1){ // sensor [all] selected
		agg_text = aggregation_text;
	} else {
		var row = sensor_rows[sensorIndex];
		if(row[3]=="NONE") {
			agg_text = aggregation_raw_text;
		} else {
			agg_text = aggregation_text;
		}
	}	
	
	var selectIndex = -1;
	$.each(agg_text, function(i,text) {
		aggregation_select.append(new Option(text));
		if(prev_agg==undefined && text=="hour") {
			selectIndex = i;
			prev_agg = "hour";
		} else if(text==prev_agg) {
			selectIndex = i;
		}
	});
	if(selectIndex>=0) {
		aggregation_select.val(prev_agg);
	}
	
	onAggregationChange();
}

function onAggregationChange() {
	if(aggregation_select.val()=="raw") {
		$("#div_quality_select").hide();
		$("#div_interpolation").hide();
	} else {
		$("#div_quality_select").show();
		$("#div_interpolation").show();
	}
	updateTypes();
}

function updateQualities() {
	$.each(quality_text, function(i,text) {quality_select.append(new Option(text,i));});
	quality_select.val(2);
}

function updateMagnification() {
	$.each(magnification_factors, function(i,factor) {magnification_select.append(new Option("x"+factor,factor));});
}

function updateTypes() {
	var prev_type = undefined;
	var prev_index = type_select.val();
	if(prev_index != undefined) {
		prev_type = type_select[0].options[prev_index].text;
	}
	type_select.empty();
	var t_text = [];
	if(aggregation_select.val()=="raw") {
		t_text = type_raw_text;
	} else if(aggregation_select.val()=="hour") {
		t_text = type_hour_text;
	} else if(aggregation_select.val()=="day"||aggregation_select.val()=="week"||aggregation_select.val()=="month"||aggregation_select.val()=="year") {
		t_text = type_high_aggregated_text;
	} else {
		t_text = type_general_text;
	}
	var select_index = -1;
	$.each(t_text, function(i,text) {
		type_select.append(new Option(text,i));
		if(text==prev_type) {
			select_index = i;
		}
	});
	if(select_index>=0) {
		type_select.val(select_index);
	}
	onTypeChange();
}

function onTypeChange() {
	var type = undefined;
	var type_index = type_select.val();
	if(type_index != undefined) {
		type = type_select[0].options[type_index].text;
	}
	
	if(type=="heatmap") {
		$("#div_scale").show();
		$("#div_magnification_select").show();
	} else {
		$("#div_scale").hide();
		$("#div_magnification_select").hide();
	}
}