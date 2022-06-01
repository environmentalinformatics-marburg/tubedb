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
            :options="sets"
            option-label="set"             
            stack-label
            v-model="selectedSet"
            :display-value="selectedSet ? selectedSet.set : '* no set selected *'"
            options-dense
            dense
            style="width: 250px"
            title="Choose one predefined monitoring set containing plots, sensors and monitoring settings."
          />          
          <q-select 
            outlined 
            label="Select monitored plots"
            :options="selectedSet.plots" 
            stack-label
            v-model="selectedPlots"
            :display-value="selectedPlots ? selectedPlots.join(', ') : '* all plots *'"
            multiple
            clearable
            options-dense
            dense
            style="width: 250px"
            title="Leave empty to select all plots."
            v-if="selectedSet !== undefined"  
          />
          <q-select 
            outlined 
            label="Select monitored sensors"
            :options="selectedSet.sensors" 
            stack-label
            option-label="sensor"
            v-model="selectedSensors"
            :display-value="selectedSensors ? selectedSensors.map(s => s.sensor).join(', ') : '* all sensors *'"
            multiple
            clearable
            options-dense
            dense
            style="width: 250px"
            title="Leave empty to select all sensors."
            v-if="selectedSet !== undefined"  
          />
        </q-toolbar>
        <q-btn @click="refresh" :loading="dataLoading" icon="refresh" v-if="selectedSet !== undefined" >refresh</q-btn>
        
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
          v-if="selectedSet !== undefined"         
        >
        <template v-slot:body="props">
        <q-tr :props="props">
          <q-td key="plot" :props="props" :class="plotClass(props.row)">
            <b>{{props.row.plot}}</b>
          </q-td>

          <q-td :props="props" v-for="sensorColumn in sensorColumns" :key="sensorColumn.sensor" :class="cellClass(sensorColumn, props.row)" :title="props.row.plot + ' ' + sensorColumn.sensor">
            <span v-if="sensorColumn.number">
              {{props.row[sensorColumn.sensor] === -99999 ? '' : props.row[sensorColumn.sensor].toFixed(2)}}
            </span>
            <i v-else>
              {{props.row[sensorColumn.sensorTimestamp] === 0 ? '' : props.row[sensorColumn.sensor]}}
            </i>
          </q-td>          
        </q-tr>
        </template>
        </q-table>
      <div v-if="selectedSet === undefined" style="color: red; padding-top: 20px; font-size: 1.5em;">
        <q-icon name="event_note" /> Select a monitoring set!
      </div>
      <div v-if="selectedSet !== undefined && data === undefined" style="color: red; padding-top: 20px; font-size: 1.5em;">
        <q-icon name="event_note" /> Click refresh button to load data!
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
        sets: [
          {
            set: 'Exploratories AEG',
            plots: [
              'AEG01', 'AEG02', 'AEG03', 'AEG04', 'AEG05', 'AEG06', 'AEG07', 'AEG08', 'AEG09', 'AEG10', 
              'AEG11', 'AEG12', 'AEG13', 'AEG14', 'AEG15', 'AEG16', 'AEG17', 'AEG18', 'AEG19', 'AEG20',
              'AEG21', 'AEG22', 'AEG23', 'AEG24', 'AEG25', 'AEG26', 'AEG27', 'AEG28', 'AEG29', 'AEG30', 
              'AEG31', 'AEG32', 'AEG33', 'AEG34', 'AEG35', 'AEG36', 'AEG37', 'AEG38', 'AEG39', 'AEG40',
              'AEG41', 'AEG42', 'AEG43', 'AEG44', 'AEG45', 'AEG46', 'AEG47', 'AEG48', 'AEG49', 'AEG50',
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
              /*{sensor: 'SM_30', ok: [1, 65], warn: [0, 70]},*/
              {sensor: 'rH_200', ok: [15, 100], warn: [0, 100]},
              {sensor: 'LWDR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'LWUR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'SWDR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'SWUR_300', ok: [0, 1000], warn: [0, 1500]},
            ],
          },
          {
            set: 'Exploratories AEW',
            plots: [
              'AEW01', 'AEW02', 'AEW03', 'AEW04', 'AEW05', 'AEW06', 'AEW07', 'AEW08', 'AEW09', 'AEW10', 
              'AEW11', 'AEW12', 'AEW13', 'AEW14', 'AEW15', 'AEW16', 'AEW17', 'AEW18', 'AEW19', 'AEW20',
              'AEW21', 'AEW22', 'AEW23', 'AEW24', 'AEW25', 'AEW26', 'AEW27', 'AEW28', 'AEW29', 'AEW30', 
              'AEW31', 'AEW32', 'AEW33', 'AEW34', 'AEW35', 'AEW36', 'AEW37', 'AEW38', 'AEW39', 'AEW40',
              'AEW41', 'AEW42', 'AEW43', 'AEW44', 'AEW45', 'AEW46', 'AEW47', 'AEW48', 'AEW49', 'AEW50',
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
              /*{sensor: 'SM_30', ok: [1, 65], warn: [0, 70]},*/
              {sensor: 'rH_200', ok: [15, 100], warn: [0, 100]},
              {sensor: 'LWDR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'LWUR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'SWDR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'SWUR_300', ok: [0, 1000], warn: [0, 1500]},
            ],
          },   
          {
            set: 'Exploratories AEF',
            plots: [
              'AEW03_1_M', 'AEW03_1_S', 'AEW03_2_M', 'AEW03_2_N', 'AEW03_3_M',
              'AEW22_1_M', 'AEW22_1_N', 'AEW22_2_M', 'AEW22_2_S', 'AEW22_3_M',
              'AEW28_1_M', 'AEW28_1_N', 'AEW28_2_M', 'AEW28_2_S', 'AEW28_3_M',
              'AEW33_4_M', 'AEW33_1_M', 'AEW33_1_S', 'AEW33_2_M', 'AEW33_2_N', 'AEW33_3_M',
              'AEW34_1_M', 'AEW34_1_N', 'AEW34_2_M', 'AEW34_2_S', 'AEW34_3_M',
              'AEW39_1_M', 'AEW39_1_S', 'AEW39_2_M', 'AEW39_2_N', 'AEW39_3_M',
              'AEW42_1_M', 'AEW42_1_S', 'AEW42_2_M', 'AEW42_2_N', 'AEW42_3_M',
              'AEW47_1_M', 'AEW47_1_N', 'AEW47_2_M', 'AEW47_2_S', 'AEW47_3_M',
            ],
            sensors: [
              {sensor: 'tt_battery_voltage', ok: [3700, 5000], warn: [3500, 5000]},              
              {sensor: 'tt_air_temperature', ok: [-20, 35], warn: [-40, 40]},
              {sensor: 'tt_air_relative_humidity', ok: [15, 100], warn: [0, 100]},
            ],
          },
          {
            set: 'Exploratories AEF cloud',
            plots: [
              'AEW03_CLOUD', 'AEW22_CLOUD', 'AEW28_CLOUD', 'AEW33_CLOUD', 'AEW34_CLOUD', 'AEW39_CLOUD', 'AEW42_CLOUD', 'AEW47_CLOUD',
            ],
            sensors: [
              {sensor: 'tt_Battery_level', ok: [3800, 5000], warn: [3600, 5000]},              
              {sensor: 'ttraw_GSM_field level'},              
              {sensor: 'ttraw_accumulated_records'},
              {sensor: 'ttraw_records_to_send'},              
              {sensor: 'ttraw_TT01'},
              {sensor: 'ttraw_TT02'},
              {sensor: 'ttraw_TT03'},
              {sensor: 'ttraw_TT04'},
              {sensor: 'ttraw_TT05'},
              {sensor: 'ttraw_TT06'},
              {sensor: 'ttraw_TT07'},
              {sensor: 'ttraw_TT08'},
              {sensor: 'ttraw_TT09'},
              {sensor: 'ttraw_TT10'},
              {sensor: 'ttraw_TT11'},
              {sensor: 'ttraw_TT12'},
              {sensor: 'ttraw_TT13'},
              {sensor: 'ttraw_TT14'},
              {sensor: 'ttraw_TT15'},
              {sensor: 'ttraw_TT16'},
              {sensor: 'ttraw_TT17'},
              {sensor: 'ttraw_TT18'},
              {sensor: 'ttraw_TT19'},
              {sensor: 'ttraw_TT20'},
              {sensor: 'ttraw_TT21'},
            ],
          },                                     
          {
            set: 'Exploratories HEG',
            plots: [
              'HEG01', 'HEG02', 'HEG03', 'HEG04', 'HEG05', 'HEG06', 'HEG07', 'HEG08', 'HEG09', 'HEG10', 
              'HEG11', 'HEG12', 'HEG13', 'HEG14', 'HEG15', 'HEG16', 'HEG17', 'HEG18', 'HEG19', 'HEG20',
              'HEG21', 'HEG22', 'HEG23', 'HEG24', 'HEG25', 'HEG26', 'HEG27', 'HEG28', 'HEG29', 'HEG30', 
              'HEG31', 'HEG32', 'HEG33', 'HEG34', 'HEG35', 'HEG36', 'HEG37', 'HEG38', 'HEG39', 'HEG40',
              'HEG41', 'HEG42', 'HEG43', 'HEG44', 'HEG45', 'HEG46', 'HEG47', 'HEG48', 'HEG49', 'HEG50',
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
              /*{sensor: 'SM_30', ok: [1, 65], warn: [0, 70]},*/
              {sensor: 'rH_200', ok: [15, 100], warn: [0, 100]},
              {sensor: 'LWDR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'LWUR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'SWDR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'SWUR_300', ok: [0, 1000], warn: [0, 1500]},
            ],
          },
          {
            set: 'Exploratories HEW',
            plots: [
              'HEW01', 'HEW02', 'HEW03', 'HEW04', 'HEW05', 'HEW06', 'HEW07', 'HEW08', 'HEW09', 'HEW10', 
              'HEW11', 'HEW12', 'HEW13', 'HEW14', 'HEW15', 'HEW16', 'HEW17', 'HEW18', 'HEW19', 'HEW20',
              'HEW21', 'HEW22', 'HEW23', 'HEW24', 'HEW25', 'HEW26', 'HEW27', 'HEW28', 'HEW29', 'HEW30', 
              'HEW31', 'HEW32', 'HEW33', 'HEW34', 'HEW35', 'HEW36', 'HEW37', 'HEW38', 'HEW39', 'HEW40',
              'HEW41', 'HEW42', 'HEW43', 'HEW44', 'HEW45', 'HEW46', 'HEW47', 'HEW48', 'HEW49', 'HEW50',
              'HEW51',
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
              /*{sensor: 'SM_30', ok: [1, 65], warn: [0, 70]},*/
              {sensor: 'rH_200', ok: [15, 100], warn: [0, 100]},
              {sensor: 'LWDR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'LWUR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'SWDR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'SWUR_300', ok: [0, 1000], warn: [0, 1500]},
            ],
          },
          {
            set: 'Exploratories HEF',
            plots: [
              'HEW05_3_M', 'HEW05_1_M', 'HEW05_1_N', 'HEW05_2_M', 'HEW05_2_S',
              'HEW06_1_M', 'HEW06_1_S', 'HEW06_2_M', 'HEW06_2_N', 'HEW06_3_M',
              'HEW19_1_N', 'HEW19_2_M', 'HEW19_1_M', 'HEW19_2_S', 'HEW19_3_M',
              'HEW21_1_M', 'HEW21_1_S', 'HEW21_3_M', 'HEW21_2_M', 'HEW21_2_N',
              'HEW29_1_M', 'HEW29_1_S', 'HEW29_2_M', 'HEW29_2_N', 'HEW29_3_M',
              'HEW30_1_M', 'HEW30_1_N', 'HEW30_2_M', 'HEW30_2_S', 'HEW30_3_M',
              'HEW32_2_M', 'HEW32_1_M', 'HEW32_1_S', 'HEW32_2_N', 'HEW32_3_M',
              'HEW47_1_M', 'HEW47_1_N', 'HEW47_2_M', 'HEW47_2_S', 'HEW47_3_M',
              'HEW48_1_M', 'HEW48_1_S', 'HEW48_2_M', 'HEW48_2_N', 'HEW48_3_M', 'HEW48_4_M',
            ],
            sensors: [
              {sensor: 'tt_battery_voltage', ok: [3700, 5000], warn: [3500, 5000]},              
              {sensor: 'tt_air_temperature', ok: [-20, 35], warn: [-40, 40]},
              {sensor: 'tt_air_relative_humidity', ok: [15, 100], warn: [0, 100]},
            ],
          },
          {
            set: 'Exploratories HEF cloud',
            plots: [
              'HEW05_CLOUD', 'HEW06_CLOUD', 'HEW19_CLOUD', 'HEW21_CLOUD', 'HEW29_CLOUD', 'HEW30_CLOUD', 'HEW32_CLOUD', 'HEW47_CLOUD', 'HEW48_CLOUD',
            ],
            sensors: [
              {sensor: 'tt_Battery_level', ok: [3800, 5000], warn: [3600, 5000]},              
              {sensor: 'ttraw_GSM_field level'},              
              {sensor: 'ttraw_accumulated_records'},
              {sensor: 'ttraw_records_to_send'},              
              {sensor: 'ttraw_TT01'},
              {sensor: 'ttraw_TT02'},
              {sensor: 'ttraw_TT03'},
              {sensor: 'ttraw_TT04'},
              {sensor: 'ttraw_TT05'},
              {sensor: 'ttraw_TT06'},
              {sensor: 'ttraw_TT07'},
              {sensor: 'ttraw_TT08'},
              {sensor: 'ttraw_TT09'},
              {sensor: 'ttraw_TT10'},
              {sensor: 'ttraw_TT11'},
              {sensor: 'ttraw_TT12'},
              {sensor: 'ttraw_TT13'},
              {sensor: 'ttraw_TT14'},
              {sensor: 'ttraw_TT15'},
              {sensor: 'ttraw_TT16'},
              {sensor: 'ttraw_TT17'},
              {sensor: 'ttraw_TT18'},
              {sensor: 'ttraw_TT19'},
              {sensor: 'ttraw_TT20'},
              {sensor: 'ttraw_TT21'},
            ],
          },                              
          {
            set: 'Exploratories SEG',
            plots: [
              'SEG01', 'SEG02', 'SEG03', 'SEG04', 'SEG05', 'SEG06', 'SEG07', 'SEG08', 'SEG09', 'SEG10', 
              'SEG11', 'SEG12', 'SEG13', 'SEG14', 'SEG15', 'SEG16', 'SEG17', 'SEG18', 'SEG19', 'SEG20',
              'SEG21', 'SEG22', 'SEG23', 'SEG24', 'SEG25', 'SEG26', 'SEG27', 'SEG28', 'SEG29', 'SEG30', 
              'SEG31', 'SEG32', 'SEG33', 'SEG34', 'SEG35', 'SEG36', 'SEG37', 'SEG38', 'SEG39', 'SEG40',
              'SEG41', 'SEG42', 'SEG43', 'SEG44', 'SEG45', 'SEG46', 'SEG47', 'SEG48', 'SEG49', 'SEG50',
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
              /*{sensor: 'SM_30', ok: [1, 65], warn: [0, 70]},*/
              {sensor: 'rH_200', ok: [15, 100], warn: [0, 100]},
              {sensor: 'LWDR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'LWUR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'SWDR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'SWUR_300', ok: [0, 1000], warn: [0, 1500]},
            ],
          },
          {
            set: 'Exploratories SEW',
            plots: [
              'SEW01', 'SEW02', 'SEW03', 'SEW04', 'SEW05', 'SEW06', 'SEW07', 'SEW08', 'SEW09', 'SEW10', 
              'SEW11', 'SEW12', 'SEW13', 'SEW14', 'SEW15', 'SEW16', 'SEW17', 'SEW18', 'SEW19', 'SEW20',
              'SEW21', 'SEW22', 'SEW23', 'SEW24', 'SEW25', 'SEW26', 'SEW27', 'SEW28', 'SEW29', 'SEW30', 
              'SEW31', 'SEW32', 'SEW33', 'SEW34', 'SEW35', 'SEW36', 'SEW37', 'SEW38', 'SEW39', 'SEW40',
              'SEW41', 'SEW42', 'SEW43', 'SEW44', 'SEW45', 'SEW46', 'SEW47', 'SEW48', 'SEW49', 'SEW50',
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
              /*{sensor: 'SM_30', ok: [1, 65], warn: [0, 70]},*/
              {sensor: 'rH_200', ok: [15, 100], warn: [0, 100]},
              {sensor: 'LWDR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'LWUR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'SWDR_300', ok: [0, 1000], warn: [0, 1500]},
              {sensor: 'SWUR_300', ok: [0, 1000], warn: [0, 1500]},
            ],
          },
          {
            set: 'Exploratories SEF',
            plots: [
              'SEW03_2_M', 'SEW03_1_S', 'SEW03_3_M', 'SEW03_2_N', 'SEW03_1_M',
              'SEW04_1_M', 'SEW04_1_S', 'SEW04_2_M', 'SEW04_2_N', 'SEW04_3_M', 'SEW04_4_M',
              'SEW17_1_M', 'SEW17_1_N', 'SEW17_2_M', 'SEW17_2_S', 'SEW17_3_M',
              'SEW18_1_N', 'SEW18_1_M', 'SEW18_2_S', 'SEW18_2_M', 'SEW18_3_M',
              'SEW24_3_M', 'SEW24_2_M', 'SEW24_1_S', 'SEW24_2_N', 'SEW24_1_M',
              'SEW25_1_M', 'SEW25_1_S', 'SEW25_2_M', 'SEW25_2_N', 'SEW25_3_M',
              'SEW27_1_M', 'SEW27_1_N', 'SEW27_2_M', 'SEW27_2_S',
              'SEW31_1_M', 'SEW31_1_S', 'SEW31_2_M', 'SEW31_2_N', 'SEW31_3_M',
              'SEW33_1_M', 'SEW33_1_S', 'SEW33_2_M', 'SEW33_2_N', 'SEW33_3_M',
              'SEW35_3_M', 'SEW35_1_M', 'SEW35_1_N', 'SEW35_2_M', 'SEW35_2_S',
              'SEW36_3_M', 'SEW36_1_M', 'SEW36_1_N', 'SEW36_2_M', 'SEW36_2_S',
              'SEW49_3_M', 'SEW49_1_M', 'SEW49_1_N', 'SEW49_2_M', 'SEW49_2_S',
            ],
            sensors: [
              {sensor: 'tt_battery_voltage', ok: [3700, 5000], warn: [3500, 5000]},              
              {sensor: 'tt_air_temperature', ok: [-20, 35], warn: [-40, 40]},
              {sensor: 'tt_air_relative_humidity', ok: [15, 100], warn: [0, 100]},
            ],
          },
          {
            set: 'Exploratories SEF cloud',
            plots: [
              'SEW03_CLOUD', 'SEW04_CLOUD', 'SEW17_CLOUD', 'SEW18_CLOUD', 'SEW24_CLOUD', 'SEW25_CLOUD', 'SEW27_CLOUD', 'SEW31_CLOUD', 'SEW33_CLOUD', 'SEW35_CLOUD', 'SEW36_CLOUD', 'SEW49_CLOUD',
            ],
            sensors: [
              {sensor: 'tt_Battery_level', ok: [3800, 5000], warn: [3600, 5000]},              
              {sensor: 'ttraw_GSM_field level'},              
              {sensor: 'ttraw_accumulated_records'},
              {sensor: 'ttraw_records_to_send'},              
              {sensor: 'ttraw_TT01'},
              {sensor: 'ttraw_TT02'},
              {sensor: 'ttraw_TT03'},
              {sensor: 'ttraw_TT04'},
              {sensor: 'ttraw_TT05'},
              {sensor: 'ttraw_TT06'},
              {sensor: 'ttraw_TT07'},
              {sensor: 'ttraw_TT08'},
              {sensor: 'ttraw_TT09'},
              {sensor: 'ttraw_TT10'},
              {sensor: 'ttraw_TT11'},
              {sensor: 'ttraw_TT12'},
              {sensor: 'ttraw_TT13'},
              {sensor: 'ttraw_TT14'},
              {sensor: 'ttraw_TT15'},
              {sensor: 'ttraw_TT16'},
              {sensor: 'ttraw_TT17'},
              {sensor: 'ttraw_TT18'},
              {sensor: 'ttraw_TT19'},
              {sensor: 'ttraw_TT20'},
              {sensor: 'ttraw_TT21'},
            ],
          },                                        
        ],
      },
      selectedSet: undefined,
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
        align: 'left',
      }];
      if(this.selectedSet !== undefined && this.data !== undefined) {
        this.data.sensors.forEach(sensorName => {
          const sensor = this.selectedSet.sensors.find(sensor => sensor.sensor === sensorName);
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
      if(this.selectedSet !== undefined) {
        this.selectedSet.sensors.forEach(sensor => {          
          const timename = sensor.sensor + '.datetime';
          map[sensor.sensor] = {sortType: 'number'};
          map[timename] = {sortType: 'text'};
        });
      }
      return map;
    },
    sensorColumns() {
      let result = [];
      if(this.selectedSet !== undefined && this.data !== undefined) {
        const sensorNames = this.data.sensors;
        const sensors = this.selectedSet.sensors;
        sensorNames.forEach(sensorName => {
          const sensor = sensors.find(sensor => sensor.sensor === sensorName);
          if(sensor) {
            const timename = sensor.sensor + '.datetime';
            const timestampName = sensor.sensor + '.timestamp';
            result.push({sensor: timename, datetime: true, number: false, sensorTimestamp: timestampName});
            result.push({sensor: sensor.sensor, datetime: false, number: true, ok: sensor.ok, warn: sensor.warn, sensorTimestamp: timestampName});
          } else {
            console.log("sensor not found " + sensorName);
          }
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
    sets() {
      if(this.monitoring_meta === undefined) {
        return [];
      }
      return this.monitoring_meta.sets;
    },    
  },
  methods: {
    async refresh() {
      try {
        const params = new URLSearchParams();
        if(this.selectedPlots) {
          this.selectedPlots.forEach(plot => params.append('plot', plot));
        } else {       
          this.selectedSet.plots.forEach(plot => params.append('plot', plot));
        }
        if(this.selectedSensors) {
          this.selectedSensors.forEach(sensor => params.append('sensor', sensor.sensor));
        } else {
          this.selectedSet.sensors.forEach(sensor => params.append('sensor', sensor.sensor));
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
      //console.log(sensorColumn.sensorTimestamp);
      //console.log(delta);      
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
    plotClass(row) {
      let c = 'sensor-ok';
      this.sensorColumns.forEach(sensorColumn => {
        const sensorValue = row[sensorColumn.sensor];
        const sensorTimestamp = row[sensorColumn.sensorTimestamp];
        const delta = this.timestampNow - sensorTimestamp;
        const deltaWarn = delta > 100000 || delta < -24 * 60;
        const deltaError = delta > 200000 || delta < -2 * 24 * 60;
        if(sensorColumn.number) {
          if(sensorValue === -99999) { // missing
            // nothing
          } else if(sensorColumn.ok) {
            if(sensorColumn.ok[0] <= sensorValue && sensorValue <= sensorColumn.ok[1]) {
              // nothing
            } else if(sensorColumn.warn && sensorColumn.warn[0] <= sensorValue && sensorValue <= sensorColumn.warn[1]) {
              if(c === 'sensor-ok') {
                //console.log("set sensor-warn  " + sensorColumn.sensor);
                c = 'sensor-warn';
              }
            } else {
              //console.log("set sensor-error  " + sensorColumn.sensor);
              c = 'sensor-error';
            }
          } else if(sensorColumn.warn) {
            if(sensorColumn.warn[0] <= sensorValue && sensorValue <= sensorColumn.warn[1]) {
              if(c === 'sensor-ok') {
                //console.log("set sensor-warn  " + sensorColumn.sensor);
                c = 'sensor-warn';
              }
            } else {
              //console.log("set sensor-error  " + sensorColumn.sensor);
              c = 'sensor-error';
            }
          } else {
            // nothing
          }
        } else if(sensorColumn.datetime) {
          if(sensorTimestamp === 0) { // missing
            // nothing
          } else if(deltaWarn) {
            if(deltaError) {
              //console.log("set sensor-error  " + sensorColumn.sensor + "  " + sensorTimestamp);
              c = 'sensor-error';
            } else {
              if(c === 'sensor-ok') {
                //console.log("set sensor-warn  " + sensorColumn.sensor + "  " + sensorTimestamp);
                c = 'sensor-warn';
              }
            }
          } else {
            // nothing
          }
        } else {
          // nothing
        }                
      });
      return c;
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
    selectedSet() {
      this.selectedPlots = undefined;
      this.selectedSensors = undefined;
      this.data = undefined;
    },
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


th:first-child, td:first-child {
  position: sticky;
  left: 0;
}

</style>
