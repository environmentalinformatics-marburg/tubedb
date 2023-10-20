<template>
  <q-layout view="hHh LpR fFf">

    <q-header reveal elevated class="bg-grey-7 text-grey-4">
      <pages-toolbar title="Plot status" active="/plot_status"/>
    </q-header>

    <q-page-container class="row justify-center">
      <q-page padding class="column justify-center">

        <q-card bordered class="overview-item">
          <q-table
            :rows="rows"
            :columns="columns"
            :pagination="pagination"
            hide-bottom
            dense
            :loading="rowsLoading"
            row-key="plot"
            binary-state-sort
            @row-click="onRowClick"
            table-header-class="table-header"
          >

          <template v-slot:top>
            Select plot
          </template>

          <template v-slot:body-cell-first_datetime="props">
            <q-td :props="props">
              {{props.row.first_date}}
              <span>{{props.row.first_time}}</span>
            </q-td>
          </template>

          <template v-slot:body-cell-last_datetime="props">
            <q-td :props="props">
              {{props.row.last_date}}
              <span>{{props.row.last_time}}</span>
            </q-td>
          </template>

          <template v-slot:body-cell-voltage="props">
            <q-td :props="props">
              {{props.row.voltage === undefined ? '-' : props.row.voltage.toFixed(2)}}
            </q-td>
          </template>

          </q-table>
        </q-card>



      </q-page>
    </q-page-container>

    <plot-status-dialog
      ref="plotStatusDialog"
      @changed="refresh"
    />
  </q-layout>
</template>

<script>

import { mapGetters } from 'vuex';

import pagesToolbar from 'components/pages-toolbar.vue';
import plotStatusDialog from 'components/plot-status-dialog.vue';

export default {
  components: {
    pagesToolbar,
    plotStatusDialog,
  },
  data () {
    return {
      columns: [
        {
          name: 'plot',
          field: 'plot',
          label: 'Plot',
          headerStyle: 'text-align: center; border-right: 1px solid #c6c6c6;',
          align: 'center',
          sortable: true,
          classes: 'plot',
        },
        {
          name: 'first_datetime',
          field: 'first_datetime',
          label: 'Earliest data',
          headerStyle: 'text-align: center;',
          align: 'center',
          sortable: true,
        },
        {
          name: 'last_datetime',
          field: 'last_datetime',
          label: 'Latest data',
          headerStyle: 'text-align: center;',
          align: 'center',
          sortable: true,
        },
        {
          name: 'elapsed_days',
          field: 'elapsed_days',
          label: 'Elapsed days',
          headerStyle: 'text-align: center;',
          align: 'right',
          sortable: true,
          classes: row => this.elapsedClass(row.elapsed_days),
        },
        {
          name: 'voltage',
          field: 'voltage',
          label: 'Latest voltage',
          headerStyle: 'text-align: center;',
          align: 'right',
          sortable: true,
          classes: this.voltageClass,
        },
        {
          name: 'status',
          field: 'status',
          label: 'Status',
          headerStyle: 'text-align: center;',
          align: 'left',
          sortable: true,
          style: 'max-width: 150px; overflow: hidden; text-overflow: ellipsis; background-color: rgba(0, 0, 255, 0.02);',
        },
        {
          name: 'tasks',
          field: 'tasks',
          label: 'Tasks',
          headerStyle: 'text-align: center;',
          align: 'left',
          sortable: true,
          style: 'max-width: 300px; overflow: hidden; text-overflow: ellipsis; background-color: #fff;',
        },
      ],
      pagination: {
        rowsPerPage: 0,
        sortBy: 'last_datetime',
      },
      rows: [],
      rowsLoading: false,
    }
  },
  computed: {
    ...mapGetters({
      apiGET: 'apiGET',
    }),
  },
  methods: {
    async refresh() {
      try {
        this.rowsLoading = true;
        const params = new URLSearchParams();
        params.append('generalstation', 'HEG');
        //params.append('region', 'BE');
        params.append('plot_status', '');
        const response = await this.apiGET(['tsdb', 'status'], {params});
        let rows = response.data;
        rows.forEach(row => {
          {
            const a = row.first_datetime.split('T');
            row.first_date = a[0];
            row.first_time = a[1];
          }
          {
            const a = row.last_datetime.split('T');
            row.last_date = a[0];
            row.last_time = a[1];
          }
        });
        this.rows = rows;
      } catch(e) {
        this.rows = [];
        console.log(e);
        this.$q.notify({message: 'Error loading data.', type: 'negative'});
      } finally {
        this.rowsLoading = false;
      }
    },
    elapsedClass(days) {
      let timeMark = "timeMarkOneMonth";
      if(days > 365) {
        timeMark = "timeMarkLost";
      } else if(days > 7 * 4) {
        timeMark = "timeMarkOneMonth";
      } else if(days > 7 * 2) {
        timeMark = "timeMarkTwoWeeks";
      } else if(days > 7) {
        timeMark = "timeMarkOneWeek";
      } else {
        timeMark = "timeMarkNow";
      }
      return timeMark;
    },
    voltageClass: function(row) {
      const voltage = row.voltage;
      let voltageMark = "voltageMarkNaN";
      if(voltage !== undefined && voltage >= 0 && voltage < row.voltage_min_error) {
        if(row.voltage_min_good <= voltage) {
          voltageMark = "voltageMarkOK";
        } else if(row.voltage_min_watch <= voltage) {
          voltageMark = "voltageMarkWARN";
        } else {
          voltageMark = "voltageMarkCRITICAL";
        }
      }
      return voltageMark;
    },
    onRowClick(e, row) {
      console.log(row.plot);
      this.$refs.plotStatusDialog.show(row.plot);
    },
  },
  watch: {
  },
  async mounted() {
    this.refresh();
  },
}
</script>

<style scoped>
.overview-item {
  margin-bottom: 0px;
}

td span {
  color: #999;
}

td.voltageMarkNaN{ background-color:#88888877; }
td.voltageMarkOK{ background-color: #44ff4477;  }
td.voltageMarkWARN { background-color: #ffff4477; }
td.voltageMarkCRITICAL { background-color: #ff444477; }

</style>

<style>

.table-header th {
  background-color:#f4f4f4;
  border-top: 1px solid #eaeaea;;
}

td.timeMarkLost { background-color: #666666; }
td.timeMarkOneMonth { background-color: #ff4444; }
td.timeMarkTwoWeeks { background-color: #ff9944; }
td.timeMarkOneWeek { background-color: #ffff44; }
td.timeMarkNow { background-color: #44ff44; }

td.plot {
  /*font-weight: bold;*/
  background-color: #f4f4f4;
  border-right: 1px solid #c6c6c6;
}

</style>
