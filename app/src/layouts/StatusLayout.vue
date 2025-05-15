<template>
  <q-layout view="hHh LpR fFf">

    <q-header reveal elevated class="bg-grey-7 text-grey-4">
      <pages-toolbar title="Plot status" active="/plot_status"/>
    </q-header>

    <q-page-container class="row justify-center">
      <q-page padding class="column" v-if="model !== undefined">

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
            :filter="filter"
          >

          <template v-slot:top-left>
            <q-select
              v-model="project"
              :options="projects"
              label="Project"
              option-label="title"
              filled
              :readonly="projects.length <= 1"
            />
            /
            <q-select
              v-model="group"
              :options="groups"
              label="Group"
              option-label="title"
              filled
              :readonly="groups.length <= 1"
            />
            /
            <q-select
              v-model="plot"
              :options="plots"
              label="Plot"
              option-label="title"
              filled
              :readonly="plots.length <= 1"
              :display-value="plot === undefined || plot === null || plot.length === 0 ? '(all)' : (plot.length === 1 ? plot[0] : '(multiple)')"
              multiple
              clearable
            />
          </template>

          <template v-slot:top-right>
            <q-input
              debounce="300"
              v-model="filter"
              stack-label
              label="Search"
              filled
              clearable
            >
              <template v-slot:append>
                <q-icon name="search" />
              </template>
            </q-input>
          </template>

          <template v-slot:body-cell-plot="props">
            <q-td :props="props">
              <a
                :href="api('content/visualisation_meta/visualisation_meta.html?pinned_project=' + project.id + '&pinned_plot=' + props.row.plot)"
                target="_blank"
                title="Open timeseries diagram in a new tab."
              >
                <q-icon name="timeline" @click.stop=""/>
              </a>
              {{props.row.plot}}
            </q-td>
          </template>

          <template v-slot:body-cell-first_datetime="props">
            <q-td :props="props">
              <span>{{props.row.first_date.substring(0, 4)}}</span>
              <span>{{props.row.first_date.substring(4, 7)}}</span>
              <span>{{props.row.first_date.substring(7)}} </span>
              <span>{{props.row.first_time}}</span>
            </q-td>
          </template>

          <template v-slot:body-cell-last_datetime="props">
            <q-td :props="props">
              <span>{{props.row.last_date.substring(0, 4)}}</span>
              <span>{{props.row.last_date.substring(4, 7)}}</span>
              <span>{{props.row.last_date.substring(7)}} </span>
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
      <div v-else>
        <div v-if="modelLoading">
          <q-spinner-ios color="primary" size="2em" />
          Loading metadata...
        </div>
        <div v-else-if="modelError">Error loading metadata. <q-btn @click="$store.dispatch('model/refresh')">try again</q-btn></div>
        <div v-else>Metadata not loaded.</div>
      </div>
    </q-page-container>

    <plot-status-dialog
      ref="plotStatusDialog"
      @changed="refresh"
    />
  </q-layout>
</template>

<script>

