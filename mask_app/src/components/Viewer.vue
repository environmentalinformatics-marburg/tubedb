<template>
<h3>{{plot}}</h3>
</template>

<script setup>
import { ref, } from 'vue'

const props = defineProps({
  plot: {
    type: String,
    required: true
  }
})


</script>

<style scoped>

</style>