"use strict";
document.addEventListener('DOMContentLoaded', function() {init();}, false);

var url_api_base = "../../";
var url_plot_info = url_api_base + "tsdb/plot_info";

var comparator = function (a, b) {
    return a.name.toLowerCase().localeCompare(b.name.toLowerCase());
};

var layersCollector = function(layers, collector) {
	if(layers === undefined) {
		return;
	}
	layers.forEach(function(layer) {
		if(layer.Name !== undefined) {
			collector.push(layer);
		}
		layersCollector(layer.Layer, collector);
	})
}

String.prototype.replaceAll = function(strReplace, strWith) {
    // See http://stackoverflow.com/a/3561711/556609
    var esc = strReplace.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
    var reg = new RegExp(esc, 'ig');
    return this.replace(reg, strWith);
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
	map: undefined,	
	clusterLayer: undefined,
	features: [],
	hoveredPlots: [],
	showPlotsDialog: false,
	selectedPlots: [],
	showPlotDialog: false,
	selectedPlot: undefined,
	featureSource: undefined,
	visibleHelp: undefined,
	control_panel_show_content: false,
	backgroundMaps: [{id: "OSM", title: "OpenStreetMap"},
					 {id: "OTM", title: "OpenTopoMap"},
					 {id: "StamenTerrain", title: "Stamen Terrain"},
					 {id: "StamenToner", title: "Stamen Toner"},
					 {id: "osm-wms", title: "WORLD OSM WMS"},
					 {id: "terrestris-wms", title: "terrestris OSM-WMS"},
					 {id: "WMS-TH-DOP", title: "Thüringen WMS für Digitale Orthophotos"},
					 {id: "landsat-wmts", title: "NASA's Global Imagery Browse Services (GIBS)"},
					 {id: "CustomWMS", title: "Custom WMS"},
					 {id: "CustomWMTS", title: "Custom WMTS"},
					 {id: "CustomXYZ", title: "Custom XYZ"},
					],
	backgroundMap: undefined,
	backgroundMapPropMap: { "OSM": {type: 'XYZ', url: 'https://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png', attributions: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors.'},
							"OTM": {type: 'XYZ', url: 'https://{a-c}.tile.opentopomap.org/{z}/{x}/{y}.png', attributions: 'Kartendaten: © <a href="https://openstreetmap.org/copyright">OpenStreetMap</a>-Mitwirkende, SRTM | Kartendarstellung: © <a href="http://opentopomap.org">OpenTopoMap</a> (<a href="https://creativecommons.org/licenses/by-sa/3.0/">CC-BY-SA</a>)'},
							"StamenTerrain": {type: 'XYZ', url: 'http://tile.stamen.com/terrain/{z}/{x}/{y}.jpg', attributions: 'Map tiles by <a href="http://stamen.com">Stamen Design</a>, under <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a>. Data by <a href="http://openstreetmap.org">OpenStreetMap</a>, under <a href="http://www.openstreetmap.org/copyright">ODbL</a>.'},
							"StamenToner": {type: 'XYZ', url: 'http://tile.stamen.com/toner/{z}/{x}/{y}.png', attributions: 'Map tiles by <a href="http://stamen.com">Stamen Design</a>, under <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a>. Data by <a href="http://openstreetmap.org">OpenStreetMap</a>, under <a href="http://www.openstreetmap.org/copyright">ODbL</a>.'},
							"osm-wms": {type: 'WMS', url: 'http://maps.heigit.org/osm-wms/service?REQUEST=GetCapabilities&SERVICE=WMS', attributions: 'Custom WMS source'},
							"terrestris-wms": {type: 'WMS', url: 'https://ows.terrestris.de/osm/service?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities', attributions: 'Custom WMS source'},
							"WMS-TH-DOP": {type: 'WMS', url: 'http://www.geoproxy.geoportal-th.de/geoproxy/services/DOP?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1', attributions: 'Custom WMS source'},
							"landsat-wmts": {type: 'WMTS', url: 'https://gibs.earthdata.nasa.gov/wmts/epsg3857/best/1.0.0/WMTSCapabilities.xml', attributions: 'NASA Global Imagery Browse Services for EOSDIS'},
							"CustomWMS": {type: 'WMS', url: '', attributions: 'Custom WMS source'},							
							"CustomWMTS": {type: 'WMTS', url: '', attributions: 'Custom WMTS source'},	
							"CustomXYZ": {type: 'XYZ', url: '', attributions: 'Custom XYZ source'},
						},
	customWMSUrl: '',
	customWMTSUrl: '',
	customXYZUrl: '',
	WMSMessage: undefined,
	WMSCapabilities: undefined,
	WMSLayer: undefined,
	WMTSMessage: undefined,
	WMTSCapabilities: undefined,
	WMTSLayer: undefined,
},

mounted: function () {
	var self = this;
	self.message = "init map ...";
	this.backgroundMap = 'OTM';
	self.createMap();
	this.refreshBackgroundMap();
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
			features: features,
			wrapX: false,
		});
		var clusterSource = new ol.source.Cluster({
			distance: 30,
			source: this.featureSource,
			wrapX: false,
		});		
		this.clusterLayer.setSource(clusterSource);
		this.map.getView().fit(this.featureSource.getExtent(), this.map.getSize());
	},
	backgroundMap: function() {
		this.refreshBackgroundMap();
	},
	customXYZUrl: function() {
		this.refreshBackgroundMap();
	},
	customWMSUrl: function() {
		this.refreshBackgroundMap();
	},
	WMSLayer: function() {
		this.refreshLayers();
	},
	customWMTSUrl: function() {
		this.refreshBackgroundMap();
	},
	WMTSLayer: function() {
		this.refreshLayers();
	},
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
	refreshBackgroundMap: function() {
		var backgroundMapId = this.backgroundMap === undefined ? this.backgroundMaps[0].id : this.backgroundMap;
		var backgroundMapProps = this.backgroundMapPropMap[backgroundMapId];

		if(backgroundMapProps.type === 'XYZ') {
			this.refreshLayers();
		}
		if(backgroundMapProps.type === 'WMS') {
			var url = backgroundMapProps.url;
			if(backgroundMapId === 'CustomWMS') {
				url = this.customWMSUrl;
			}
			this.WMSMessage = "loading WMS metadata";
			this.WMSCapabilities = undefined;
			this.refreshLayers();
			axios.get(url)
			.then(r => {
				this.WMSMessage = "parsing WMS metadata";
				var parser = new ol.format.WMSCapabilities();
				console.log(parser);
				var cap = parser.read(r.data);
				console.log(JSON.parse(JSON.stringify(cap)));
				this.WMSCapabilities = cap;
				this.WMSMessage = undefined;
				this.refreshLayers();
			})
			.catch(e => {
				this.WMSMessage = "ERROR loading WMS metadata";
				this.refreshLayers();
			});
		}
		if(backgroundMapProps.type === 'WMTS') {
			var url = backgroundMapProps.url;
			if(backgroundMapId === 'CustomWMTS') {
				url = this.customWMTSUrl;
			}
			this.WMTSMessage = "loading WMTS metadata";
			this.WMTSCapabilities = undefined;
			this.refreshLayers();
			axios.get(url)
			.then(r => {
				this.WMTSMessage = "parsing WMTS metadata";
				var parser = new ol.format.WMTSCapabilities();
				console.log(parser);
				var cap = parser.read(r.data);
				console.log(JSON.parse(JSON.stringify(cap)));
				this.WMTSCapabilities = cap;
				this.WMTSMessage = undefined;
				this.refreshLayers();
			})
			.catch(e => {
				this.WMTSMessage = "ERROR loading WMTS metadata";
				this.refreshLayers();
			});
		}
	},

	refreshLayers: function() {
		var backgroundLayer = undefined;

		var backgroundMapId = this.backgroundMap === undefined ? this.backgroundMaps[0].id : this.backgroundMap;
		var backgroundMapProps = this.backgroundMapPropMap[backgroundMapId];
		
		if(backgroundMapProps.type === 'XYZ') {
			var url = backgroundMapProps.url;
			if(backgroundMapId === 'CustomXYZ') {
				url = this.customXYZUrl;
			}
			var source = new ol.source.XYZ({
				url: url,
				attributions: backgroundMapProps.attributions,
				wrapX: false,
			});
			backgroundLayer = new ol.layer.Tile({
				source: source,
			});
		}

		if(backgroundMapProps.type === 'WMS' && this.WMSCapabilities != undefined) {
			var url = undefined;
			if(this.WMSCapabilities.Capability.Request !== undefined) {
				try {
					url = this.WMSCapabilities.Capability.Request.GetMap.DCPType[0].HTTP.Get.OnlineResource;
				} catch(e) {
					console.log(e);
				}
			}
			if(url === undefined && this.customWMSUrl !== undefined) {
				url = this.customWMSUrl.replaceAll('REQUEST=GetCapabilities', '');
			}
			var WMSOptions = {};
			WMSOptions.url = url;
			if(this.WMSLayer !== undefined) {
				WMSOptions.params = {'LAYERS': this.WMSLayer};
			}
			if(this.WMSCapabilities.Service !== undefined && this.WMSCapabilities.Service.AccessConstraints !== undefined) {
				WMSOptions.attributions = this.WMSCapabilities.Service.AccessConstraints;
			}
			var source = new ol.source.ImageWMS(WMSOptions/*{
				attributions: '© <a href="http://www.geo.admin.ch/internet/geoportal/' +
					'en/home.html">National parks / geo.admin.ch</a>',
				crossOrigin: 'anonymous',
				params: {'LAYERS': 'ch.bafu.schutzgebiete-paerke_nationaler_bedeutung'},
				serverType: 'mapserver',
				url: 'https://wms.geo.admin.ch/'
			  }*/);
			backgroundLayer = new ol.layer.Image({
				source:	source,
			});		  			
		}

		if(backgroundMapProps.type === 'WMTS' && this.WMTSCapabilities !== undefined && this.WMTSLayer !== undefined) {
			var options = ol.source.WMTS.optionsFromCapabilities(this.WMTSCapabilities, {layer: this.WMTSLayer});
			console.log(options);
			var source =  new ol.source.WMTS(options);
			backgroundLayer = new ol.layer.Tile({
				source:	source,
			})		  			
		}

		var layers = this.map.getLayers();
		layers.clear();
		if(backgroundLayer !== undefined) {
			layers.push(backgroundLayer);
			console.log(backgroundLayer);
		}
		layers.push(this.clusterLayer);
		this.map.changed();
	},
	
	createMap: function() {
		var self = this;
		
		this.clusterLayer = new ol.layer.Vector({
			/*source: clusterSource,*/
			style: this.createStyleFunction(false),
		});		

		self.map = new ol.Map({
			layers: [this.clusterLayer],
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
		self.map.addInteraction(selectHover);
		
		var selectSingleClick = new ol.interaction.Select({
			style: this.createStyleFunction(true),
		});
		selectSingleClick.on('select', function(e) {
			self.upateSelectedPlots(selectSingleClick.getFeatures());
			selectSingleClick.getFeatures().clear();
			self.viewSelectedPlots();
		});
		self.map.addInteraction(selectSingleClick);

		var dragBox = new ol.interaction.DragBox({
			condition: ol.events.condition.platformModifierKeyOnly
		});
		self.map.addInteraction(dragBox);
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

	toggleHelp(language) {
		if(this.visibleHelp === language) {
			this.visibleHelp = undefined;
		} else {
			this.visibleHelp = language;
		}
	},
}, // end of methods

computed: {
	WMSLayers() {
		if(this.WMSCapabilities === undefined || this.WMSCapabilities.Capability === undefined || this.WMSCapabilities.Capability.Layer === undefined || this.WMSCapabilities.Capability.Layer.Layer === undefined) {
			return [];
		}
		var collector = [];
		layersCollector(this.WMSCapabilities.Capability.Layer.Layer, collector);
		return collector;
	}
}, 

}); // end of app




} // end of init

