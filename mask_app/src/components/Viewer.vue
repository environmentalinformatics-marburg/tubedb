<template>
  <div class="chart-container">
    <UplotVue :options="options_cmp" :data="data_cmp" v-if="data_cmp" ref="uplotDiagram_cmp" style="background-color: rgba(200,200,255,0.04);" />
    <UplotVue :options="options" :data="data" v-if="data" ref="uplotDiagram" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, defineEmits, toRaw } from 'vue'
import UplotVue from 'uplot-vue'
import 'uplot/dist/uPlot.min.css'
import uPlot from 'uplot';

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
  height: {
    type: Number,
    required: true
  },  
  data: {
    type: [Array, null],
    required: true,
  },
  data_cmp: {
    type: [Array, null],
    required: true,
  },
  mask: {
    type: [Object, null],
    required: true,
  }
});

const emit = defineEmits(['selection-change']);

const uplotDiagram_cmp = ref(null); // ref to UplotVue instance
const uplotDiagram = ref(null); // ref to UplotVue instance

function setScale(nxMin, nxMax) {
  if (uplotDiagram_cmp.value && uplotDiagram_cmp.value._chart) {
    const u = uplotDiagram_cmp.value._chart;
    u.batch(() => {
      u.setScale("x", {
        min: nxMin,
        max: nxMax,
      });            
    });
  }
  if (uplotDiagram.value && uplotDiagram.value._chart) {
    const u = uplotDiagram.value._chart;
    u.batch(() => {
      u.setScale("x", {
        min: nxMin,
        max: nxMax,
      });            
    });
  }
}


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

        //xMin = u.scales.x.min;
        //xMax = u.scales.x.max;
        xMin = props.data[0][0];
        xMax = props.data[0][props.data[0].length-1];
        setScale(xMin, xMax);
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

          /*u.batch(() => {
            u.setScale("x", {
              min: nxMin,
              max: nxMax,
            });
          });*/
          setScale(nxMin, nxMax);
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
          /*u.batch(() => {
            u.setScale("x", {
              min: nxMin,
              max: nxMax,
            });            
          });*/
          setScale(nxMin, nxMax);
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

const selectionMin = computed(() => {
  if (selectionStart.value == null) {
    return null;
  }
  if(selectionEnd.value === null) {
    return selectionStart.value;
  }
  return Math.min(selectionStart.value, selectionEnd.value);
});

const selectionMax = computed(() => {
  if (selectionEnd.value == null) {
    return null;
  }
  if(selectionStart.value === null) {
    return selectionEnd.value;
  }
  return Math.max(selectionStart.value, selectionEnd.value);
});


function formatTimestamp(timestamp) {
  if (timestamp === null) {
    return '*';
  }  

  const date = new Date(timestamp * 1000);
  
  const pad = (n) => n.toString().padStart(2, '0');
  
  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1);
  const day = pad(date.getDate());
  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());
  
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

watch([selectionMin, selectionMax], () => {
  if (uplotDiagram.value && uplotDiagram.value._chart) {
    uplotDiagram.value._chart.redraw();
  }
  emit('selection-change', { 
    min: selectionMin.value, 
    max: selectionMax.value,
    dateMin: formatTimestamp(selectionMin.value),
    dateMax: formatTimestamp(selectionMax.value),
  });
});

watch(() => props.mask, () => {
  if (uplotDiagram.value && uplotDiagram.value._chart) {
    uplotDiagram.value._chart.redraw();
  }
});

function handleSelectionPluginKeyDown(e) {
  if (e.key === 'Escape' || e.key === 'Esc') {
    resetSelection();
  }
}

const resetSelection = () => {
  selectionStart.value = null;
  selectionEnd.value = null;
}

defineExpose({
  resetSelection
});

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

