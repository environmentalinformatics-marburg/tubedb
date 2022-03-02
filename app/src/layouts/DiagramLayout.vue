<template>
  <q-layout view="hHh LpR fFf">

    <q-header reveal elevated class="bg-grey-7 text-grey-4">
      <pages-toolbar title="TubeDB Diagram" active="/diagram"/>
    </q-header>

    <q-drawer show-if-above side="left" behavior="desktop" content-class="bg-grey-4" :width="drawerWidth">    
      <div class="fit row">
      <q-scroll-area class="col-grow" v-if="model !== undefined">  
        <q-list>

          <q-item tag="label" >
            <q-item-section>
              <q-select 
                v-model="timeAggregation" 
                :options="['none', 'hour', 'day', 'week', 'month', 'year']" 
                label="Aggregation by time" 
                stack-label 
                borderless 
                :dense="true" 
                :options-dense="true"
                transition-show="scale"
                transition-hide="scale"                 
              />
            </q-item-section>
          </q-item>

          <q-item tag="label" >
            <q-item-section>
              <q-select 
                v-model="quality" 
                :options="qualities" 
                label="Quality checks" 
                stack-label 
                borderless 
                :dense="true" 
                :options-dense="true"
                :option-disable="opt => timeAggregation === 'none' && opt === 'empirical'"
                transition-show="scale"
                transition-hide="scale"                 
              />
            </q-item-section>
          </q-item> 

          <q-item tag="label" style="user-select: none;" :disable="timeAggregation === 'none'">
            <q-item-section avatar>
              <q-checkbox v-model="interpolation" color="teal" size="xs" :disable="timeAggregation === 'none'"/>
            </q-item-section>
            <q-item-section v-if="timeAggregation !== 'none'">
              <q-item-label>Interpolation</q-item-label>
            </q-item-section>
            <q-item-section v-else>
              <q-item-label>(Interpolation not available for raw data.)</q-item-label>
            </q-item-section>
          </q-item>

          <q-separator />

          <q-item>
            <timeseries-selector 
              :multiTimeseries="multiTimeseries" 
              :timeAggregation="timeAggregation" 
              @plot-sensor-changed="selectedPlots = $event.plots; selectedSensors = $event.sensors;"
              ref="timeseriesSelector" 
            />            
          </q-item>

          <q-separator />
          <template v-if="plotSensorList.length > 0">
            Selected timeseries <span style="color: grey;">(max {{max_timeseries}})</span>
            <q-list dense separator>
              <q-item v-for="(plotSensor, i) in plotSensorList" :key="plotSensor.plot + '/' + plotSensor.sensor" clickable>
                <q-item-section>
                  <q-item-label>{{plotSensor.plot}}</q-item-label>
                  <q-item-label caption lines="2">{{plotSensor.sensor}}</q-item-label>
                </q-item-section>
                  <q-item-section side top color="yellow">
                  <q-item-label caption style="background-color: rgb(240,240,240);" :style="{color: $refs.timeseriesDiagram.timeseriesStrokes[i]}">[{{i + 1}}]</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </template>
                    
        </q-list>
      </q-scroll-area>
      <div v-else  class="fit">
        <div v-if="modelLoading">
          <q-spinner-ios color="primary" size="2em" />
          Loading metadata...
        </div>
        <div v-else-if="modelError">Error loading metadata. <q-btn @click="$store.dispatch('model/refresh')">try again</q-btn></div>
        <div v-else>Metadata not loaded.</div>
      </div>
      <div v-touch-pan.prevent.mouse="onChangeDrawerWidth" class="drawerChanger">
      </div>
      </div> 
    </q-drawer>

    <q-page-container>
      <div style="position: relative;">
      <div v-if="dataRequestSentCounter > dataRequestReceivedCounter" style="position: absolute; top: 70px; left: 50px;">        
        <q-item>
          <q-item-section avatar ><q-icon name="error_outline" color="blue-14"/></q-item-section>
          <q-item-section>
            <q-spinner-ios color="primary" size="2em" />
            Requesting data from server ...
          </q-item-section>
        </q-item>
      </div>
      <div v-if="dataRequestError !== undefined" style="position: absolute; top: 100px; left: 100px;">
        <q-item>
          <q-item-section avatar ><q-icon name="error_outline" color="red-14"/></q-item-section>
          <q-item-section>{{dataRequestError}}</q-item-section>
        </q-item>
      </div>
      <div style="margin-top: 10px; margin-left: 10px;">
        <table>
        <tr><td style="padding-right: 10px; text-align:center"><b>Zoom in/out</b></td><td>Place mouse on diagram and rotate the mouse wheel.</td></tr>
        <tr><td style="padding-right: 10px; text-align:center"><b>Move in time</b></td><td>Place mouse on diagram, press and hold left mouse button and move mouse left / right on the diagram.</td></tr>
        <tr><td style="padding-right: 10px; text-align:center"><b>Inspect timeseries values</b></td><td>Move mouse over diagram without mouse buttons pressed to show time / measurement values.</td></tr>        
        </table>
      </div>
      <timeseries-diagram :data="data" :timeAggregation="timeAggregation" ref="timeseriesDiagram" />
      </div>
    </q-page-container>

  </q-layout>
</template>

<script>

import { mapState, mapGetters } from 'vuex';

import pagesToolbar from 'components/pages-toolbar.vue';
import timeseriesSelector from 'components/timeseries-selector.vue';
import timeseriesDiagram from 'components/timeseries-diagram.vue';

