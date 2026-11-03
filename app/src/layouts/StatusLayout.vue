<template>
  <q-layout view="hHh LpR fFf">

    <q-header reveal elevated class="bg-grey-7 text-grey-4">
      <pages-toolbar title="Plot status" active="/plot_status"/>
    </q-header>

    <q-page-container class="row justify-center">
      <q-page padding class="column" v-if="model !== undefined">

        <q-card bordered class="overview-item">

          <div class="table-toolbar">

          <div class="table-toolbar-left">
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

            <q-input
              v-if="viewMode === 'table'"
              debounce="300"
              v-model="filter"
              stack-label
              label="Search"
              filled
              clearable
              style="margin-left: 10px;"
            >
              <template v-slot:append>
                <q-icon name="search" />
              </template>
            </q-input>

            <div v-if="viewMode === 'report'" class="report-toggles">
              <q-checkbox
                v-model="includeOutdated"
                label="Outdated"
                dense
              />
              <q-checkbox
                v-model="includeLowVoltage"
                label="Low voltage"
                dense
              />
            </div>
          </div>

          <div class="table-toolbar-right">
            <q-btn-toggle
              v-model="viewMode"
              no-caps
              toggle-color="primary"
              :options="[
                { label: 'Table', value: 'table', icon: 'table_chart' },
                { label: 'Report', value: 'report', icon: 'summarize' },
              ]"
            />
            <q-btn
              no-caps
              icon="download"
              label="Excel"
              outline
              @click="viewMode === 'table' ? exportTable() : exportReport()"
              class="q-mr-md"
              style="margin-left: 10px;"
              dense
            />
          </div>

          </div>

          <q-table
            v-if="viewMode === 'table'"          
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
            class="sticky-header-table"
          >      

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

          <template v-slot:body-cell-tasks="props">
            <q-td :props="props">
              <q-tooltip anchor="bottom middle" self="top middle" :offset="[0, 8]">
                <div v-if="Array.isArray(props.row.tasks)">
                  <div v-for="(t, i) in visibleTasks(props.row.tasks)" :key="i">{{ t.task }}</div>
                </div>
                <div v-else>{{ props.row.tasks }}</div>
              </q-tooltip>
              <span v-if="!props.row.tasks || (Array.isArray(props.row.tasks) && visibleTasks(props.row.tasks).length === 0)">-</span>
              <span v-else-if="typeof props.row.tasks === 'string'">{{ props.row.tasks }}</span>
              <span v-else>{{ visibleTasks(props.row.tasks).map(t => t.task).join('; ') }}</span>
            </q-td>
          </template>

          </q-table>

          <q-table
            v-if="viewMode === 'report'"          
            :rows="reportRows"
            :columns="reportColumns"
            :pagination="pagination"
            hide-bottom
            dense
            :loading="rowsLoading"
            row-key="id"
            binary-state-sort
            @row-click="onRowClick"
            table-header-class="table-header"
            class="sticky-header-table"
          >
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

          <template v-slot:body-cell-task="props">            
            <q-td :props="props">
              <q-icon v-if="props.row.icon" :name="props.row.icon" size="xs" color="orange-8" class="q-mr-xs" />
              <span :class="{ 'generated-task': !!props.row.generated }">{{ props.row.task }}</span>
              <q-tooltip>
                {{ props.row.task }}
              </q-tooltip>
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
      :transmission-options="transmissionOptions"
      :condition-options="conditionOptions"
    />
  </q-layout>
</template>

<script>

import ExcelJS from 'exceljs';

import { mapState, mapGetters } from 'vuex';

import pagesToolbar from 'components/pages-toolbar.vue';
import plotStatusDialog from 'components/plot-status-dialog.vue';


const EXCEL_DAY_COLORS = {
  timeMarkLost: 'FF666666',
  timeMarkSixWeeks: 'FFFF4444',
  timeMarkTwoWeeks: 'FFFF9944',
  timeMarkOneWeek: 'FFFFFF44',
  timeMarkNow: 'FF44FF44',
};

const EXCEL_VOLTAGE_COLORS = {
  voltageMarkNaN: 'FFC7C7C7',       // #88888877
  voltageMarkOK: 'FFA8FFA8',       // #44ff4477
  voltageMarkWARN: 'FFFFFFA8',     // #ffff4477
  voltageMarkCRITICAL: 'FFFFA8A8', // #ff444477
};

const EXCEL_THIN = { style: 'thin', color: { argb: 'FFE0E0E0' } };
const EXCEL_PLOT_SIDE = { style: 'thin', color: { argb: 'FFC6C6C6' } };

