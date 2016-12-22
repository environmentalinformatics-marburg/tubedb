"use strict";

Vue.component('translation-dialog', {
	template: '#translation-dialog-template',
	replace: true,
	props: {
		row: Array,
		translations: Array,
	},
})



Vue.component('catalog-table', {
	
template: '#catalog-table-template',

replace: true,

props: {
	message: String,
	header: Array,
	data: Array,
	filterKey: String,
	filterYear: String,
},

data: function () {
	var columnNames = ['station', 'first', 'last', 'rows', 'timestep', 'filename', 'path', 'translation'];
	var sortOrders = {};
	columnNames.forEach(function (key) {
	  sortOrders[key] = 1;
    });
    return {
      	columnIndices: [0, 1, 2, 3, 4, 6, 7, 5],
		columnNames: columnNames,
		sortOrders: sortOrders,
		sortKey: 'first',
	    selectedRow: undefined,
		showTranslationDialog: false,
		translationDialogData: [],
		translationDialogStation: undefined,
    }
},

computed: {
	filteredData: function () {
		var self = this;
		var data = this.data;

		var filterKey = this.filterKey && this.filterKey.toLowerCase();
		if(this.filterYear!='*') {
			var year = parseInt(this.filterYear);
			data = data.filter(function (row) {
				return self.getFirstYear(row)<=year && year<=self.getLastYear(row);
			});
		}
		if (filterKey) {			
			data = data.filter(function (row) {
				return Object.keys(row).some(function (key) {
					return String(row[key]).toLowerCase().indexOf(filterKey) > -1
				});
			});
		}
		var sortKey = this.sortKey;
		var sortIndex = -1;
		for(var i in this.columnNames) {
			if(this.header[i]===sortKey) {
				sortIndex = i;
			}
		}
		var order = this.sortOrders[sortKey] || 1;
		switch(sortKey) {
		case 'rows':
		case 'timestep':
			data = data.slice().sort(function (a, b) {
				  a = parseInt(a[sortIndex]);
				  b = parseInt(b[sortIndex]);
				  if(a==undefined) {
					  if(b==undefined) {
						return 0;
					  } else {
						return 1;
					  }
				  } else if(b==undefined) {
					  return -1;
				  }
				  return (a === b ? 0 : a > b ? 1 : -1) * order;
			});
			break;
		default:
			data = data.slice().sort(function (a, b) {
				  a = a[sortIndex];
				  b = b[sortIndex];
				  if(a==undefined) {
					  if(b==undefined) {
						return 0;
					  } else {
						return 1;
					  }
				  } else if(b==undefined) {
					  return -1;
				  }
				  return (a === b ? 0 : a > b ? 1 : -1) * order;
			});
		}
		return data;
	},
},

methods: {
    sortBy: function(key) {
      if(this.sortKey == key) {	  
		this.sortOrders[key] = this.sortOrders[key] * -1;
	  }
	  this.sortKey = key;
    },
	getFirstYear: function(entry) {
		return parseInt(entry[1].substring(0,4));
	},
	getLastYear: function(entry) {
		return parseInt(entry[2].substring(0,4));
	},
	viewRow: function(row) {
		this.selectedRow = row;
		this.getTranslation(row);
		//alert(row[5].slice(1, -1).replace(/->/g,'\t->\t').replace(/ /g,'\n'));
		/*$( "#translation_dialog" ).dialog({
			resizable: false,
			height: 480,
			width: 640,
			modal: true,
			title: this.selectedRow[0],
		});*/
		this.translationDialogRow = row; 
		this.translationDialogData = this.getTranslation(row); 
		this.showTranslationDialog = true;		
	},
	getTranslation: function(row) {
		var trans = [];
		row[5].slice(1, -1).split(' ').forEach(function (entry) {
			if(entry.indexOf('->')>=0) {
				trans.push(entry.split('->'));
			} else {
				if(entry.substring(0,1)=='<') {
					trans.push([entry.slice(1,-1), '']);
				} else {
					trans.push([entry, entry]);
				}
			}
			
		});
		//console.log(trans);
		return trans;
	}
},

}); //end catalog-table component