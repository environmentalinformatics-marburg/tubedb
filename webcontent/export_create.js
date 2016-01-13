var url_base = "../";

var url_export_settings = url_base + "export/settings";
var url_export_create = url_base + "export/create";
var url_export_create_get_output = url_base + "export/create_get_output";
var url_download = url_base + "download";


var id = -1;
var total_plots = -1;

function getID(id) {
	return document.getElementById(id);
}

function ready() {
getID("status").innerHTML = "start creating zip-file";
$.get(url_export_create)
	.done(function(data) {
		id = data.id;
		total_plots = data.plots;
		getID("progress").max = total_plots;
		getID("status").innerHTML = "with id "+id;
		window.setTimeout(function() {get_output();}, 100);
	}).fail(function() {
		getID("status").innerHTML = "error";
	});
}

function get_output() {
$.get(url_export_create_get_output+"?id="+id)
	.done(function(data) {
		var processed_plots = data.processed_plots;
		getID("progress").value = processed_plots;
		getID("progressLabel").innerHTML = "processed "+processed_plots + " of " + total_plots + " plots";
		var output_lines = data.output_lines;
		if(output_lines.length>0) {
			var out = "";
			for(i in output_lines) {
				out += output_lines[i]+"\n";
			}
			append_output(out);
		}		
		if(!data.finished) {
			window.setTimeout(function() {get_output();}, 500);
		} else {
			getID("status").innerHTML = "ready";
			handle_download(data);			
		}
	}).fail(function() {
		append_output("\nerror");
		getID("status").innerHTML = "ready";		
	});
}

function append_output(text) {
	$("#output").val($("#output").val()+text);
	getID("output").scrollTop = getID("output").scrollTopMax;
}

function handle_download(data) {
	getID("download").innerHTML = "plots processed ";
	var a = document.createElement('a');
	var dl = document.createElement('h2');
	dl.appendChild(document.createTextNode("click to download"));	
	a.appendChild(dl);
	a.title = "link to resulting zip file";
	a.href = url_download+"/"+data.filename;
	var pref = data.filename;
	if(data.title !== undefined) {
		pref = data.title+data.id+".zip";
	}
	a.download = pref;
	getID("download").appendChild(a);
}

$(document).ready(ready);