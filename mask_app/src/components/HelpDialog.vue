<template>
  <el-dialog
    v-model="dialogVisible"
    title="How to Use the Mask App"
    width="600px"
    :close-on-click-modal="true"
    @close="onClose"
  >
    <div class="help-content">
      <!-- Workflow Section -->
      <el-collapse accordion>
        <el-collapse-item title="Workflow: Project → Group → Plot → Sensor" name="1">
          <p>1. Select a <strong>Project</strong> from the dropdown list.</p>
          <p>2. Once loaded, select a <strong>Group</strong> to display associated plots.</p>
          <p>3. Select a specific <strong>Plot</strong>.</p>
          <p>4. Finally, choose the desired <strong>Sensor</strong> from the available list.</p>
        </el-collapse-item> 
      </el-collapse>
      
      <el-divider />
      
      <!-- Tips Section -->
      <div class="help-tips">
        <h4>Diagram Navigation</h4>
        <ul>
          <li><b>Click & Drag:</b> Hold the left mouse button and drag left/right to pan through time.</li>
          <li><b>Zoom:</b> Rotate the mouse wheel to zoom in and out.</li>
        </ul>
        
        <h4>Creating a Time Series Mask</h4>
        <ul>
          <li><b>Set Start:</b> Hold <b>Shift</b>, or <b>Ctrl</b>, or <b>Alt</b> + <b>Left Click</b> to mark the interval start.</li>
          <li><b>Set End:</b> Hold <b>Shift</b>, or <b>Ctrl</b>, or <b>Alt</b> + <b>Right Click</b> to mark the interval end.</li>
          <li><b>Comment:</b> Enter a description in the bottom toolbar.</li>
          <li><b>Store:</b> Click the <b>Save</b> button on the right side of the toolbar.</li>
        </ul>
      </div>
    </div>
    
    <template #footer>
      <el-button type="primary" @click="closeDialog">
        Close
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, defineEmits } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'close'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const closeDialog = () => {
  dialogVisible.value = false
}

const onClose = () => {
  emit('close')
}
</script>

<style scoped>
.help-content {
  max-height: 500px;
  overflow-y: auto;
  text-align: left; /* Explizite Linksbündigkeit */
}

.help-content h4 {
  margin: 16px 0 8px 0;
  color: #303133;
  font-size: 15px;
  font-weight: 600;
}

/* Erster Header ohne Margin-Top */
.help-content > div > h4:first-child {
  margin-top: 0;
}

.help-content p {
  margin: 6px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.help-content strong {
  color: #409EFF;
  font-weight: 600;
}

.help-tips {
  padding: 20px;
  background-color: #f4f4f5;
  border-radius: 4px;
  margin-top: 16px;
  text-align: left; /* Explizite Linksbündigkeit im Tips-Bereich */
}

.help-tips ul {
  margin: 0 0 16px 0;
  padding-left: 20px;
}

.help-tips ul:last-child {
  margin-bottom: 0;
}

.help-tips li {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 4px;
}

:deep(.el-collapse-item__header) {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-collapse-item__content) {
  padding: 12px 16px;
  background-color: #fff;
}

:deep(.el-divider) {
  margin: 16px 0;
}
</style>