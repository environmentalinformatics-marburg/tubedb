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
        
      </q-scroll-area>
      <div v-touch-pan.prevent.mouse="onChangeDrawerWidth" class="drawerChanger">
      </div>
      </div> 
    </q-drawer>

    <q-page-container>
      <div ref="diagram"></div>
    </q-page-container>



  </q-layout>
</template>

<script>

import { mapState, mapGetters } from 'vuex';
import * as d3 from 'd3';
import uPlot from 'uPlot';
import 'uPlot/dist/uPlot.min.css';

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
  data () {
    return {
      drawerWidth: 400,
      data: undefined,
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
    createDiagram() {
      if(this.data === undefined) { 
        return;   
      }

      let opts = {
        title: "My Chart",
        id: "chart1",
        class: "my-chart",
        width: 800,
        height: 600,
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
            value: (self, rawValue) => rawValue,

            // series style
            stroke: "red",
            width: 1,
            //fill: "rgba(255, 0, 0, 0.3)",
            //dash: [10, 5],
          }
        ],
      };

      this.$refs.diagram.innderHTML = '';
      new uPlot(opts, this.data, this.$refs.diagram);

    },
  },
  watch: {
    async model() {
      if(this.model !== undefined) {
        try {
          var params = {
            plot: 'AEG01', 
            sensor: 'Ta_200', 
            aggregation: 'hour', 
            interpolated: false, 
            quality: 'step', 
            width: 1890, 
            height: 100, 
            by_year: true, 
            connection: 'step', 
            raw_connection: 'curve', 
            value: 'line', 
            raw_value: 'point', 
            //year: 2019, 
            datetime_format: 'timestamp',
          };
          console.time('apiGET');
          var data = await this.apiGET(['tsdb', 'query_csv'], { params: params });
          console.timeEnd('apiGET');
          console.time('csvParseRows');
          var d = d3.csvParseRows(data.data);
          console.timeEnd('csvParseRows');
          console.time('prepare');
          d.shift();
          var col0 = d.map(row => (row[0] - 36819360 - 60) * 60);
          var col1 = d.map(row => row[1] === 'NA' ? null : Number.parseFloat(row[1]));
          this.data = [col0, col1];
          console.timeEnd('prepare');
        } catch(e) {
          console.log(e);
        }
      }
    },
    data() {
      this.createDiagram();    
    },
  },
  async mounted() {
    this.$store.dispatch('model/init');
    this.createDiagram();
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
