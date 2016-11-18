"use strict";
document.addEventListener('DOMContentLoaded', function() {init();}, false);

var url_api_base = "../../";
var url_plot_info = url_api_base + "tsdb/plot_info";

function init() {
	
var app = new Vue({
	
el: '#app',

data: {
	message: "init ...",
},

created: function () {
	var self = this;
	self.message = "query plots ...";
	axios.get(url_plot_info)
	.then(function(r) {
		var json = r.data;
		self.message = "create map ...";
		var features = [];
		for(var i in json) {
			var entry = json[i];
			if(!entry.hasOwnProperty("lon")) {
				continue;
			}
			//console.log(entry);
			var coordinates = ol.proj.fromLonLat([entry.lon,entry.lat]);
			//console.log(coordinates);
			features.push(new ol.Feature({
				geometry: new ol.geom.Point(coordinates),
				name: entry.name,
			}));
		}
		
		//console.log("OK");
		
		var source = new ol.source.Vector({
			features: features
		});

		var clusterSource = new ol.source.Cluster({
			distance: 30,
			source: source
		});

		var styleCache = {};
		var clusters = new ol.layer.Vector({
			source: clusterSource,
			style: function(clusterFeature) {
				var subFeatures = clusterFeature.get('features');
				var size = subFeatures.length;
				if(size==1) {
					var feature = subFeatures[0];
					var label = feature.get('name');
					//console.log(label);
					var style = new ol.style.Style({
						image: new ol.style.Circle({
							radius: 10,
							stroke: new ol.style.Stroke({
								color: '#fff'
							}),
							fill: new ol.style.Fill({
								color: '#88CCFF'
							})
						}),
						text: new ol.style.Text({
							text: label,
							fill: new ol.style.Fill({
								color: '#000'
							}),
							font: '12px sans-serif',
						})
					});
					return style;
				} else {
					var style = styleCache[size];
					if (!style) {
						style = new ol.style.Style({
							image: new ol.style.Circle({
								radius: 15,
								stroke: new ol.style.Stroke({
									color: '#fff'
								}),
								fill: new ol.style.Fill({
									color: '#3399CC'
								})
							}),
							text: new ol.style.Text({
								text: size.toString(),
								fill: new ol.style.Fill({
									color: '#fff'
								})
							})
						});
						styleCache[size] = style;
					}		
					return style;
				}
			}
		});

		var raster = new ol.layer.Tile({
			source: new ol.source.OSM()
		});

		var map = new ol.Map({
			layers: [raster, clusters/*new ol.layer.Vector({source})*/],
			target: 'map',
			view: new ol.View({
				center: [0, 0],
				zoom: 2
			})
		});
		
		self.message = undefined;
	})
	.catch(function(e) {
		console.log(e);
	});

}, // end of created
}); // end of app




} // end of init


