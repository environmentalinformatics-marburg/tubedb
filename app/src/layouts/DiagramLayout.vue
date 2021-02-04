<template>
  <q-layout view="hHh LpR fFf">

    <q-header reveal elevated class="bg-grey-7 text-grey-4">
      <q-toolbar class="fit row wrap justify-center items-start content-start title">       
        <div class="text-h5">TubeDB Diagram</div>
       </q-toolbar>
    </q-header>

    <q-drawer show-if-above side="left" behavior="desktop" content-class="bg-grey-4" v-if="model !== undefined" :width="drawerWidth">    
      <div class="fit row">
      <q-scroll-area class="col-grow">  
        <q-list>

          <q-item tag="label" >
            <q-item-section>
              <q-select v-model="timeAggregation" :options="['none', 'hour', 'day', 'week', 'month', 'year']" label="Aggregation by time" stack-label borderless :dense="true" :options-dense="true"/>
            </q-item-section>
          </q-item>

          <q-item tag="label" >
            <q-item-section>
              <q-select v-model="quality" :options="['none', 'physical', 'step', 'empirical']" label="Quality checks" stack-label borderless :dense="true" :options-dense="true"/>
            </q-item-section>
          </q-item> 

          <q-item tag="label" style="user-select: none;" v-if="timeAggregation != 'none'">
            <q-item-section avatar>
              <q-checkbox v-model="interpolation" color="teal" size="xs"/>
            </q-item-section>
            <q-item-section>
              <q-item-label>Interpolation</q-item-label>
            </q-item-section>
          </q-item>

          <q-separator />

          <q-item>
            <timeseries-selector @plot-sensor-changed="selectedPlots = $event.plots; selectedSensors = $event.sensors;" />            
          </q-item>
                    
        </q-list>
      </q-scroll-area>
      <div v-touch-pan.prevent.mouse="onChangeDrawerWidth" class="drawerChanger">
      </div>
      </div> 
    </q-drawer>

    <q-page-container style="position: relative;">
      <div v-if="dataRequestSentCounter > dataRequestReceivedCounter" style="position: absolute;">
        Waiting for data ...
      </div>
      <div v-if="dataRequestError !== undefined" style="position: absolute;">
        {{dataRequestError}}
      </div>
      <div ref="diagram">        
        <q-resize-observer @resize="onChangeDiagramDimensions" debounce="250" />
      </div>
    </q-page-container>

  </q-layout>
</template>

<script>

import { mapState, mapGetters } from 'vuex';
import uPlot from 'uPlot';
import 'uPlot/dist/uPlot.min.css';

import timeseriesSelector from 'components/timeseries-selector.vue';

function convertFloat32ArrayToArray(a) {
  var r = [];
  for(var i = 0; i < a.length; i++) {
    var v = a[i];
    r[i] = Number.isFinite(v) ? v : null;
  }
  return r;
}

function wheelZoomPlugin(opts) {
  let factor = opts.factor || 0.75;

  let xMin, xMax, yMin, yMax, xRange, yRange;

  function clamp(nRange, nMin, nMax, fRange, fMin, fMax) {
    if (nRange > fRange) {
      nMin = fMin;
      nMax = fMax;
    } else if (nMin < fMin) {
      nMin = fMin;
      nMax = fMin + nRange;
    } else if (nMax > fMax) {
      nMax = fMax;
      nMin = fMax - nRange;
    }

    return [nMin, nMax];
  }

  return {
    hooks: {
      ready: u => {
        xMin = u.scales.x.min;
        xMax = u.scales.x.max;
        yMin = u.scales.y.min;
        yMax = u.scales.y.max;

        xRange = xMax - xMin;
        yRange = yMax - yMin;

        let plot = u.root.querySelector(".u-over");
        let rect = plot.getBoundingClientRect();

        // wheel drag pan
        plot.addEventListener("mousedown", e => {
          if (e.button === 1) {
          //plot.style.cursor = "move";
            e.preventDefault();

            let left0 = e.clientX;
            //let top0 = e.clientY;

            let scXMin0 = u.scales.x.min;
            let scXMax0 = u.scales.x.max;

            let xUnitsPerPx = u.posToVal(1, 'x') - u.posToVal(0, 'x');

            function onmove(e) {
              e.preventDefault();

              let left1 = e.clientX;
              //let top1 = e.clientY;

              let dx = xUnitsPerPx * (left1 - left0);

              u.setScale('x', {
                min: scXMin0 - dx,
                max: scXMax0 - dx,
              });
            }

            function onup(e) {
              document.removeEventListener("mousemove", onmove);
              document.removeEventListener("mouseup", onup);
            }

            document.addEventListener("mousemove", onmove);
            document.addEventListener("mouseup", onup);
          }
        });

        // wheel scroll zoom
        plot.addEventListener("wheel", e => {
          e.preventDefault();

          let {left, top} = u.cursor;

          let leftPct = left / rect.width;
          let btmPct = 1 - top / rect.height;
          let xVal = u.posToVal(left, "x");
          let yVal = u.posToVal(top, "y");
          let oxRange = u.scales.x.max - u.scales.x.min;
          let oyRange = u.scales.y.max - u.scales.y.min;

          let nxRange = e.deltaY < 0 ? oxRange * factor : oxRange / factor;
          let nxMin = xVal - leftPct * nxRange;
          let nxMax = nxMin + nxRange;
          [nxMin, nxMax] = clamp(nxRange, nxMin, nxMax, xRange, xMin, xMax);

          let nyRange = e.deltaY < 0 ? oyRange * factor : oyRange / factor;
          let nyMin = yVal - btmPct * nyRange;
          let nyMax = nyMin + nyRange;
          [nyMin, nyMax] = clamp(nyRange, nyMin, nyMax, yRange, yMin, yMax);

          u.batch(() => {
            u.setScale("x", {
              min: nxMin,
              max: nxMax,
            });

            /*u.setScale("y", {
              min: nyMin,
              max: nyMax,
            });*/
          });
        });
      }
    }
  };
}

