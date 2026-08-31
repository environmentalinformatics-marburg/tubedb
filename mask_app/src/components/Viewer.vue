<template>
  <div class="chart-container">
    <UplotVue :options="options" :data="data" v-if="data" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
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
    type: Array,
    required: true,
  }
})

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
  series: [
    {},
    {
      stroke: 'black',
      width: 1,
    }
  ]
}))

</script>

<style scoped>
.chart-container {
  width: 100%;
}

</style>