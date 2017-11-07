"use strict";
document.addEventListener('DOMContentLoaded', function() {init();}, false);

var url_api_base = "../../";
var url_plot_info = url_api_base + "tsdb/plot_info";

var comparator = function (a, b) {
    return a.name.toLowerCase().localeCompare(b.name.toLowerCase());
};

function init() {
	
Vue.component('plot-dialog', {
	template: '#plot-dialog-template',
	replace: true,
	props: {
		plot: Object,
	},
});
	
Vue.component('plots-dialog', {
	template: '#plots-dialog-template',
	replace: true,
	props: {
		plots: Array,
	},
});
	
	
var app = new Vue({
	
el: '#app',

data: {
	message: "init ...",	
	clusterLayer: undefined,
	features: [],
	hoveredPlots: [],
	showPlotsDialog: false,
	selectedPlots: [],
	showPlotDialog: false,
	selectedPlot: undefined,
	featureSource: undefined,
},

mounted: function () {
	var self = this;
	self.message = "init map ...";
	self.createMap();
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
				plot_info: entry,
			}));
		}	
		self.features = features;		
		self.message = undefined;
	})
	.catch(function(e) {
		console.log(e);
	});

}, // end of mounted

watch: {
	features: function(features) {
		this.featureSource = new ol.source.Vector({
			features: features
		});
		var clusterSource = new ol.source.Cluster({
			distance: 30,
			source: this.featureSource
		});		
		this.clusterLayer.setSource(clusterSource);
	}
},

methods: {

	createStyleFunction: function(isSelected) {
		var styleCache = {};
		return function(clusterFeature, resolution) {
				var scale = 10/resolution;
				if(scale<1) {
					scale = 1;
				}
				console.log(scale);
				var subFeatures = clusterFeature.get('features');
				var size = subFeatures.length;
				if(size==1) {
					var feature = subFeatures[0];
					var label = feature.get('plot_info').name;
					//console.log(label);
					var style = new ol.style.Style({
						image: new ol.style.Circle({
							radius: (isSelected?15:10)*scale,
							stroke: new ol.style.Stroke({
								color: '#fff'
							}),
							fill: new ol.style.Fill({
								color: isSelected?'#AAEEFFAA':'#88CCFFAA',
							})
						}),
						text: new ol.style.Text({
							text: label,
							fill: new ol.style.Fill({
								color: '#000'
							}),
							font: isSelected?'14px sans-serif':'12px sans-serif',
						})
					});
					return style;
				} else {
					var style = new ol.style.Style({
						image: new ol.style.Circle({
							radius: (isSelected?20:15)*scale,
							stroke: new ol.style.Stroke({
								color: '#fff'
							}),
							fill: new ol.style.Fill({
								color: isSelected?'#3399FFAA':'#3399CCAA',
							})
						}),
						text: new ol.style.Text({
							text: size.toString(),
							fill: new ol.style.Fill({
								color: '#fff'
							}),
							font: isSelected?'14px sans-serif':'12px sans-serif',
						})
					});							
					return style;
				}
			}
	},
	
	createMap: function() {
		var self = this;
		
		this.clusterLayer = new ol.layer.Vector({
			/*source: clusterSource,*/
			style: this.createStyleFunction(false),
		});

		var raster = new ol.layer.Tile({
			source: new ol.source.OSM()
		});

		var map = new ol.Map({
			layers: [raster, this.clusterLayer],
			target: 'map',
			view: new ol.View({
				center: [0, 0],
				zoom: 2
			}),
			controls: ol.control.defaults({attributionOptions:{collapsible:false}}).extend([
				new ol.control.ScaleLine(),
				/*new ol.control.FullScreen(),*/
				new ol.control.MousePosition({coordinateFormat: function(pos) {var coord = ol.proj.toLonLat(pos);return coord[0].toFixed(4)+" "+coord[1].toFixed(4);}, className: "ol-mouse-position ol-control" }),
				new ol.control.ZoomSlider(),
			]),
			logo: false,
		});
		
		var selectHover = new ol.interaction.Select({
			condition: ol.events.condition.pointerMove,
			style: this.createStyleFunction(true),
		});
		selectHover.on('select', function(e) {
			var hoveredPlots = [];
			var hoveredClusterFeatures = selectHover.getFeatures();
			hoveredClusterFeatures.forEach(function(clusterFeature) {
				var features = clusterFeature.get('features');
				features.forEach(function(feature) {
					var plot_info = feature.get('plot_info');
					hoveredPlots.push(plot_info);
				});
			});
			var maxPlots = 15;
			hoveredPlots.sort(comparator);
			if(hoveredPlots.length<=maxPlots) {
				self.hoveredPlots = hoveredPlots;
			} else {
				self.hoveredPlots = hoveredPlots.slice(0,maxPlots-1);
				self.hoveredPlots.push('...');
			}
		});
		map.addInteraction(selectHover);
		
		var selectSingleClick = new ol.interaction.Select({
			style: this.createStyleFunction(true),
		});
		selectSingleClick.on('select', function(e) {
			self.upateSelectedPlots(selectSingleClick.getFeatures());
			selectSingleClick.getFeatures().clear();
			self.viewSelectedPlots();
		});
		map.addInteraction(selectSingleClick);

		var dragBox = new ol.interaction.DragBox({
			condition: ol.events.condition.platformModifierKeyOnly
		});
		map.addInteraction(dragBox);
		dragBox.on('boxstart', function() {
			self.selectedPlots = [];			
		});
		dragBox.on('boxend', function() {
			var selectedPlots = [];
			var extent = dragBox.getGeometry().getExtent();
			self.featureSource.forEachFeatureIntersectingExtent(extent, function(feature) {
				var plot_info = feature.get('plot_info');				
				selectedPlots.push(plot_info);
			});
			selectedPlots.sort(comparator);
			self.selectedPlots = selectedPlots;
			self.viewSelectedPlots();
		});			
	},
	
	upateSelectedPlots(selectedClusterFeatures) {
		var self = this;
		var selectedPlots = [];
		selectedClusterFeatures.forEach(function(clusterFeature) {
			var features = clusterFeature.get('features');
			features.forEach(function(feature) {
				var plot_info = feature.get('plot_info');				
				selectedPlots.push(plot_info);
			});
		});
		selectedPlots.sort(comparator);
		self.selectedPlots = selectedPlots;
	},
	
	viewSelectedPlots() {
		var self = this;
		if(self.selectedPlots.length == 0) {
			//nothing
		} else if(self.selectedPlots.length == 1) {
			self.viewPlot(self.selectedPlots[0]);
		} else {
			self.showPlotsDialog = true;
		}		
	},
	
	viewPlot(plot) {
		this.selectedPlot = plot;
		this.showPlotDialog = true;
		console.log(plot);
	},
}, // end of methods

}); // end of app




} // end of init

