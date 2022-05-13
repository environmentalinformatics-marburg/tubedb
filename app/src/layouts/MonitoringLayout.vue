<template>
  <q-layout view="hHh LpR fFf">

    <q-header reveal elevated class="bg-grey-7 text-grey-4">
      <pages-toolbar title="TubeDB monitoring" active="/monitoring"/>
    </q-header>

    <q-page-container class="row">
      <q-page padding class="column">
        <q-toolbar class="shadow-2">
          <q-select 
            outlined 
            label="Select monitored set"
            :options="['Exploratories HEG']" 
            stack-label
            v-model="selectedSet"
            options-dense
            dense
            style="width: 250px"
          />          
          <q-select 
            outlined 
            label="Select monitored plots"
            :options="monitoring_meta.plots" 
            stack-label
            v-model="selectedPlots"
            multiple
            clearable
            options-dense
            dense
            style="width: 250px"
          />

          <q-select 
            outlined 
            label="Select monitored sensors"
            :options="monitoring_meta.sensors" 
            stack-label
            option-label="sensor"
            v-model="selectedSensors"
            multiple
            clearable
            options-dense
            dense
            style="width: 250px"
          />
        </q-toolbar>
        <q-btn @click="refresh" :loading="dataLoading" icon="refresh">refresh</q-btn>
        <q-table         
          dense
          :columns="columns"
          :rows="rows"
          row-key="plot"
          :rows-per-page-options="[0]" 
          :pagination="pagination"
          hide-pagination
          :sort-method="customSort"    
          binary-state-sort            
        >
              <template v-slot:body="props">
        <q-tr :props="props">
          <q-td key="plot" :props="props">
            <b>{{props.row.plot}}</b>
          </q-td>
          <!--<q-td key="UB.datetime" :props="props">
            {{props.row['UB.datetime']}}
          </q-td>
          <q-td key="UB" :props="props">
            {{props.row.UB.toFixed(2)}}
          </q-td>-->

          <q-td :props="props" v-for="sensorColumn in sensorColumns" :key="sensorColumn.sensor" :class="cellClass(sensorColumn, props.row)" :title="props.row.plot + ' ' + sensorColumn.sensor">
            <span v-if="sensorColumn.number">
              {{props.row[sensorColumn.sensor] === -99999 ? '' : props.row[sensorColumn.sensor].toFixed(2)}}
            </span>
            <i v-else>
              {{props.row[sensorColumn.sensorTimestamp] === 0 ? '' : props.row[sensorColumn.sensor]}}
            </i>
          </q-td>

          <!--<template v-for="sensor in monitoring_meta.sensors" :key="sensor">
            
            <q-td :props="props">
              {{sensor}}
              Ready
            </q-td>
            <q-td :props="props">
              {{props.row[sensor]}}
            </q-td>
          </template>-->
          
        </q-tr>
      </template>
        </q-table>
      <div v-if="data === undefined" style="color: red;">
        Click refresh button to load data!
      </div>
      </q-page>
    </q-page-container>

  </q-layout>
</template>

<script>

import { mapGetters } from 'vuex';

import pagesToolbar from 'components/pages-toolbar.vue';