export default {
  components: {
    pagesToolbar,
    plotStatusDialog,
  },
  data () {
    return {
      viewMode: 'table',
      columns: [
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
          name: 'plot',
          field: 'plot',
          label: 'Plot',
          headerStyle: 'text-align: center; border-left: 1px solid #c6c6c6; border-right: 1px solid #c6c6c6; background-color: #d8d8d8; font-weight: 800;',
          align: 'left',
          sortable: true,
          classes: 'plot',
        },
        {
          name: 'status',
          field: 'status',
          label: 'Transmission',
          headerStyle: 'text-align: center; min-width: 150px; max-width: 150px;',
          align: 'left',
          sortable: true,
          style: 'min-width: 120px; max-width: 120px; overflow: hidden; text-overflow: ellipsis; background-color: rgba(255, 255, 0, 0.02); color: #757575;',
        },
        {
          name: 'condition',
          field: 'condition',
          label: 'Condition',
          headerStyle: 'text-align: center; min-width: 150px; max-width: 150px;',
          align: 'left',
          sortable: true,
          style: 'min-width: 250px; max-width: 250px; overflow: hidden; text-overflow: ellipsis; background-color: rgba(0, 0, 255, 0.02);',
        },        
        {
          name: 'tasks',
          field: 'tasks',
          label: 'Tasks',
          headerStyle: 'text-align: center; min-width: 150px; max-width: 150px;',
          align: 'left',
          sortable: true,
          classes: 'tasks-column',
          style: 'min-width: 400px; max-width: 400px; overflow: hidden; text-overflow: ellipsis; background-color: #fff; color: #1976d2;',
        },       
      ],
      reportColumns: [
        {
          name: 'plot',
          field: 'plot',
          label: 'Plot',
          headerStyle: 'text-align: center; border-left: 1px solid #c6c6c6; border-right: 1px solid #c6c6c6; background-color: #d8d8d8; font-weight: 800;',
          align: 'left',
          sortable: true,
          classes: 'plot',
        },
        {
          name: 'task',
          field: 'task',
          label: 'Task',
          headerStyle: 'text-align: center; min-width: 150px; max-width: 150px;',
          align: 'left',
          sortable: true,
          classes: 'tasks-column',
          style: 'min-width: 400px; max-width: 400px; overflow: hidden; text-overflow: ellipsis; background-color: #fff; color: #1976d2;',
        },
        {
          name: 'status',
          field: 'status',
          label: 'Status',
          headerStyle: 'text-align: center; min-width: 60px; max-width: 60px;',
          align: 'center',
          sortable: true,
          style: 'min-width: 60px; max-width: 60px; overflow: hidden; text-overflow: ellipsis; background-color: rgba(255, 255, 0, 0.02); color: #757575;',
        },
        {
          name: 'elapsed_days',
          field: 'elapsed_days',
          label: 'Elapsed days',
          headerStyle: 'text-align: center; min-width: 85px; max-width: 85px;',
          align: 'right',
          sortable: true,
          classes: row => this.elapsedClass(row.elapsed_days),
          style: 'min-width: 85px; max-width: 85px; overflow: hidden; text-overflow: ellipsis;', 
        },
        {
          name: 'voltage',
          field: 'voltage',
          label: 'Latest voltage',
          headerStyle: 'text-align: center; min-width: 100px; max-width: 100px;',
          align: 'right',
          sortable: true,
          classes: this.voltageClass,
          style: 'min-width: 100px; max-width: 100px; overflow: hidden; text-overflow: ellipsis;',          
        },                                        
      ],      
      pagination: {
        rowsPerPage: 0,
        sortBy: 'plot',
      },
      rawRows: [],
      rowsLoading: false,
      project: undefined,
      group: undefined,
      plot: undefined,
      filter: undefined,
      transmissionOptions: ['A', 'B', 'C'],
      conditionOptions: ['A', 'B', 'C'],
      includeOutdated: false,
      includeLowVoltage: false,
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
    },
    reportRows() {
      const result = [];
      this.rows.forEach(row => {
        const base = {
          plot: row.plot,
          last_date: row.last_date,
          last_time: row.last_time,
          elapsed_days: row.elapsed_days,
          voltage: row.voltage,
          voltage_min_error: row.voltage_min_error,
          voltage_min_good: row.voltage_min_good,
          voltage_min_watch: row.voltage_min_watch,
        };

        const tasks = this.visibleTasks(row.tasks);
        if (Array.isArray(tasks)) {
          tasks.forEach((t, i) => {
            result.push({ ...base, id: row.plot + '#' + i, task: t.task, status: t.status || 'open' });
          });
        } else if (typeof tasks === 'string' && tasks.trim() !== '' && tasks.trim() !== '-') {
          result.push({ ...base, id: row.plot + '#text', task: tasks, status: 'open' });
        }

        if (this.includeOutdated && this.isDataMissing(row)) {
          result.push({
            ...base,
            id: row.plot + '#auto_data',
            task: this.includeLowVoltage && this.isLowVoltage(row) ? 'outdated & low voltage' : 'outdated',
            //status: 'open',
            generated: true,
            icon: 'history',
          });
        }
        if (this.includeLowVoltage && this.isLowVoltage(row) && (!this.includeOutdated || !this.isDataMissing(row))) {
          result.push({
            ...base,
            id: row.plot + '#auto_voltage',
            task: 'low voltage',
            //status: 'open',
            generated: true,
            icon: 'battery_alert',
          });
        }
      });
      return result;
    },
  },
  methods: {
    visibleTasks(tasks) {
      if (!Array.isArray(tasks)) return tasks;
      return tasks.filter(t => {
        const status = t.status || (t.done ? 'done' : 'open'); // Legacy: alte Tasks mit done-Flag
        return status !== 'done' && status !== 'cancelled';
      });
    },
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
        const response = await this.apiGET(['tsdb', 'status2'], {params});
        
        let rawData = response.data;
        let rawRows = [];
        
        if (Array.isArray(rawData)) {
          // Backward compatibility: old API returns array directly
          rawRows = rawData;
        } else if (rawData && typeof rawData === 'object') {
          // New API structure: object with results
          rawRows = rawData.results || [];
          
          // options if available
          if (rawData.transmission_options && Array.isArray(rawData.transmission_options)) {
            this.transmissionOptions = rawData.transmission_options;
          }
          if (rawData.condition_options && Array.isArray(rawData.condition_options)) {
            this.conditionOptions = rawData.condition_options;
          }
        }
        
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
    isDataMissing(row) {
      const mark = this.elapsedClass(row.elapsed_days);
      return mark === 'timeMarkTwoWeeks' ||
        mark === 'timeMarkSixWeeks';
    },
    isLowVoltage(row) {
      const mark = this.voltageClass(row);
      return mark === 'voltageMarkWARN' || mark === 'voltageMarkCRITICAL';
    },
    tasksText(row) {
      if (!row.tasks) return '-';
      if (Array.isArray(row.tasks)) {
        const visible = this.visibleTasks(row.tasks);
        if (visible.length === 0) return '-';
        return visible.map(t => t.task).join('; ');
      }
      return row.tasks;
    },

    tableExportRows() {
      const term = this.filter === undefined || this.filter === null
        ? ''
        : String(this.filter).trim().toLowerCase();
      if (term === '') {
        return this.rows;
      }
      return this.rows.filter(row => [
        row.plot,
        row.status,
        row.condition,
        this.tasksText(row),
        row.first_datetime,
        row.last_datetime,
        row.elapsed_days,
        row.voltage,
      ].some(v => v !== undefined && v !== null && String(v).toLowerCase().includes(term)));
    },

    setExcelDatetime(cell, date, time) {
      cell.value = {
        richText: [
          { font: { bold: true, color: { argb: 'FF000000' } }, text: date.substring(0, 4) },
          { font: { color: { argb: 'FF000000' } }, text: date.substring(4, 7) },
          { font: { color: { argb: 'FF5C5C5C' } }, text: date.substring(7) + ' ' },
          { font: { color: { argb: 'FF808080' } }, text: time },
        ],
      };
    },

    async downloadWorkbook(workbook, filename) {
      const buffer = await workbook.xlsx.writeBuffer();
      const blob = new Blob([buffer], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      a.click();
      URL.revokeObjectURL(url);
    },

    async exportTable() {
      const workbook = new ExcelJS.Workbook();
      const sheet = workbook.addWorksheet(this.group.id);

      const columns = [
        { header: 'Plot', key: 'plot', width: 10 },        
        { header: 'Earliest data', key: 'first_datetime', width: 19 },
        { header: 'Latest data', key: 'last_datetime', width: 19 },
        { header: 'Elapsed days', key: 'elapsed_days', width: 12 },
        { header: 'Latest voltage', key: 'voltage', width: 14 },
        { header: 'Transmission', key: 'status', width: 16 },
        { header: 'Condition', key: 'condition', width: 22 },
        { header: 'Tasks', key: 'tasks', width: 100 },
      ];
      sheet.columns = columns;
      const keys = columns.map(c => c.key);

      const header = sheet.getRow(1);
      header.height = 20;
      keys.forEach(key => {
        const cell = header.getCell(key);
        cell.font = { bold: true };
        cell.alignment = { horizontal: 'center', vertical: 'middle' };
        cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFF4F4F4' } };
        cell.border = {
          top: { style: 'thin', color: { argb: 'FFEAEAEA' } },
          bottom: EXCEL_THIN,
          left: EXCEL_THIN,
          right: EXCEL_THIN,
        };
      });

      const plotHeader = header.getCell('plot');
      plotHeader.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFD8D8D8' } };
      plotHeader.border = {
        top: { style: 'thin', color: { argb: 'FFEAEAEA' } },
        bottom: EXCEL_THIN,
        left: EXCEL_PLOT_SIDE,
        right: EXCEL_PLOT_SIDE,
      };

      this.tableExportRows().forEach(row => {
        const added = sheet.addRow({
          elapsed_days: row.elapsed_days,
          voltage: row.voltage === undefined ? '-' : Number(row.voltage.toFixed(2)),
          plot: row.plot,
          status: row.status,
          condition: row.condition,
          tasks: this.tasksText(row),
        });

        this.setExcelDatetime(added.getCell('first_datetime'), row.first_date, row.first_time);
        this.setExcelDatetime(added.getCell('last_datetime'), row.last_date, row.last_time);

        const plotCell = added.getCell('plot');
        plotCell.alignment = { horizontal: 'center', vertical: 'middle' };
        plotCell.font = { bold: true };
        plotCell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFF4F4F4' } };

        added.getCell('status').font = { color: { argb: 'FF757575' } };

        added.getCell('tasks').font = { color: { argb: 'FF1976D2' } };

        const mark = this.elapsedClass(row.elapsed_days);
        if (EXCEL_DAY_COLORS[mark]) {
          added.getCell('elapsed_days').fill = {
            type: 'pattern', pattern: 'solid', fgColor: { argb: EXCEL_DAY_COLORS[mark] },
          };
        }

        const vMark = this.voltageClass(row);
        if (EXCEL_VOLTAGE_COLORS[vMark]) {
          added.getCell('voltage').fill = {
            type: 'pattern', pattern: 'solid', fgColor: { argb: EXCEL_VOLTAGE_COLORS[vMark] },
          };
        }
        if (row.voltage !== undefined) {
          added.getCell('voltage').numFmt = '0.00';
        }

        added.getCell('elapsed_days').alignment = { horizontal: 'right' };
        added.getCell('voltage').alignment = { horizontal: 'right' };

        keys.forEach(key => {
          added.getCell(key).border = { top: EXCEL_THIN, bottom: EXCEL_THIN, left: EXCEL_THIN, right: EXCEL_THIN };
        });
        plotCell.border = { top: EXCEL_THIN, bottom: EXCEL_THIN, left: EXCEL_PLOT_SIDE, right: EXCEL_PLOT_SIDE };
      });

      sheet.views = [{ state: 'frozen', xSplit: 1, ySplit: 1, topLeftCell: 'B2' }];
      sheet.autoFilter = { from: { row: 1, column: 1 }, to: { row: 1, column: columns.length } };

      await this.downloadWorkbook(workbook, `plot_status_table_${this.group.id}_${new Date().toISOString().slice(0, 10)}.xlsx`);
    },

    async exportReport() {
      const workbook = new ExcelJS.Workbook();
      const sheet = workbook.addWorksheet(this.group.id);

      sheet.columns = [
        { header: 'Plot', key: 'plot', width: 10 },
        { header: 'Task', key: 'task', width: 100 },
        { header: 'Status', key: 'status', width: 12 },
        { header: 'Elapsed days', key: 'elapsed_days', width: 12 },
        { header: 'Latest voltage', key: 'voltage', width: 14 },
      ];

      const header = sheet.getRow(1);
      header.height = 20;
      ['plot', 'task', 'status', 'elapsed_days', 'voltage'].forEach(key => {
        const cell = header.getCell(key);
        cell.font = { bold: true };
        cell.alignment = { horizontal: 'center', vertical: 'middle' };
        cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFF4F4F4' } };
        cell.border = {
          top: { style: 'thin', color: { argb: 'FFEAEAEA' } },
          bottom: EXCEL_THIN,
          left: EXCEL_THIN,
          right: EXCEL_THIN,
        };
      });
      const plotHeader = header.getCell('plot');
      plotHeader.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFD8D8D8' } };
      plotHeader.border = {
        top: { style: 'thin', color: { argb: 'FFEAEAEA' } },
        bottom: EXCEL_THIN,
        left: EXCEL_PLOT_SIDE,
        right: EXCEL_PLOT_SIDE,
      };

      this.reportRows.forEach(row => {
        const added = sheet.addRow({
          plot: row.plot,
          task: row.task,
          status: row.status,
          elapsed_days: row.elapsed_days,
          voltage: row.voltage === undefined ? '-' : Number(row.voltage.toFixed(2)),
        });

        const plotCell = added.getCell('plot');
        plotCell.alignment = { horizontal: 'center', vertical: 'middle' };
        plotCell.font = { bold: true };
        plotCell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFF4F4F4' } };

        const taskCell = added.getCell('task');
        taskCell.alignment = { wrapText: true, vertical: 'middle' };
        if (row.generated) {
          taskCell.font = { color: { argb: 'FFE65100' }, italic: true };
        } else {
          taskCell.font = { color: { argb: 'FF1976D2' } };
        }

        added.getCell('status').font = { color: { argb: 'FF757575' } };
        added.getCell('status').alignment = { horizontal: 'center', vertical: 'middle' };

        const mark = this.elapsedClass(row.elapsed_days);
        if (EXCEL_DAY_COLORS[mark]) {
          added.getCell('elapsed_days').fill = {
            type: 'pattern', pattern: 'solid', fgColor: { argb: EXCEL_DAY_COLORS[mark] },
          };
        }

        const vMark = this.voltageClass(row);
        if (EXCEL_VOLTAGE_COLORS[vMark]) {
          added.getCell('voltage').fill = {
            type: 'pattern', pattern: 'solid', fgColor: { argb: EXCEL_VOLTAGE_COLORS[vMark] },
          };
        }
        if (row.voltage !== undefined) {
          added.getCell('voltage').numFmt = '0.00';
        }

        added.getCell('elapsed_days').alignment = { horizontal: 'right', vertical: 'middle' };
        added.getCell('voltage').alignment = { horizontal: 'right', vertical: 'middle' };

        ['plot', 'task', 'status', 'elapsed_days', 'voltage'].forEach(key => {
          added.getCell(key).border = { top: EXCEL_THIN, bottom: EXCEL_THIN, left: EXCEL_THIN, right: EXCEL_THIN };
        });
        plotCell.border = { top: EXCEL_THIN, bottom: EXCEL_THIN, left: EXCEL_PLOT_SIDE, right: EXCEL_PLOT_SIDE };
      });

      sheet.views = [{ state: 'frozen', xSplit: 1, ySplit: 1, topLeftCell: 'B2' }];
      sheet.autoFilter = { from: { row: 1, column: 1 }, to: { row: 1, column: 5 } };

      await this.downloadWorkbook(workbook, `plot_status_report_${this.group.id}_${new Date().toISOString().slice(0, 10)}.xlsx`);
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
        if (this.groups === undefined || this.groups.length === 0) {
          this.group = undefined;
        } else {
          const defaultId = this.project && this.project.default_group;
          const defaultGroup = defaultId
            ? this.groups.find(g => g !== undefined && g.id === defaultId)
            : undefined;
          this.group = defaultGroup !== undefined ? defaultGroup : this.groups[0];
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

td.tasks-column span {
  min-width: 400px; 
  max-width: 400px; 
  overflow: hidden; 
  text-overflow: ellipsis; 
  background-color: #fff;
  color: #1976d2;
  font-weight: 400;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px 16px;
  padding: 12px 16px; 
}

.table-toolbar-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.table-toolbar-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.report-toggles {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0px;
  margin-left: 8px;
}

</style>

<style>

.table-header th {
  background-color:#f4f4f4;
  border-top: 1px solid #eaeaea;
}

td.timeMarkLost { background-color: #666666; }
td.timeMarkSixWeeks { background-color: #ff4444; }
td.timeMarkTwoWeeks { background-color: #ff9944; }
td.timeMarkOneWeek { background-color: #ffff44; }
td.timeMarkNow { background-color: #44ff44; }

td.plot {
  background-color: #e0e0e0;
  border-left: 1px solid #c6c6c6;
  border-right: 1px solid #c6c6c6;
  font-weight: 500;
  /*font-weight: bold;
  font-family: monospace;*/
}

td.plot a {
  cursor: zoom-in;
}

td.plot a:hover {
  background-color: white;
}

td.tasks-column span.generated-task {
  color: #e65100;
  font-style: italic;
}

.sticky-header-table .q-table__middle {
  overflow: visible;
}

.sticky-header-table thead tr th {
  position: sticky;
  top: 0;
  z-index: 1;
  background-color: #f4f4f4;
}

</style>