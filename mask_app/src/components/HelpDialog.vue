<template>
  <el-dialog
    v-model="dialogVisible"
    title="How to Use the Mask App"
    width="650px"
    close-on-click-modal
    draggable
    @close="onClose"
  >
    <div class="help-content">
      <el-collapse v-model="activeSections">

        <!-- Workflow Section -->
        <el-collapse-item title="Workflow: Project → Group → Plot → Sensor" name="workflow">
          <p>1. Select a <strong>Project</strong> from the dropdown list.</p>
          <p>2. Once loaded, select a <strong>Group</strong> to display associated plots.</p>
          <p>3. Select a specific <strong>Plot</strong> (Station) – the list is <strong>filterable</strong>, just type to search.</p>
          <p>4. Finally, choose the desired <strong>Sensor</strong> from the available list (also filterable).</p>
        </el-collapse-item>

        <!-- Data Settings Section -->
        <el-collapse-item title="Data Settings: Time Aggregation & QC" name="data">
          <p><strong>Time Aggregation:</strong> Choose between <i>Hour</i> and <i>Day</i> aggregation. Only sensors supporting the selected aggregation are listed.</p>
          <p><strong>QC (Quality Control):</strong> Select the applied quality checks applied:</p>
          <ul>
            <li><b>Raw</b> – no data checks</li>
            <li><b>Physical</b> – physical check only</li>
            <li><b>Basic</b> – physical check & step check & invalid mask</li>
            <li><b>Unsuspect</b> – basic checks & suspect mask</li>
          </ul>
        </el-collapse-item>

        <!-- Comparison Section -->
        <el-collapse-item title="Comparison (Cmp)" name="comparison">
          <p><strong>Cmp:</strong> Select a <strong>comparison sensor</strong> to compare with the main sensor.</p>
          <p><strong>Y-Range:</strong> Fix the y-axis range of the comparison diagram (e.g. -1 – 1). Clear the selection for auto-scaling.</p>
          <p><strong>Cmp_station:</strong> Select a <strong>comparison station</strong>. The list is sorted by <i>nearest plots</i> of the current station.</p>
        </el-collapse-item>

        <!-- Diagram Navigation Section -->
        <el-collapse-item title="Diagram Navigation" name="navigation">
          <ul>
            <li><b>Click &amp; Drag:</b> Hold the left mouse button and drag left/right to pan through time.</li>
            <li><b>Zoom:</b> Rotate the mouse wheel to zoom in and out (centered on the cursor position).</li>
            <li><b>Double-click:</b> Reset zoom to the full time range.</li>
            <li><b>Sync:</b> Main and comparison diagram are always synchronized.</li>
          </ul>
        </el-collapse-item>

        <!-- Legend Section -->
        <el-collapse-item title="Diagram Legend" name="legend">
          <div class="legend-item">
            <span class="legend-swatch legend-selection"></span>
            <span>Current time interval selection</span>
          </div>
          <div class="legend-item">
            <span class="legend-swatch legend-invalid"></span>
            <span>Stored <b>invalid</b> mask</span>
          </div>
          <div class="legend-item">
            <span class="legend-swatch legend-suspect"></span>
            <span>Stored <b>suspect</b> mask</span>
          </div>
        </el-collapse-item>

        <!-- Creating a Mask Section -->
        <el-collapse-item title="Creating a Time Series Mask" name="mask">
          <ul>
            <li><b>Set Start:</b> Hold <b>Shift</b>-key + <b>Left Click</b> to mark the interval start.</li>
            <li><b>Set End:</b> Hold <b>Shift</b>-key + <b>Right Click</b> to mark the interval end.</li>
            <li><i>(Instead of <b>Shift</b> works also: <b>Ctrl</b> or <b>Alt</b>)</i></li>
            <li><b>Reset:</b> Press <b>Esc</b> to clear the time interval selection.</li>
            <li><b>Comment:</b> Enter a description in the bottom toolbar.</li>
            <li><b>Mask type:</b> Ensure the appropriate mask type is chosen:
              <ul>
                <li><span class="mask-invalid">Invalid</span> – value is definitely wrong</li>
                <li><span class="mask-suspect">Suspect</span> – value is questionable</li>
              </ul>
            </li>
            <li><b>Store:</b> Click the <b>Save</b> button to finish. The current selection (start – end) is shown in the bottom toolbar.</li>
          </ul>
        </el-collapse-item>

        <!-- Mask List Section -->
        <el-collapse-item title="Mask List Management" name="masklist">
          <p>Open the mask list with the <b>grid button</b> in the bottom right corner. It shows all stored masks of the selected station/sensor.</p>
          <ul>
            <li>Columns: <b>Type</b>, <b>Start</b>, <b>End</b>, <b>User</b>, <b>Date</b>, <b>Comment</b> – click a column header to sort.</li>
            <li><b>Show Removed:</b> Toggle to also display previously removed entries.</li>
            <li><b>Remove:</b> Delete an entry via the trash icon (an optional comment can be provided).</li>
          </ul>
        </el-collapse-item>

        <!-- Settings Section -->
        <el-collapse-item title="Settings" name="settings">
          <ul>
            <li><b>Separate sensor diagrams:</b> Display the comparison sensor in a separate diagram above the main diagram. When disabled, the comparison sensor is drawn into the main diagram.</li>
            <li><b>Show stored masks:</b> Show/hide stored masks in the diagram.</li>
          </ul>
        </el-collapse-item>