export default {
  components: {
    pagesToolbar,
  },
  data () {
    return {
      data: undefined,
      dataLoading: false,
      pagination: {
        page: 1,    
        rowsPerPage: 0, 
      },
      monitoring_meta: {
        plots: [
          'HEG01', 
          'HEG02', 
          'HEG03', 
          'HEG04',
          'HEG05',
          'HEG06',
          'HEG07',
          'HEG08',
          'HEG09',
          'HEG10',
          'HEG11', 
          'HEG12', 
          'HEG13', 
          'HEG14',
          'HEG15',
          'HEG16',
          'HEG17',
          'HEG18',
          'HEG19',
          'HEG20',
          'HEG21', 
          'HEG22', 
          'HEG23', 
          'HEG24',
          'HEG25',
          'HEG26',
          'HEG27',
          'HEG28',
          'HEG29',
          'HEG30',
          'HEG31', 
          'HEG32', 
          'HEG33', 
          'HEG34',
          'HEG35',
          'HEG36',
          'HEG37',
          'HEG38',
          'HEG39',
          'HEG40',
          'HEG41', 
          'HEG42', 
          'HEG43', 
          'HEG44',
          'HEG45',
          'HEG46',
          'HEG47',
          'HEG48',
          'HEG49',
          'HEG50',
        ],
        sensors: [
          {sensor: 'UB', ok: [12.2, 14.9], warn: [11.9, 14.9]}, 
          {sensor: 'Ta_200', ok: [-20, 35], warn: [-40, 40]}, 
          {sensor: 'Ta_10', ok: [-20, 35], warn: [-40, 40]}, 
          {sensor: 'Ts_05', ok: [-20, 35], warn: [-40, 40]}, 
          {sensor: 'Ts_10', ok: [-20, 35], warn: [-40, 40]}, 
          {sensor: 'Ts_20', ok: [-20, 35], warn: [-40, 40]}, 
          {sensor: 'Ts_50', ok: [-20, 35], warn: [-40, 40]}, 
          {sensor: 'SM_10', ok: [1, 65], warn: [0, 70]}, 
          {sensor: 'SM_20', ok: [1, 65], warn: [0, 70]},
          {sensor: 'SM_30', ok: [1, 65], warn: [0, 70]},
          {sensor: 'rH_200', ok: [15, 100], warn: [0, 100]},
          {sensor: 'LWDR_300', ok: [0, 1000], warn: [0, 1500]},
          {sensor: 'LWUR_300', ok: [0, 1000], warn: [0, 1500]},
          {sensor: 'SWDR_300', ok: [0, 1000], warn: [0, 1500]},
          {sensor: 'SWUR_300', ok: [0, 1000], warn: [0, 1500]},
        ],
      },
      selectedSet: 'Exploratories HEG',
      selectedSensors: undefined,
      selectedPlots: undefined,
    }
  },
  computed: {
    ...mapGetters({
      apiGET: 'apiGET',
    }),
    columns() {
      let result = [{
        name: 'plot',
        field: 'plot',
        label: 'Plot',
        sortable: true,
      }];
      if(this.monitoring_meta !== undefined && this.data !== undefined) {
        this.data.sensors.forEach(sensorName => {
          const sensor = this.monitoring_meta.sensors.find(sensor => sensor.sensor === sensorName);
          //});
          //this.monitoring_meta.sensors.forEach(sensor => {
          console.log(sensor);
          const timename = sensor.sensor + '.datetime';
          result.push({
            name: timename,
            field: timename,
            label: 'datetime',
            sortable: true,
          });
          result.push({
            name: sensor.sensor,
            field: sensor.sensor,
            label: sensor.sensor,
            sortable: true,
          });
        });
      }
      return result;
    },
    columnMap() {
      let map = {};
      map.plot = {sortType: 'text'};
      if(this.monitoring_meta !== undefined) {
        this.monitoring_meta.sensors.forEach(sensor => {          
          const timename = sensor.sensor + '.datetime';
          map[sensor.sensor] = {sortType: 'number'};
          map[timename] = {sortType: 'text'};
        });
      }
      return map;
    },
    sensorColumns() {
      let result = [];
      if(this.monitoring_meta !== undefined) {
        this.monitoring_meta.sensors.forEach(sensor => {
          const timename = sensor.sensor + '.datetime';
          const timestampName = sensor.sensor + '.timestamp';
          result.push({sensor: timename, datetime: true, number: false, sensorTimestamp: timestampName});
          result.push({sensor: sensor.sensor, datetime: false, number: true, ok: sensor.ok, warn: sensor.warn, sensorTimestamp: timestampName});
        });
      }
      return result;
    }, 
    rows() {
      if(this.data === undefined) {
        return [];
      }
      const sensors = this.data.sensors;
      let result = this.data.measurements.map(e => {
        let row = {
          plot: e.plot,
        };
        const datetimes = e.datetime;
        const values = e.value;
        const timestamps = e.timestamp;
        for (let i = 0; i < sensors.length; i++) {
          const sensor = sensors[i];
          const timename = sensor + '.datetime';
          const timestampName = sensor + '.timestamp';
          row[timename] = datetimes[i];
          row[sensor] = values[i];
          row[timestampName] = timestamps[i];
        }     
        return row;
      });
      return result;
    },  
    timestampNow() {
      return this.data === undefined ? 0 : this.data.timestamp;
    },    
  },
  methods: {
    async refresh() {
      try {
        const params = new URLSearchParams();
        if(this.selectedPlots) {
          this.selectedPlots.forEach(plot => params.append('plot', plot));
        } else {       
          this.monitoring_meta.plots.forEach(plot => params.append('plot', plot));
        }
        if(this.selectedSensors) {
          this.selectedSensors.forEach(sensor => params.append('sensor', sensor.sensor));
        } else {
          this.monitoring_meta.sensors.forEach(sensor => params.append('sensor', sensor.sensor));
        }
        this.dataLoading = true;
        const response = await this.apiGET(['tsdb', 'monitoring'], {params});
        this.data = response.data;
      } catch(e) {
        this.data = undefined;
        console.log(e);
        this.$q.notify({message: 'Error loading data.', type: 'negative'});
      } finally {
        this.dataLoading = false;
      }
    },
    cellClass(sensorColumn, row) {
      const sensorValue = row[sensorColumn.sensor];
      const sensorTimestamp = row[sensorColumn.sensorTimestamp];
      const delta = this.timestampNow - sensorTimestamp;
      const deltaWarn = delta > 100000 || delta < -24 * 60;
      const deltaError = delta > 200000 || delta < -2 * 24 * 60;
      console.log(sensorColumn.sensorTimestamp);
      console.log(delta);      
      if(sensorColumn.number) {
        if(sensorValue === -99999) { // missing
          return '';
        } else if(sensorColumn.ok) {
          if(sensorColumn.ok[0] <= sensorValue && sensorValue <= sensorColumn.ok[1]) {
            return deltaWarn ? 'sensor-ok-outdated' : 'sensor-ok';
          } else if(sensorColumn.warn && sensorColumn.warn[0] <= sensorValue && sensorValue <= sensorColumn.warn[1]) {
            return deltaWarn ? 'sensor-warn-outdated' : 'sensor-warn';
          } else {
            return deltaWarn ? 'sensor-error-outdated' : 'sensor-error';
          }
        } else if(sensorColumn.warn) {
          if(sensorColumn.warn[0] <= sensorValue && sensorValue <= sensorColumn.warn[1]) {
            return deltaWarn ? 'sensor-warn-outdated' : 'sensor-warn';
          } else {
            return deltaWarn ? 'sensor-error-outdated' : 'sensor-error';
          }
        } else {
          return deltaWarn ? 'sensor-ok-outdated' : 'sensor-ok';
        }
      } else if(sensorColumn.datetime) {
        if(deltaWarn) {
          if(deltaError) {
            return 'time-error';
          } else {
            return 'time-warn';
          }
        } else {
          return 'time-ok';
        }
      } else {
        return '';
      }
    },
    customSort(rows, sortBy, descending) {
      const data = [...rows];
      if (sortBy) {
        const column = this.columnMap[sortBy];
        let func;
        switch(column.sortType) {
          case 'number': {
            if(descending) {
              func = (a, b) => parseFloat(b[sortBy]) - parseFloat(a[sortBy]);
            } else {
              func = (a, b) => parseFloat(a[sortBy]) - parseFloat(b[sortBy]);
            }
            break;
          }
          default: {
            if(descending) {
              func = (a, b) => b[sortBy] > a[sortBy] ? 1 : b[sortBy] < a[sortBy] ? -1 : 0;
            } else {
              func = (a, b) => a[sortBy] > b[sortBy] ? 1 : a[sortBy] < b[sortBy] ? -1 : 0;
            }
          }
        }
        if(func !== undefined) {
          data.sort(func);
        }
        /*data.sort((a, b) => {
          const x = descending ? b : a;
          const y = descending ? a : b;

          if (sortBy === 'plot') {
            // string sort
            return x[sortBy] > y[sortBy] ? 1 : x[sortBy] < y[sortBy] ? -1 : 0;
          } else {
            // numeric sort
            return parseFloat(x[sortBy]) - parseFloat(y[sortBy]);
          }
        })*/
      }

      return data;
    }    
  },
  watch: {
  },
  async mounted() {
  },
}
</script>

<style scoped>

.overview-item {
  margin-bottom: 20px;
}

.sensor-ok {
  /*background-color: #22aa22ad;*/
  background-color: #ffffff00;
}

.sensor-warn {
  background-color: #dbb10ead;
}

.sensor-error {
  background-color: #ec0f0fad;
}

.sensor-ok-outdated {
  background-color: #ffffff1f;
  color: #00000054;
}

.sensor-warn-outdated {
  background-color: #dbb20e1f;
  color: #00000054;
}

.sensor-error-outdated {
  background-color: #ec0f0f1f;
  color: #00000054;
}

.time-ok {
  color: #00000085;
}

.time-warn {
  color: #dbb10e85;
}

.time-error {
  color: #ec0f0f85;
}

</style>