import { mapState, mapGetters } from 'vuex';

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
          align: 'left',
          sortable: true,
          classes: 'plot',
        },
        {
          name: 'first_datetime',
          field: 'first_datetime',
          label: 'Earliest data',
          headerStyle: 'text-align: center;',
          align: 'left',
          sortable: true,
        },
        {
          name: 'last_datetime',
          field: 'last_datetime',
          label: 'Latest data',
          headerStyle: 'text-align: center;',
          align: 'left',
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
          headerStyle: 'text-align: center; min-width: 150px; max-width: 150px;',
          align: 'left',
          sortable: true,
          style: 'min-width: 150px; max-width: 150px; overflow: hidden; text-overflow: ellipsis; background-color: rgba(0, 0, 255, 0.02);',
        },
        {
          name: 'tasks',
          field: 'tasks',
          label: 'Tasks',
          headerStyle: 'text-align: center; min-width: 150px; max-width: 150px;',
          align: 'left',
          sortable: true,
          style: 'min-width: 300px; max-width: 300px; overflow: hidden; text-overflow: ellipsis; background-color: #fff;',
        },
      ],
      pagination: {
        rowsPerPage: 0,
        sortBy: 'last_datetime',
      },
      rawRows: [],
      rowsLoading: false,
      project: undefined,
      group: undefined,
      plot: undefined,
      filter: undefined
    }
  },
  computed: {
    ...mapState({
      model: state => state.model.data,
      modelLoading: state => state.model.loading,
      modelError: state => state.model.error,
    }),
    ...mapGetters({
      api: 'api',
      apiGET: 'apiGET',
    }),
    projects() {
      if(this.model === undefined) {
        return [];
      }
      return Object.values(this.model.projects);
    },
    groups() {
      if(this.project === undefined) {
        return [];
      }
      return this.project.groups.map(id => this.model.groups[id]);
    },
    plots() {
      if(this.group === undefined) {
        return [];
      }
      return this.group.plots;
    },
    rows() {
      if(this.rawRows === undefined || this.rawRows.length === 0) {
        return [];
      }
      if(this.plot === undefined || this.plot === null || this.plot.length === 0) {
        return this.rawRows;
      }
      if(this.plot.length === 1) {
        const p = this.plot[0];
        return this.rawRows.filter(row => row.plot === p);
      }
      const ps = this.plot;
      return this.rawRows.filter(row => ps.includes(row.plot));
    }
  },
  methods: {
    async refresh() {
      if(this.group === undefined) {
        this.rawRows = [];
        return;
      }
      try {
        this.rowsLoading = true;
        const params = new URLSearchParams();
        params.append('generalstation', this.group.id);
        //params.append('region', this.project.id);
        params.append('plot_status', '');
        const response = await this.apiGET(['tsdb', 'status'], {params});
        let rawRows = response.data;
        rawRows.forEach(row => {
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
        this.rawRows = rawRows;
      } catch(e) {
        this.rawRows = [];
        console.log(e);
        this.$q.notify({message: 'Error loading data.', type: 'negative'});
      } finally {
        this.rowsLoading = false;
      }
    },
    elapsedClass(days) {
      let timeMark = "timeMarkSixWeeks";
      if(days > 365) {
        timeMark = "timeMarkLost";
      } else if(days > 7 * 6) {
        timeMark = "timeMarkSixWeeks";
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
      this.$refs.plotStatusDialog.show(this.project, row.plot);
    },
  },
  watch: {
    projects: {
      handler() {
        if(this.projects === undefined || this.projects.length === 0) {
          this.project = undefined;
        } else if(this.projects.length === 1) {
          this.project = this.projects[0];
        } else {
          //this.project = undefined;
          this.project = this.projects[0];
        }
      },
      immediate: true,
    },
    /*project() {
      this.refresh();
    },*/
    groups: {
      handler() {
        if(this.groups === undefined || this.groups.length === 0) {
          this.group = undefined;
        } else if(this.groups.length === 1) {
          this.group = this.groups[0];
        } else {
          //this.group = undefined;
          this.group = this.groups[0];
        }
      },
      immediate: true,
    },
    group() {
      this.refresh();
    },
    plots() {
      if(this.plots === undefined || this.plots.length === 0) {
        this.plot = undefined;
      } else if(this.plots.length === 1) {
        this.plot = [this.plots[0]];
      } else {
        this.plot = undefined;
        //this.plot = [this.plots[0]];
      }
    },
  },
  async mounted() {
    this.$store.dispatch('model/init');
    this.refresh();
  },
}
</script>

<style scoped>
.overview-item {
  margin-bottom: 0px;
}

td span:nth-child(1) {
  color: #000;
  font-weight: 700;
}

td span:nth-child(2) {
  color: #000;
  font-weight: 400;
}

td span:nth-child(3) {
  color: #000000a3;
  font-weight: 400;
}

td span:nth-child(4) {
  padding-left: 7px;
  color: #00000080;
  font-weight: 400;
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
td.timeMarkSixWeeks { background-color: #ff4444; }
td.timeMarkTwoWeeks { background-color: #ff9944; }
td.timeMarkOneWeek { background-color: #ffff44; }
td.timeMarkNow { background-color: #44ff44; }

td.plot {
  background-color: #f4f4f4;
  border-right: 1px solid #c6c6c6;
  /*font-weight: bold;
  font-family: monospace;*/
}

td.plot a {
  cursor: zoom-in;
}

td.plot a:hover {
  background-color: white;
}

</style>
