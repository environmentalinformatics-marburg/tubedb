<template>
    <Shell v-if="metaData" :metaData="metaData" />
    <div v-else-if="metaDataLoading">Loading metadata...</div>    
    <div v-else class="error-message">
        <h3>⚠️ Unable to Load Metadata</h3>
        <p>{{ metaDataError }}</p>
        <p class="error-hint">Please try refreshing the page. If the problem persists, contact support.</p>
        <button @click="retryLoad">Try Again</button>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

import Shell from '@/components/Shell.vue'
import { getFriendlyErrorMessage } from '@/utils/errorMessages'

const metaData = ref(null)
const metaDataLoading = ref(false)
const metaDataError = ref(null)

const loadMetaData = async () => {
    metaDataLoading.value = true
    metaDataError.value = null
    
    try {
        const response = await fetch('/tsdb/model')    
        if (!response.ok) {
            throw new Error(getFriendlyErrorMessage(response.status))
        }    
        metaData.value = await response.json()
    } catch (err) {
        if (err.name === 'TypeError') {
            metaDataError.value = 'Unable to connect to the server. Please check your internet connection.'
        } else {
            metaDataError.value = err.message
        }
    } finally {
        metaDataLoading.value = false
    }
}

const retryLoad = () => {
    loadMetaData()
}

onMounted(() => {
    loadMetaData()
})
</script>

<style scoped>
.error-message {
    padding: 20px;
    border: 1px solid #ffcccc;
    border-radius: 8px;
    background-color: #fff5f5;
    color: #c00;
}

.error-message h3 {
    margin-top: 0;
}

.error-hint {
    font-size: 0.9em;
    color: #666;
}

button {
    padding: 8px 16px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

button:hover {
    background-color: #0056b3;
}
</style>