var url_base = "../";

var url_plot_info = url_base + "tsdb/plot_info";
var url_plot_status = url_base + "tsdb/status";

var BE_prop = {pos:new google.maps.LatLng(50.471885, 10.8121638), zoom:7, region:"BE"};
var KI_prop = {pos:new google.maps.LatLng(-3.1738427, 37.3574291),zoom:11, region:"KI"};
var SA_prop = {pos:new google.maps.LatLng(-28.6948631, 24.4571832),zoom:6, region:"SA"};
var SA_OWN_prop = {pos:new google.maps.LatLng(-28.1873589, 26.4343353),zoom:7, region:"SA_OWN"};
var pops = [BE_prop,KI_prop,SA_prop,SA_OWN_prop];
	
var map;

var plotInfoMap = {};

var plots_select;

var tasks = 0;

function incTask() {
	//ready_to_run(false);
	tasks++;	
	document.getElementById("status").innerHTML = "busy ("+tasks+")...";
	document.getElementById("busy_indicator").style.display = 'inline';
}

function decTask() {
	tasks--;
	if(tasks===0) {
		document.getElementById("status").innerHTML = "ready";
		document.getElementById("busy_indicator").style.display = 'none';
		//ready_to_run(true);
	} else if(tasks<0){
		document.getElementById("status").innerHTML = "error";
		document.getElementById("busy_indicator").style.display = 'none';
	} else {
		document.getElementById("status").innerHTML = "busy ("+tasks+")...";
		document.getElementById("busy_indicator").style.display = 'inline';
	}
}

function document_ready() {
	incTask();
	plots_select = $("#plots_select");	
	$.each(pops, function(i,prop) {plots_select.append(new Option(prop.region,i));});
	plots_select.val(1);
	plots_select[0].onchange = onPlotsChange;
	onPlotsChange();	
	decTask();
} // END document_ready

function onPlotsChange() {
	incTask();
	var prop = pops[plots_select.val()];
	var mapOptions = { 	zoom: prop.zoom, 
						center: prop.pos,
						mapTypeId: google.maps.MapTypeId.TERRAIN,
						streetViewControl: false,
						panControl: false
					};
	document.getElementById('map-canvas').innerHTML = "";
	map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);	
	queryPlotInfo(prop);
	decTask();	
}	

function queryPlotInfo(prop) {
	incTask();
	$.get(url_plot_info+"?region="+prop.region).done(function(plots) {
		for(var i in plots) {
			var plot = plots[i];
			
			plotInfoMap[plot.name] = plots[i];
			
			//addPlotMarker(plot);
			
		}
		//console.log(plotInfoMap);
		queryPlotStatus(prop);
		decTask();
	}).fail(function() {sensor_select.append(new Option("[error]","[error]"));decTask();});		
}

function queryPlotStatus(prop) {
	incTask();
	$.getJSON(url_plot_status+"?region="+prop.region).done(function(intervals) {
		var max_last = 0;
		for(var i in intervals) {
			var interval = intervals[i];
			var plotID = interval.plot;
			var plotInfo = plotInfoMap[plotID]; 
			//console.log(plotInfo);
			for (var attrname in plotInfo) {
				interval[attrname] = plotInfo[attrname];
			}
			if(max_last<interval.last_timestamp) {
				max_last = interval.last_timestamp;
			}			
			//console.log(max_last);
			//addPlotMarker(interval);
		}
		for(var i in intervals) {
			var plot = intervals[i];
			var t = max_last - plot.last_timestamp;
			plot.days = t/(60*24);
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
			addPlotMarker(plot,timeMark);
		}
		//console.log(intervals);
		decTask();
	}).fail(function() {decTask();});
}

function addPlotMarker(plot, timeMark) {
	if(plot.lat!=undefined && plot.lon!=undefined) {			
		var myLatlng = new google.maps.LatLng(plot.lat, plot.lon);
		//console.log(plot.name+"  "+plot.lat,"  "+plot.lon);

		var timeMarkColor = '#FF0000';
		if(timeMark!="timeMarkNow") {
			timeMarkColor = '#00FF00'; 
		}
		switch(timeMark) {
			case "timeMarkNow":
				timeMarkColor = '#44ff44';
				break;
			case "timeMarkOneWeek":
				timeMarkColor = '#ffff44';
				break;
			case "timeMarkTwoWeeks":
				timeMarkColor = '#ff9944';
				break;
			case "timeMarkOneMonth":
				timeMarkColor = '#ff4444';
				break;
			case "timeMarkLost":
				timeMarkColor = '#666666';
				break;
			default: //error
				console.log("error unknown marker text: "+timeMark);
				timeMarkColor = '#0000FF';
		}
		
		
		var populationOptions = {
			strokeColor: timeMarkColor,
			strokeOpacity: 1.0,
			strokeWeight: 2,
			fillColor: timeMarkColor,
			fillOpacity: 0.8,
			map: map,
			center: myLatlng,
			radius: 100,
			title: plot.name
		};			
		var circle = new google.maps.Circle(populationOptions);
		
		var marker = new MarkerWithLabel({
		   position: myLatlng,
		   map: map,
		   labelContent: plot.name+"<br>"+parseInt(plot.days),
		   labelClass: "plotlabels", // the CSS class for the label
		   //labelInBackground: false,
		   icon: 'plot_icon.png'
		 });
		 
		/*var contentString = "<h1>"+plot.name+"</h1>";
		var infowindow = new google.maps.InfoWindow({content: contentString});
		google.maps.event.addListener(marker, 'click', function() {infowindow.open(map,marker);});*/
	}
}