<!-- Shortcuts Section -->
<el-collapse-item title="Keyboard, Mouse & Touchpad Shortcuts" name="shortcuts">
  <table class="shortcut-table">
    <tbody>
    <tr>
      <td><b>Shift / Ctrl / Alt + Left Click</b></td>
      <td>Set mask interval start</td>
    </tr>
    <tr>
      <td>Shift / <b>Ctrl / Alt + Right Click</b></td>
      <td>Set mask interval end</td>
    </tr>
    <tr>
      <td><b>Esc</b></td>
      <td>Reset interval selection</td>
    </tr>
    <tr>
      <td><b>Left Click &amp; Drag</b></td>
      <td>Pan through time</td>
    </tr>
    <tr>
      <td><b>Mouse Wheel</b></td>
      <td>Zoom in / out</td>
    </tr>
    <tr>
      <td><b>Double-click</b></td>
      <td>Reset zoom to full range</td>
    </tr>
    </tbody>
  </table>

  <p><br><i>Touchpad gestures (analogous to the mouse) depend on your device and system settings.<br>Typically, these are:</i></p>
  <table class="shortcut-table">
    <tbody>
    <tr>
      <td><b>Shift / Ctrl / Alt + Left-click / Double-tap</b></td>
      <td>Set mask interval start (hold for a second to prevent a double-click event)</td>
    </tr>
    <tr>
      <td>Shift / <b>Ctrl / Alt + Right-click / Two-finger tap</b></td>
      <td>Set mask interval end</td>
    </tr>
    <tr>
      <td><b>Double-tap &amp; Drag</b></td>
      <td>Pan through time</td>
    </tr>
    <tr>
      <td><b>Vertical two-finger spread-pinch / Two finger move up-down</b></td>
      <td>Zoom in / out</td>
    </tr>
    <tr>
      <td><b>Double-tap</b></td>
      <td>Reset zoom to full range</td>
    </tr>
    </tbody>
  </table>
</el-collapse-item>

      </el-collapse>
    </div>

    <template #footer>
      <el-button type="primary" @click="closeDialog">
        Close
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'close'])

const activeSections = ref(['workflow'])

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
  text-align: left;
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

.help-content ul {
  margin: 6px 0;
  padding-left: 20px;
}

.help-content li {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 4px;
}

.mask-invalid {
  color: #de4242;
  font-weight: 600;
}

.mask-suspect {
  color: #c5c503;
  font-weight: 600;
}

/* Legende */
.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
}

.legend-swatch {
  display: inline-block;
  width: 32px;
  height: 14px;
  border-radius: 2px;
  border: 1px solid rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.legend-selection {
  background-color: rgba(66, 133, 244, 0.3);
}

.legend-invalid {
  background-color: rgba(222, 66, 66, 0.3);
}

.legend-suspect {
  background-color: rgba(222, 222, 66, 0.3);
}

/* Shortcut-Tabelle */
.shortcut-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  color: #606266;
}

.shortcut-table td {
  padding: 6px 8px;
  border-bottom: 1px solid #ebeef5;
  vertical-align: top;
}

.shortcut-table td:first-child {
  width: 230px;
  color: #303133;
}

.shortcut-table tr:last-child td {
  border-bottom: none;
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
</style>