<template>
  <div class="layout-wrapper">
    <!-- Top Toolbar -->
    <div class="top-bar">
      <div class="toolbar-left">
        <!-- Project Select -->
        <span class="toolbar-label">Project:</span>
        <el-select
          v-model="selectedProject"
          style="width: 200px; margin-right: 8px;"
          @change="onProjectChange"
          placeholder="Projekt wählen"
        >
          <el-option
            v-for="project in projectOptions"
            :key="project.value"
            :label="project.label"
            :value="project.value"
          />
        </el-select>

        <!-- Group Select -->
        <span class="toolbar-label">Group:</span>
        <el-select
          v-model="selectedGroup"
          style="width: 200px; margin-right: 8px;"
          @change="onGroupChange"
          placeholder="Gruppe wählen"
          :disabled="!selectedProject"
        >
          <el-option
            v-for="group in groupOptions"
            :key="group.value"
            :label="group.label"
            :value="group.value"
          />
        </el-select>

        <!-- Plot Select -->
        <span class="toolbar-label">Plot:</span>
        <el-select
          v-model="selectedPlot"
          style="width: 200px; margin-right: 8px;"
          @change="onPlotChange"
          placeholder="Plot wählen"
          :disabled="!selectedGroup"
          filterable
          allow-create
          default-first-option
        >
          <el-option
            v-for="plot in plotOptions"
            :key="plot.value"
            :label="plot.label"
            :value="plot.value"
          />
        </el-select>

        <!-- Sensor Select -->
        <span class="toolbar-label">Sensor:</span>
        <el-select
          v-model="selectedSensor"
          style="width: 200px;"
          @change="onSensorChange"
          placeholder="Sensor wählen"
          :disabled="!selectedPlot"
          filterable
          allow-create
          default-first-option
        >
          <el-option
            v-for="sensor in sensorOptions"
            :key="sensor.value"
            :label="sensor.label"
            :value="sensor.value"
          />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button
          type="info"
          circle
          @click="showHelpDialog = true"
          icon="QuestionFilled"
          title="Hilfe / Bedienung"
        />
      </div>
    </div>

    <!-- Help Dialog Component -->
    <HelpDialog v-model="showHelpDialog" />

    <!-- Main Content -->
    <div ref="mainContent" class="main-content">
      <div v-if="dataLoading" class="loading-overlay">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>Loading data...</span>
      </div>

      <div v-else-if="dataError" class="error-overlay">
        <el-icon><Warning /></el-icon>
        <span>{{ dataError }}</span>
        <el-button type="primary" size="small" @click="fetchData">
          Retry
        </el-button>
      </div>

      <Viewer 
        v-if="selectedPlot && !dataLoading && data" 
        :plot="selectedPlot" 
        :sensor="selectedSensor"
        :width="mainContentWidth"
        :height="mainContentHeight"
        :data="data"
        :mask="mask"
        @selection-change="maskSelection = $event"
        ref="viewerRef" 
      />
    </div>

    <!-- Bottom Toolbar -->
    <div class="bottom-bar">
      <div class="toolbar-left">
        <span class="toolbar-label">Plot:</span>
        <span class="toolbar-value">{{ selectedPlot || '-' }}</span>
        <span class="toolbar-divider">|</span>
        <span class="toolbar-label">Sensor:</span>
        <span class="toolbar-value">{{ selectedSensor || '-' }}</span>
        <span class="toolbar-divider">|</span>
        <span class="toolbar-label">Selection:</span>
        <span class="toolbar-value" style="min-width: 250px;">{{ maskSelection.dateMin }} - {{ maskSelection.dateMax }}</span>
      </div>
      <div class="toolbar-center">
        <el-input
          v-model="commentText"
          placeholder="Insert comment..."
          clearable
          size="small"
          prefix-icon="Comment"
        />
      </div>
      <div class="toolbar-right">
        <span class="toolbar-label">Mask</span>
        <el-select
          v-model="maskType"
          size="small"
          style="width: 80px;"
        >
          <el-option label="Invalid" value="invalid" />
          <el-option label="Suspect" value="suspect" />
        </el-select>

        <el-button
          type="primary"
          :disabled="maskSelectionSaving"
          :loading="maskSelectionSaving"
          @click="maskSelectionSave"
          icon="DocumentAdd"
        >
          Save
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

import Viewer from '@/components/Viewer.vue'
import HelpDialog from '@/components/HelpDialog.vue'
import { getFriendlyErrorMessage } from '@/utils/errorMessages'

const props = defineProps({
  metaData: {
    type: Object,
    required: true
  }
});

const selectedProject = ref('');
const selectedGroup = ref('');
const selectedPlot = ref('');
const selectedSensor = ref('');

// Help Dialog State
const showHelpDialog = ref(false);

const maskSelection = ref({min: null, max: null, dateMin: '*', dateMax: '*'});

const maskType = ref('invalid');