function convertFloat32ArrayToArray(a) {
  var r = [];
  for(var i = 0; i < a.length; i++) {
    var v = a[i];
    r[i] = Number.isFinite(v) ? v : null;
  }
  return r;
}

function convertInt32ArrayToArray(a) {
  var r = [];
  for(var i = 0; i < a.length; i++) {
    var t = a[i];
    r[i] = (t - 36819360 - 60) * 60;
  }
  return r;
}

export default {
  name: 'DiagramLayout',  
  components: {
    pagesToolbar,
    timeseriesSelector,
    timeseriesDiagram,
  },
  data() {
    return {
      drawerWidth: 400,
      data: undefined,
      dataRequestSentCounter: 0,
      dataRequestReceivedCounter: 0,
      dataRequestError: 'no data loaded',
      max_timeseries: 12,

      timeAggregation: 'hour',
      quality: 'step',
      interpolation: false,
      
      selectedPlots: [],
      selectedSensors: [],

      multiTimeseries: true,
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
      apiPOST: 'apiPOST',
    }),
    qualities() {
      return ['none', 'physical', 'step', 'empirical'];
    },
    plotSensorList() {
      let list = [];
      for(let selectedSensor of this.selectedSensors) {
        for(let selectedPlot of this.selectedPlots) {
          if(selectedPlot.sensorSet.has(selectedSensor.id)) {
            let entry = {plot: selectedPlot.id, sensor: selectedSensor.id};
            list.push(entry);
            if(list.length >= this.max_timeseries) {
              return list;
            }
          }
        }
      }
      return list;
    },
  },
  methods: {
    onChangeDrawerWidth(e) {
      var delta = e.delta.x;
      this.drawerWidth += delta;
      if(this.drawerWidth < 30) {
        this.drawerWidth = 30;
      }
      if(this.drawerWidth > 800) {
        this.drawerWidth = 800;
      }
    },
    settingsChanged() {
      this.requestData();
    },
    async requestData() {
      if(this.model !== undefined) {
        if(this.plotSensorList.length < 1) {
          this.data = undefined;
          return;
        }
        try {
          this.dataRequestSentCounter++;
          var dataRequestCurrentCounter = this.dataRequestSentCounter;
          this.dataRequestError = undefined;
          //console.time('apiPOST');
          var reqConfig = {
            responseType: 'arraybuffer',
          }
          var settings = {
            timeAggregation: this.timeAggregation,
            quality: this.quality,            
          };
          if(this.timeAggregation !== 'none') {
            settings.interpolation = this.interpolation;
          }
          if(this.$refs.timeseriesSelector.view_time_range_limit) {
            settings.view_time_limit_start = this.$refs.timeseriesSelector.view_time_range_limit[0];
            settings.view_time_limit_end = this.$refs.timeseriesSelector.view_time_range_limit[1];
          }
          var reqData = {
            settings: settings,
            timeseries: this.plotSensorList,
          };
          var response = await this.apiPOST(['tsdb', 'query_js'], reqData, reqConfig);
          if(dataRequestCurrentCounter < this.dataRequestSentCounter) {
            return;
          }
          this.dataRequestReceivedCounter = dataRequestCurrentCounter;
          //console.log(response);
          //console.timeEnd('apiPOST');
          //console.time('prepare');         
          var arrayBuffer = response.data;
          var dataView = new DataView(arrayBuffer);
          var entryCount = dataView.getInt32(0, true);
          var schemaCount = dataView.getInt32(4, true); 
          console.log("entryCount: " + entryCount + "   schemaCount: " + schemaCount);
          var data = [];
          var timestamps = new Int32Array(arrayBuffer, 4 + 4, entryCount);
          //console.log(timestamps);
          data[0] = convertInt32ArrayToArray(timestamps);
          for(var i = 0; i < schemaCount; i++) {
            var values = new Float32Array(arrayBuffer, 4 + 4 + 4 * entryCount * (i + 1), entryCount); 
            data[i + 1] = convertFloat32ArrayToArray(values);
          }
          //console.log(data);
          //console.timeEnd('prepare');
          this.data = data;
        } catch(e) {
          console.log(e);
          if(dataRequestCurrentCounter < this.dataRequestSentCounter) {
            return;
          }
          this.dataRequestError = "ERROR receiving data";
          this.dataRequestReceivedCounter = dataRequestCurrentCounter;
        }       
      }
    },
  },
  watch: {
    async model() {
      this.requestData();
    },
    timeAggregation: {
      handler() {
        if(this.quality === undefined || this.quality === null || (this.timeAggregation === 'none' && this.quality === 'empirical')) {
          this.quality = this.qualities[2];
        }
        this.settingsChanged();
      },
      immediate: true, 
    },
    quality() {
      this.settingsChanged();
    },    
    interpolation() {
      this.settingsChanged();
    },
    plotSensorList() {
      this.settingsChanged();
    },
  },
  async mounted() {
    this.requestData();

    this.$store.dispatch('model/init');
  },
}
</script>

<style scoped>

.drawerChanger {
  display: flex;
  align-items: center;
  cursor: col-resize;
  width: 6px;
  justify-content: space-between;
  background-color: rgba(0, 0, 0, 0.08);
  background: linear-gradient(to right, rgba(0, 0, 0, 0.06),rgba(255, 255, 255, 0.78),rgba(0, 0, 0, 0.24));
}

.drawerChanger::before, .drawerChanger::after {
  width: 1px;
  height: 60px;
  content: "";
}

.drawerChanger::before {
  background-color: #00000057;
  margin-left: 1px;
}

.drawerChanger::after {
  background-color: #0000008f;
  margin-right: 1px;
}

</style>