function markPlugin(opts) {

  function draw(u) {
        const start = selectionMin.value;
        const end = selectionMax.value;

        if (start === null && end === null) {
          return;
        }

        const ctx = u.ctx;
        const bbox = u.bbox;
        
        let x1, x2;

        if (start === null) {
          x1 = bbox.left;
        } else {
          x1 = u.valToPos(start, 'x', true);
        }

        if (end === null) {
          x2 = bbox.left + bbox.width;
        } else {
          x2 = u.valToPos(end, 'x', true);
        }

        if (x1 === null || x2 === null) return;

        ctx.save();
        ctx.fillStyle = 'rgba(66, 133, 244, 0.3)';
        ctx.fillRect(x1, bbox.top, x2 - x1, bbox.height);
        ctx.restore();
  }

  return {hooks: {draw}};
}

function maskPlugin(opts) {

  function draw(u) {
        if(props.mask === null) {
          return;
        }

        const ctx = u.ctx;
        const bbox = u.bbox;
        ctx.save();

        for (const interval of props.mask.suspect_mask) {
          const start = (interval[0] - 36819360 - 60) * 60;
          const end = (interval[1] - 36819360 - 60) * 60;
          if (start === null && end === null) {
            return;
          }

          let x1, x2;

          if (start === null) {
            x1 = bbox.left;
          } else {
            x1 = u.valToPos(start, 'x', true);
          }

          if (end === null) {
            x2 = bbox.left + bbox.width;
          } else {
            x2 = u.valToPos(end, 'x', true);
          }

          if (x1 === null || x2 === null) return;

          ctx.fillStyle = 'rgba(222, 222, 66, 0.3)';
          ctx.fillRect(x1, bbox.top, x2 - x1, bbox.height);
        }

        for (const interval of props.mask.mask) {
          const start = (interval[0] - 36819360 - 60) * 60;
          const end = (interval[1] - 36819360 - 60) * 60;
          if (start === null && end === null) {
            return;
          }

          let x1, x2;

          if (start === null) {
            x1 = bbox.left;
          } else {
            x1 = u.valToPos(start, 'x', true);
          }

          if (end === null) {
            x2 = bbox.left + bbox.width;
          } else {
            x2 = u.valToPos(end, 'x', true);
          }

          if (x1 === null || x2 === null) return;

          ctx.fillStyle = 'rgba(222, 66, 66, 0.3)';
          ctx.fillRect(x1, bbox.top, x2 - x1, bbox.height);          
        }

        ctx.restore();
  }

  return {hooks: {draw}};
}

const options_cmp = computed(() => ({
  width: props.width,
  height: props.height / 2,
  padding: [0, 0, 0, 0],
  bg: '#f0f4f8',
  legend: {
    show: false
  },
  cursor: {
    x: true,
    y: false,
    drag: {
      x: false,
      y: false,
    },
    points: {
      show: false
    },
    sync: {
      key: '_',
    } ,  
  },
  axes: [
    {
      size: 0,
      grid: {
        stroke: "#f7f7f7",
      },
      ticks: {
        show: false,
      }
    },
    {
      grid: {
        stroke: "#f7f7f7",
      },
    },
  ],
  plugins: [
    wheelZoomPlugin({factor: 0.75}),
    dragPlugin({}),
    selectionPlugin({}),
  ],
  series: [
    {},
    {
      stroke: 'grey',
      width: 1,
      fill: "rgba(150,150,150,0.15)",
    }
  ]
}))

const options = computed(() => ({
  width: props.width,
  height: uplotDiagram_cmp.value ? props.height - (props.height / 2) : props.height,
  padding: [0, 0, 0, 0],
  legend: {
    show: false
  },
  cursor: {
    x: true,
    y: false,
    drag: {
      x: false,
      y: false,
    },
    points: {
      show: false
    },
    sync: {
      key: '_',
    } ,     
  },
  plugins: [
    wheelZoomPlugin({factor: 0.75}),
    dragPlugin({}),
    selectionPlugin({}),
    markPlugin({}),
    maskPlugin({}),
  ],
  series: [
    {},
    {
      stroke: 'black',
      width: 1,
      fill: "rgba(150,150,150,0.15)",
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