const selectedProjectData = computed(() => {
  if (!selectedProject.value || !props.metaData?.model?.projects) return null
  return props.metaData.model.projects[selectedProject.value]
});

const selectedGroupData = computed(() => {
  if (!selectedGroup.value || !props.metaData?.model?.groups) return null
  return props.metaData.model.groups[selectedGroup.value]
});

const selectedPlotData = computed(() => {
  if (!selectedPlot.value || !props.metaData?.model?.plots) return null
  return props.metaData.model.plots[selectedPlot.value]
});

const selectedSensorData = computed(() => {
  if (!selectedSensor.value || !selectedPlotData.value) return null
  return selectedPlotData.value.sensors?.includes(selectedSensor.value) 
    ? selectedSensor.value 
    : null
});

const projectOptions = computed(() => {
  if (!props.metaData?.model?.projects) return []
  return Object.values(props.metaData.model.projects).map(p => ({
    value: p.id,
    label: p.title
  }));
});

const groupOptions = computed(() => {
  if (!selectedProject.value || !props.metaData?.model?.projects) return []
  
  const project = props.metaData.model.projects[selectedProject.value]
  if (!project?.groups) return []
  
  return project.groups.map(groupId => {
    const groupData = props.metaData.model.groups[groupId]
    return {
      value: groupId,
      label: groupData?.title || groupId
    }
  });
});

const plotOptions = computed(() => {
  if (!selectedGroup.value || !props.metaData?.model?.groups) return []
  
  const group = props.metaData.model.groups[selectedGroup.value]
  if (!group?.plots) return []
  
  return group.plots.map(plotId => ({
    value: plotId,
    label: plotId
  }));
});

const sensorOptions = computed(() => {
  if (!selectedPlot.value || !props.metaData?.model?.plots) return []
  
  const plot = props.metaData.model.plots[selectedPlot.value]
  if (!plot?.sensors) return []
  
  return plot.sensors.map(sensorId => ({
    value: sensorId,
    label: sensorId
  }));
});

watch(() => props.metaData, (newData) => {
  if (newData?.model?.projects) {
    const firstProject = Object.values(newData.model.projects)[0]
    if (firstProject) {
      selectedProject.value = firstProject.id
      
      if (firstProject.groups && firstProject.groups.length > 0) {
        selectedGroup.value = firstProject.groups[0]
        
        const firstGroup = newData.model.groups[firstProject.groups[0]]
        if (firstGroup?.plots && firstGroup.plots.length > 0) {
          selectedPlot.value = firstGroup.plots[0]
          
          const firstPlot = newData.model.plots[firstGroup.plots[0]]
          if (firstPlot?.sensors && firstPlot.sensors.length > 0) {
            selectedSensor.value = firstPlot.sensors[0]
          }
        }
      }
    }
  }
}, { immediate: true });

watch(selectedProject, (newProject) => {
  if (newProject && props.metaData?.model?.projects?.[newProject]?.groups) {
    const firstGroup = props.metaData.model.projects[newProject].groups[0]
    selectedGroup.value = firstGroup || ''
    selectedPlot.value = ''
    selectedSensor.value = ''
  } else {
    selectedGroup.value = ''
    selectedPlot.value = ''
    selectedSensor.value = ''
  }
});

watch(selectedGroup, (newGroup) => {
  if (newGroup && props.metaData?.model?.groups?.[newGroup]?.plots) {
    const firstPlot = props.metaData.model.groups[newGroup].plots[0]
    selectedPlot.value = firstPlot || ''
    selectedSensor.value = ''
  } else {
    selectedPlot.value = ''
    selectedSensor.value = ''
  }
});

watch(selectedPlot, (newPlot) => {
  if (newPlot && props.metaData?.model?.plots?.[newPlot]?.sensors) {
    const firstSensor = props.metaData.model.plots[newPlot].sensors[0]
    selectedSensor.value = firstSensor || ''
  } else {
    selectedSensor.value = ''
  }
});

const onProjectChange = (value) => {
  console.log('selected project:', value)
}

const onGroupChange = (value) => {
  console.log('selected group:', value)
}

const onPlotChange = (value) => {
  console.log('selected plot:', value)
}

const onSensorChange = (value) => {
  console.log('selected sensor:', value)
}

const viewerRef = ref(null); // Ref zu Viewer

const mainContent = ref(null); // ref to div mainContent
const mainContentWidth = ref(300);
const mainContentHeight = ref(300);
let resizeObserver = null;

onMounted(() => {
  mainContentWidth.value = mainContent.value.clientWidth;
  mainContentHeight.value = mainContent.value.clientHeight;
  resizeObserver = new ResizeObserver(entries => {
    mainContentWidth.value = mainContent.value.clientWidth;
    mainContentHeight.value = mainContent.value.clientHeight;
  });  
  resizeObserver.observe(mainContent.value);

  if (selectedPlot.value && selectedSensor.value) {
    fetchData();
  }
});

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect();
  }
});

