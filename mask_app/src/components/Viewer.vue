<template>
  <div class="chart-container">
    {{ selectionStart }} - {{ selectionEnd }}
    <UplotVue :options="options" :data="data" v-if="data" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import UplotVue from 'uplot-vue'
import 'uplot/dist/uPlot.min.css'

const props = defineProps({
  plot: {
    type: String,
    required: true
  },
  sensor: {
    type: String,
    required: true
  },  
  width: {
    type: Number,
    required: true
  },
  data: {
    type: [Array, null],
    required: true,
  }
});


function wheelZoomPlugin(opts) {
  let factor = opts.factor || 0.75;

  let xMin, xMax, xRange;

  function clamp(nRange, nMin, nMax, fRange, fMin, fMax) {
    if (nRange > fRange) {
      nMin = fMin;
      nMax = fMax;
    }
    else if (nMin < fMin) {
      nMin = fMin;
      nMax = fMin + nRange;
    }
    else if (nMax > fMax) {
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
        xRange = xMax - xMin;

        let over = u.over;
        let rect = over.getBoundingClientRect();

        over.addEventListener("wheel", e => {
          e.preventDefault();

          let {left} = u.cursor;
          let leftPct = left / rect.width;
          
          let xVal = u.posToVal(left, "x");
          let oxRange = u.scales.x.max - u.scales.x.min;

          let nxRange = e.deltaY < 0 ? oxRange * factor : oxRange / factor;
          let nxMin = xVal - leftPct * nxRange;
          let nxMax = nxMin + nxRange;
          
          [nxMin, nxMax] = clamp(nxRange, nxMin, nxMax, xRange, xMin, xMax);

          u.batch(() => {
            u.setScale("x", {
              min: nxMin,
              max: nxMax,
            });
          });
        });
      }
    }
  };
}

function dragPlugin(opts) {

  let mouseDownXval;
  let mouseDownXmin;
  let mouseDownXmax;

  function ready(u) {
    let plot = u.root.querySelector(".u-over");

    const hasModifier = (e) => e.shiftKey || e.ctrlKey || e.altKey || e.metaKey;

    function initMove() {
      let xVal = u.posToVal(u.cursor.left, "x");
      mouseDownXval = xVal;
      mouseDownXmin = u.scales.x.min;
      mouseDownXmax = u.scales.x.max;
    }

    function finishMove() {
      mouseDownXval = undefined;
    }

    plot.addEventListener("mousedown", function(e) {
      if (hasModifier(e)) return;
      initMove();
    });

    plot.addEventListener("mouseup", function(e) {
      finishMove();
    });

    plot.addEventListener("mousemove", function(e) {
      if (hasModifier(e)) return;

      if(e.buttons === 1) {
        if(mouseDownXval === undefined) {
          initMove();
        } else {
          let xVal = u.posToVal(u.cursor.left, "x");
          let xDelta = mouseDownXval - xVal;
          let nxMin = mouseDownXmin + xDelta;
          let nxMax = mouseDownXmax + xDelta
          u.batch(() => {
            u.setScale("x", {
              min: nxMin,
              max: nxMax,
            });
          });
          mouseDownXmin = nxMin;
          mouseDownXmax = nxMax;
        }
      }
    });
  }
  return {hooks: {ready}};
}

const selectionStart = ref(null);
const selectionEnd = ref(null);

function handleSelectionPluginKeyDown(e) {
  if (e.key === 'Escape' || e.key === 'Esc') {
    selectionStart.value = null;
    selectionEnd.value = null;
  }
}

function selectionPlugin(opts) {

  function ready(u) {
    let plot = u.root.querySelector(".u-over");   

    plot.addEventListener("click", function(e) {
      if (!(e.shiftKey || e.ctrlKey || e.altKey)) return;
      if (e.button === 0) {
        e.preventDefault();
        e.stopPropagation();
        selectionStart.value = u.posToVal(u.cursor.left, "x");
      }    
    });

    plot.addEventListener("contextmenu", function(e) {
      if (!(e.shiftKey || e.ctrlKey || e.altKey)) return;
      e.preventDefault();
      e.stopPropagation();
      selectionEnd.value = u.posToVal(u.cursor.left, "x");
    });
  }
  return {hooks: {ready}};
}

const options = computed(() => ({
  width: props.width,
  height: 400,
  cursor: {
    x: false,
    y: false,
    drag: {
      x: false,
      y: false,
    },
  },
  plugins: [
    wheelZoomPlugin({factor: 0.75}),
    dragPlugin({}),
    selectionPlugin({})
  ],
  series: [
    {},
    {
      stroke: 'black',
      width: 1,
    }
  ]
}))


onMounted(() => {
  window.addEventListener('keydown', handleSelectionPluginKeyDown);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleSelectionPluginKeyDown);
});

</script>

<style scoped>
.chart-container {
  width: 100%;
}
</style>