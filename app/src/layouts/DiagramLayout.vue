<template>
  <q-layout view="hHh LpR fFf">

    <q-header reveal elevated class="bg-grey-7 text-grey-4">
      <pages-toolbar title="TubeDB Diagram" active="/diagram"/>
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
            <timeseries-selector :multiTimeseries="multiTimeseries" @plot-sensor-changed="selectedPlots = $event.plots; selectedSensors = $event.sensors;" />            
          </q-item>

          <q-separator />
          <template v-if="plotSensorList.length > 0">
            Selected timeseries:
          <q-item v-for="plotSensor in plotSensorList" :key="plotSensor">
            {{plotSensor.plot}} / {{plotSensor.sensor}}
          </q-item>
          </template>
                    
        </q-list>
      </q-scroll-area>
      <div v-touch-pan.prevent.mouse="onChangeDrawerWidth" class="drawerChanger">
      </div>
      </div> 
    </q-drawer>

    <q-page-container>
      <div style="position: relative;">
      <div v-if="dataRequestSentCounter > dataRequestReceivedCounter" style="position: absolute; top: 50px; left: 50px;">
        Waiting for data ...
      </div>
      <div v-if="dataRequestError !== undefined" style="position: absolute; top: 100px; left: 100px;">
        {{dataRequestError}}
      </div>
      <div style="margin-top: 10px; margin-left: 10px;">
        <b>Inspect timeseries values</b>: Move mouse over diagram to show time / measurement values.
        <br><b>Zoom in/out</b>: Place mouse on diagram and rotate the mouse wheel.
      </div>
      <div ref="diagram">        
        <q-resize-observer @resize="onChangeDiagramDimensions" debounce="250" />
      </div>
      </div>
    </q-page-container>

  </q-layout>
</template>

<script>

import { mapState, mapGetters } from 'vuex';
import uPlot from 'uPlot';
import 'uPlot/dist/uPlot.min.css';

import pagesToolbar from 'components/pages-toolbar.vue';
import timeseriesSelector from 'components/timeseries-selector.vue';

function convertFloat32ArrayToArray(a) {
  var r = [];
  for(var i = 0; i < a.length; i++) {
    var v = a[i];
    r[i] = Number.isFinite(v) ? v : null;
  }
  return r;
}

function touchZoomPlugin(opts) {
  function init(u, opts, data) {
    let plot = u.root.querySelector(".u-over");
    let rect, oxRange, oyRange, xVal, yVal;
    let fr = {x: 0, y: 0, dx: 0, dy: 0};
    let to = {x: 0, y: 0, dx: 0, dy: 0};

    function storePos(t, e) {
      let ts = e.touches;

      let t0 = ts[0];
      let t0x = t0.clientX - rect.left;
      let t0y = t0.clientY - rect.top;

      if (ts.length === 1) {
        t.x = t0x;
        t.y = t0y;
        t.d = t.dx = t.dy = 1;
      } else {
        let t1 = e.touches[1];
        let t1x = t1.clientX - rect.left;
        let t1y = t1.clientY - rect.top;

        let xMin = Math.min(t0x, t1x);
        let yMin = Math.min(t0y, t1y);
        let xMax = Math.max(t0x, t1x);
        let yMax = Math.max(t0y, t1y);

        // midpts
        t.y = (yMin + yMax) / 2;
        t.x = (xMin + xMax) / 2;

        t.dx = xMax - xMin;
        t.dy = yMax - yMin;

        // dist
        t.d = Math.sqrt(t.dx * t.dx + t.dy * t.dy);
      }
    }

    let rafPending = false;

    function zoom() {
      rafPending = false;

      let left = to.x;
      let top = to.y;

      // non-uniform scaling
      //let xFactor = fr.dx / to.dx;
      //let yFactor = fr.dy / to.dy;

      // uniform x/y scaling
      let xFactor = fr.d / to.d;
      let yFactor = fr.d / to.d;

      let leftPct = left / rect.width;
      let btmPct = 1 - top / rect.height;

      let nxRange = oxRange * xFactor;
      let nxMin = xVal - leftPct * nxRange;
      let nxMax = nxMin + nxRange;

      let nyRange = oyRange * yFactor;
      let nyMin = yVal - btmPct * nyRange;
      let nyMax = nyMin + nyRange;

      u.batch(() => {
        u.setScale("x", {
          min: nxMin,
          max: nxMax,
        });

        u.setScale("y", {
          min: nyMin,
          max: nyMax,
        });
      });
    }

    function touchmove(e) {
      storePos(to, e);

      if (!rafPending) {
        rafPending = true;
        requestAnimationFrame(zoom);
      }
    }

    plot.addEventListener("touchstart", function(e) {
      rect = plot.getBoundingClientRect();

      storePos(fr, e);

      oxRange = u.scales.x.max - u.scales.x.min;
      oyRange = u.scales.y.max - u.scales.y.min;

      let left = fr.x;
      let top = fr.y;

      xVal = u.posToVal(left, "x");
      yVal = u.posToVal(top, "y");

      document.addEventListener("touchmove", touchmove, {passive: true});
    });

    plot.addEventListener("touchend", function(e) {
      document.removeEventListener("touchmove", touchmove, {passive: true});
    });


    plot.addEventListener("mousedown", function(e) {
      console.log("mousedown");
      rect = plot.getBoundingClientRect();

      storePos(fr, e);

      oxRange = u.scales.x.max - u.scales.x.min;
      oyRange = u.scales.y.max - u.scales.y.min;

      let left = fr.x;
      let top = fr.y;

      xVal = u.posToVal(left, "x");
      yVal = u.posToVal(top, "y");

      document.addEventListener("mousemove", touchmove, {passive: true});
    });

    plot.addEventListener("mouseup", function(e) {
      console.log("mouseup");
      document.removeEventListener("mousemove", touchmove, {passive: true});
    });
  }  

  return {
    hooks: {
      init
    }
  };
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
    pagesToolbar,
    timeseriesSelector,
  },
  data() {
    return {
      drawerWidth: 400,
      data: undefined,
      uplot: undefined,
      dataRequestSentCounter: 0,
      dataRequestReceivedCounter: 0,
      dataRequestError: 'no data loaded',

      timeAggregation: 'hour',
      quality: 'step',
      interpolation: false,
      
      selectedPlots: [],
      selectedSensors: [],

      multiTimeseries: false,
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
        cursor: {
          x: false,
          y: false,
          drag: {
            x: false,
            y: false,
          },          
        },
        plugins: [
          touchZoomPlugin({}),
          wheelZoomPlugin({factor: 0.75}),
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
