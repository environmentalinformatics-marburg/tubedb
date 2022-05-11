<template>
  <q-layout view="hHh LpR fFf">

    <q-header reveal elevated class="bg-grey-7 text-grey-4">
      <pages-toolbar title="TubeDB Monitoring" active="/monitoring"/>
    </q-header>

    <q-page-container class="row">
      <q-page padding class="column">
        <q-btn @click="refresh">refresh</q-btn>
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

          <q-td :props="props" v-for="sensorColumn in sensorColumns" :key="sensorColumn.sensor" :class="cellClass(sensorColumn, props.row[sensorColumn.sensor])">
            <span v-if="sensorColumn.number">
              {{props.row[sensorColumn.sensor].toFixed(2)}}
            </span>
            <i v-else>
              {{props.row[sensorColumn.sensor]}}
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
          {sensor: 'UB', ok: [14, 20], warn: [13, 25]}, 
          {sensor: 'Ta_200', ok: [10, 17], warn: [5, 20]}, 
          {sensor: 'rH_200', ok: [60, 90], warn: [50, 95]},
        ],
      },
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
      if(this.monitoring_meta !== undefined) {
        this.monitoring_meta.sensors.forEach(sensor => {
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
          result.push({sensor: timename, number: false});
          result.push({sensor: sensor.sensor, number: true, ok: sensor.ok, warn: sensor.warn});
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
        for (let i = 0; i < sensors.length; i++) {
          const sensor = sensors[i];
          const timename = sensor + '.datetime';
          row[timename] = datetimes[i];
          row[sensor] = values[i];
        }     
        return row;
      });
      return result;
    },       
  },
  methods: {
    async refresh() {
      try {
        const params = new URLSearchParams();
        this.monitoring_meta.plots.forEach(plot => params.append('plot', plot));
        this.monitoring_meta.sensors.forEach(sensor => params.append('sensor', sensor.sensor));
        const response = await this.apiGET(['tsdb', 'monitoring'], {params});
        this.data = response.data;
      } catch(e) {
        this.data = undefined;
        console.log(e);
        this.$q.notify({message: 'Error loading data.', type: 'negative'});
      }
    },
    cellClass(sensorColumn, sensorValue) {
      if(sensorColumn.number) {
        if(sensorColumn.ok) {
          if(sensorColumn.ok[0] <= sensorValue && sensorValue <= sensorColumn.ok[1]) {
            return 'sensor-ok';
          } else if(sensorColumn.warn && sensorColumn.warn[0] <= sensorValue && sensorValue <= sensorColumn.warn[1]) {
            return 'sensor-warn';
          } else {
            return 'sensor-error';
          }
        } else if(sensorColumn.warn) {
          if(sensorColumn.warn[0] <= sensorValue && sensorValue <= sensorColumn.warn[1]) {
            return 'sensor-warn';
          } else {
            return 'sensor-error';
          }
        } else {
          return '';
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
  background-color: #22aa22ad;
}

.sensor-warn {
  background-color: #dbb10ead;
}

.sensor-error {
  background-color: #ec0f0fad;
}

</style>
