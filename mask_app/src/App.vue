<template>

    <Shell v-if="metaData" :metaData="metaData" />
    <div v-else-if="metaDataLoading">Loading metadata...</div>    
    <div v-else style="color: red;">Error loading metadata: {{ metaDataError }}</div>

</template>

<script setup>
import { ref, onMounted } from 'vue'
import Shell from './components/Shell.vue'

const metaData = ref(null)
const metaDataLoading = ref(false)
const metaDataError = ref(null)

onMounted(async () => {
  metaDataLoading.value = true
  try {
    const response = await fetch('/tsdb/model')    
    if (!response.ok) {
      throw new Error(`HTTP error: ${response.status}`)
    }    
    metaData.value = await response.json()
  } catch (err) {
    metaDataError.value = err.message
  } finally {
    metaDataLoading.value = false
  }
})
</script>