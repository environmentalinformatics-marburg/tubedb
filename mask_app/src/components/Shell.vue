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
          style="width: 200px;"
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
      </div>
      <div class="toolbar-right">
        <!-- Platz für weitere Toolbar-Elemente -->
      </div>
    </div>

    <!-- Main Content -->
    <div class="main-content">
      <Viewer v-if="selectedPlot" :plot="selectedPlot" />
    </div>

    <!-- Bottom Toolbar -->
    <div class="bottom-bar">
      <div class="toolbar-left">
        <span class="toolbar-label">Plot:</span>
        <span class="toolbar-value">{{selectedPlot}}</span>
      </div>
      <div class="toolbar-right">
        <!-- Platz für weitere Bottom-Toolbar-Elemente -->
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import Viewer from './Viewer.vue'

const props = defineProps({
  metaData: {
    type: Object,
    required: true
  }
})

const selectedProject = ref('')
const selectedGroup = ref('')
const selectedPlot = ref('')

const selectedProjectData = computed(() => {
  if (!selectedProject.value || !props.metaData?.model?.projects) return null
  return props.metaData.model.projects[selectedProject.value]
})

const selectedGroupData = computed(() => {
  if (!selectedGroup.value || !props.metaData?.model?.groups) return null
  return props.metaData.model.groups[selectedGroup.value]
})

const selectedPlotData = computed(() => {
  if (!selectedPlot.value || !props.metaData?.model?.plots) return null
  return props.metaData.model.plots[selectedPlot.value]
})

const projectOptions = computed(() => {
  if (!props.metaData?.model?.projects) return []
  return Object.values(props.metaData.model.projects).map(p => ({
    value: p.id,
    label: p.title
  }))
})

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
  })
})

const plotOptions = computed(() => {
  if (!selectedGroup.value || !props.metaData?.model?.groups) return []
  
  const group = props.metaData.model.groups[selectedGroup.value]
  if (!group?.plots) return []
  
  return group.plots.map(plotId => ({
    value: plotId,
    label: plotId
  }))
})

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
        }
      }
    }
  }
}, { immediate: true })

watch(selectedProject, (newProject) => {
  if (newProject && props.metaData?.model?.projects?.[newProject]?.groups) {
    const firstGroup = props.metaData.model.projects[newProject].groups[0]
    selectedGroup.value = firstGroup || ''
    selectedPlot.value = ''
  } else {
    selectedGroup.value = ''
    selectedPlot.value = ''
  }
})

watch(selectedGroup, (newGroup) => {
  if (newGroup && props.metaData?.model?.groups?.[newGroup]?.plots) {
    const firstPlot = props.metaData.model.groups[newGroup].plots[0]
    selectedPlot.value = firstPlot || ''
  } else {
    selectedPlot.value = ''
  }
})

const onProjectChange = (value) => {
  console.log('selected project:', value)
}

const onGroupChange = (value) => {
  console.log('selected group:', value)
}

const onPlotChange = (value) => {
  console.log('selected plot:', value)
}
</script>

<style scoped>
/* Layout Wrapper für korrektes Sticky-Verhalten */
.layout-wrapper {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* Gemeinsame Toolbar-Stile */
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

/* Scrollbarer Hauptbereich */
.main-content {
  flex: 1;
  overflow: auto;
  position: relative;
}
</style>