export default {
  components: {
    timeseriesSelector,
  },
  data () {
    return {
      drawerWidth: 400,
      data: undefined,
      uplot: undefined,
      dataRequestSentCounter: 0,
      dataRequestReceivedCounter: 0,
      dataRequestError: 'init',

      timeAggregation: 'hour',
      quality: 'step',
      interpolation: false,
      
      selectedPlots: [],
      selectedSensors: [],
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
    plotSensorList() {
      let list = [];
      for(let selectedSensor of this.selectedSensors) {
        for(let selectedPlot of this.selectedPlots) {
          if(selectedPlot.sensorSet.has(selectedSensor.id)) {
            let entry = {plot: selectedPlot.id, sensor: selectedSensor.id};
            list.push(entry);
            if(list.length > 4) {
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
    onChangeDiagramDimensions() {
      if(this.uplot !== undefined && this.$refs.diagram !== undefined) {
        var width = this.$refs.diagram.clientWidth;
        //var height = this.$refs.diagram.clientHeight;
        var height = 400;
        this.uplot.setSize({ width: width, height: height });
      }
    },
    createDiagram() {
      if(this.uplot !== undefined) {
        this.uplot.destroy();
        this.uplot = undefined;
      }
      if(this.data === undefined) { 
        return;   
      }

      var width = this.$refs.diagram.clientWidth;
      //var height = this.$refs.diagram.clientHeight;
      var height = 400;

      console.log(width + " x "  + height);

      let opts = {
        width: width,
        height: height,
        plugins: [
          wheelZoomPlugin({factor: 0.75})
        ],        
        series: [
          {},
          {
            // initial toggled state (optional)
            show: true,

            spanGaps: false,

            // in-legend display
            label: "Value",
            value: (self, rawValue) => rawValue === null ? '---' : rawValue.toFixed(2),

            // series style
            stroke: "red",
            width: 1,
            //fill: "rgba(255, 0, 0, 0.3)",
            //dash: [10, 5],
          }
        ],
      };

      this.$refs.diagram.innderHTML = '';
      this.uplot = new uPlot(opts, this.data, this.$refs.diagram);

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
          console.time('apiPOST');
          var reqConfig = {
            responseType: 'arraybuffer',
          }
          var reqData = {
            settings: {
              timeAggregation: this.timeAggregation,
              quality: this.quality,
              interpolation: this.interpolation,
            },
            timeseries: this.plotSensorList,
          };
          var response = await this.apiPOST(['tsdb', 'query_js'], reqData, reqConfig);
          if(dataRequestCurrentCounter < this.dataRequestSentCounter) {
            return;
          }
          this.dataRequestReceivedCounter = dataRequestCurrentCounter;
          console.log(response);
          console.timeEnd('apiPOST');
          console.time('prepare');         
          var arrayBuffer = response.data;
          var dataView = new DataView(arrayBuffer);
          var entryCount = dataView.getInt32(0, true);
          var schemaCount = dataView.getInt32(4, true); 
          console.log(entryCount);
          console.log(schemaCount);
          var data = [];
          var timestamps = new Int32Array(arrayBuffer, 4 + 4, entryCount);
          console.log(timestamps);
          data[0] = timestamps.map(t => (t - 36819360 - 60) * 60);
          for(var i = 0; i < schemaCount; i++) {
            var values = new Float32Array(arrayBuffer, 4 + 4 + 4 * entryCount * (i + 1), entryCount); 
            data[i + 1] = convertFloat32ArrayToArray(values);
          }
          //console.log(data);
          console.timeEnd('prepare');
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
    data() {
      this.createDiagram();    
    },
    timeAggregation() {
      this.settingsChanged();
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

.title {
  background: radial-gradient( circle farthest-corner at center center, #757575, #a4a2a2 ) no-repeat;
  color: #e0e0e0b5 !important;
  text-shadow: 0 0 5px #8c8c8c, 0 0 10px #4a4a4a, 0 0 20px #ffffff1f, 0 0 30px #fff3, 0 0 40px #ffffff38, 0 0 55px #ffffff42, 0 0 70px #fff3;
  transition: 5s;
  user-select: none;
}

.title:hover {
  background: radial-gradient( circle farthest-corner at center center, #757575, #a4a2a2 ) no-repeat;
  text-shadow: 0 0 5px #000, 0 0 10px #000, 0 0 20px #ffffff8a, 0 0 30px rgba(255, 255, 255, 0.514), 0 0 40px #ffffff91, 0 0 55px #ffffff8a, 0 0 70px rgba(255, 255, 255, 0.582);
  user-select: none;
}

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