const data = ref(null);
const dataLoading = ref(false);
const dataError = ref(null);

function convertFloat32ArrayToArray(a) {
  let r = [];
  for(let i = 0; i < a.length; i++) {
    let v = a[i];
    r[i] = Number.isFinite(v) ? v : null;
  }
  return r;
}

function convertInt32ArrayToArray(a) {
  let r = [];
  for(let i = 0; i < a.length; i++) {
    let t = a[i];
    r[i] = (t - 36819360 - 60) * 60;
  }
  return r;
}

const fetchData = async () => {
  if (!selectedPlot.value || !selectedSensor.value) {
    data.value = null;
    return;
  }

  data.value = null;
  dataLoading.value = true;
  dataError.value = null;

  try {
    const response = await fetch('/tsdb/query_js', {
      method: 'POST', 
      body: JSON.stringify({
      settings: {
        timeAggregation: 'hour',
        quality: 'step'
      },  
      timeseries: [{  
        plot: selectedPlot.value,
        sensor: selectedSensor.value,
      }]
      })
    })

    if (!response.ok) {
      throw new Error(getFriendlyErrorMessage(response.status))
    }

    const arrayBuffer = await response.arrayBuffer();
    console.log(arrayBuffer);
    const dataView = new DataView(arrayBuffer);

    let entryCount = dataView.getInt32(0, true);
    let schemaCount = dataView.getInt32(4, true);
    console.log("entryCount: " + entryCount + "   schemaCount: " + schemaCount);
    let dataArray = [];
    let timestamps = new Int32Array(arrayBuffer, 4 + 4, entryCount);
    console.log(timestamps);
    dataArray[0] = convertInt32ArrayToArray(timestamps);
    for(let i = 0; i < schemaCount; i++) {
      let values = new Float32Array(arrayBuffer, 4 + 4 + 4 * entryCount * (i + 1), entryCount);
      dataArray[i + 1] = convertFloat32ArrayToArray(values);
    }
    console.log(dataArray);
    data.value = dataArray;
  } catch (err) {
    console.error(err);
    dataError.value = err.message;
  } finally {
    dataLoading.value = false;
  }
}

const mask = ref(null);
const maskLoading = ref(false);
const maskError = ref(null);

const fetchMask = async () => {
  if (!selectedPlot.value || !selectedSensor.value) {
    mask.value = null;
    return;
  }

  mask.value = null;
  maskLoading.value = true;
  maskError.value = null;

  try {
    const params = new URLSearchParams({
      'station': selectedPlot.value,
      'sensor': selectedSensor.value
    });

    const response = await fetch(`/tsdb/mask?${params.toString()}`);

    if (!response.ok) {
      throw new Error(getFriendlyErrorMessage(response.status))
    }

    mask.value = await response.json();    
  } catch (err) {
    console.error(err);
    maskError.value = err.message;
    ElMessage.error('Failed to load mask: ' + err.message);
  } finally {
    maskLoading.value = false;
  }
}

watch([selectedPlot, selectedSensor], () => {
  console.log('watch([selectedPlot, selectedSensor]');
  fetchData();
  fetchMask();
}, { immediate: false });


const commentText = ref('');
const maskSelectionSaving = ref(false);

const maskSelectionSave = async () => {
  maskSelectionSaving.value = true;

  try {
    await new Promise(resolve => setTimeout(resolve, 1000));
    const response = await fetch('/tsdb/mask', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        action: 'add',
        content: {
          station: selectedPlot.value,
          sensor: selectedSensor.value,
          start: maskSelection.value.dateMin,
          end: maskSelection.value.dateMax,
          comment: commentText.value,
        }
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error: ${response.status}`);
    }

    if (viewerRef.value) {
      viewerRef.value.resetSelection();
    }

    fetchMask();

    ElMessage.success('Mask selection saved successfully!');
  } catch (err) {
    console.error(err);
    ElMessage.error('Failed to save mask selection: ' + err.message);
  } finally {
    maskSelectionSaving.value = false;
  }
};

</script>

<style scoped>
.layout-wrapper {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.top-bar,
.bottom-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f5f7fa;
  width: 100%;
  position: sticky;
  z-index: 1000;
  padding: 8px 16px;
  flex-shrink: 0;
  box-sizing: border-box;
}

.top-bar {
  border-bottom: 1px solid #dcdfe6;
  top: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.bottom-bar {
  border-top: 1px solid #dcdfe6;
  bottom: 0;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.toolbar-center {
  flex: 1;
  padding-left: 20px;
  padding-right: 20px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-label {
  font-weight: 500;
  color: #606266;
  font-size: 13px;
  white-space: nowrap;
}

.toolbar-value {
  color: #303133;
  font-size: 13px;
  margin-left: 4px;
}

.toolbar-divider {
  color: #dcdfe6;
  margin: 0 8px;
  font-size: 13px;
}

.main-content {
  flex: 1;
  overflow: auto;
  position: relative;
}
</style>