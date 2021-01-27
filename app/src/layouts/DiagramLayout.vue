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
      <canvas style="border-style: solid;">
      </canvas>
    </q-page-container>



  </q-layout>
</template>

<script>

import { mapState, mapGetters } from 'vuex';
import * as d3 from 'd3';

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
    dataXrange() {
      if(this.data === undefined) {
        return [0, 1];
      }
      return [0, this.data.length - 1];     
    },
    dataYrange() {
      if(this.data === undefined) {
        return [0, 1];
      }
      var min = Number.POSITIVE_INFINITY;
      var max = Number.NEGATIVE_INFINITY;
      this.data.forEach(function(r) {
        var v = r[1];
        if (max < v) {
          max = v;
        }
        if (min > v) {
          min = v;
        }
      });
      return [min, max];      
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
    createDiagram() {
      const Width = 1280;
      const Height = 400;
      const margin = {top: 0, right: 0, bottom: 0, left: 0};
      const width = Width - margin.right - margin.left;
      const height = Height - margin.top - margin.bottom;

      const xScale = d3.scaleLinear().range([0, width]);
      const yScale = d3.scaleLinear().range([height, 0]);
      const xAxis = d3.axisBottom(xScale);
      const yAxis = d3.axisLeft(yScale);

      const canvas = d3.select('canvas').attr('width', Width).attr('height', Height);
      const context = canvas.node().getContext('2d', { alpha: false });
      context.webkitImageSmoothingEnabled = false;
      context.mozImageSmoothingEnabled = false;
      context.imageSmoothingEnabled = false;
      context.translate(margin.left, margin.top);

      const line = d3.line()
        .x((r, i) => ~~xScale(i))
        .y(r => ~~yScale(r[1]))
        .context(context);

      const data = this.data === undefined ? [] : this.data;
      xScale.domain(this.dataXrange);
      yScale.domain(this.dataYrange);

      //context.clearRect(0, 0, width, height);
      context.fillStyle = "white";
      context.fillRect(0, 0, width, height);
      context.beginPath();
      line(data);
      context.lineWidth = 1;
      context.opacity = 1;
      context.strokeStyle = 'black';
      context.stroke();
      context.closePath();
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
            year: 2020, 
          };
          var data = await this.apiGET(['tsdb', 'query_csv'], { params: params });
          var d = d3.csvParseRows(data.data);
          d.shift();
          d = d.map(r => [r[0], Number.parseFloat(r[1])]);
          this.data = d;
        } catch(e) {
          console.log(e);
        }
      }
    },
    data() {
      //var q = d3.csvParse(this.data);
      //var q = d3.csvParseRows(this.data);
      //console.log(q);  
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

canvas {
  image-rendering: crisp-edges;
  image-rendering: -moz-crisp-edges;
  image-rendering: -webkit-optimize-contrast;
  -ms-interpolation-mode: nearest-neighbor;
}

</style>
