<template>
  <el-dialog
    v-model="dialogVisible"
    title="Mask List"
    width="1200px"
    close-on-click-modal
    draggable
    @open="fetchMaskList"
    @close="onClose"
  >
    <!-- Loading -->
    <div v-if="loading" class="state-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>Loading mask list...</span>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="state-container">
      <el-icon><Warning /></el-icon>
      <span>{{ error }}</span>
      <el-button type="primary" size="small" @click="fetchMaskList">
        Retry
      </el-button>
    </div>

    <!-- Keine Daten -->
    <el-empty
      v-else-if="!maskData"
      description="No mask data available"
      :image-size="80"
    />

    <!-- Tabellen -->
    <div v-else class="mask-list-content">
      <!-- Kopfzeile: Station / Sensor -->
      <div class="mask-info">
        <span class="info-label">Station:</span>
        <span class="info-value">{{ maskData.station || plot || '-' }}</span>
        <span class="info-divider">|</span>
        <span class="info-label">Sensor:</span>
        <span class="info-value">{{ maskData.sensor || sensor || '-' }}</span>
      </div>

      <!-- Invalid Masks -->
      <div class="mask-section">
        <div class="mask-section-header">
          <span>Masks</span>
          <el-tag size="small" effect="plain" round>
            {{ masks.length }}
          </el-tag>
        </div>

        <el-table
          :data="masks"
          size="small"
          border
          stripe
          empty-text="No masks"
        >
        <el-table-column label="Time Interval" header-align="center">
          <el-table-column label="Type" width="70" prop="type" sortable show-overflow-tooltip header-align="center">
            </el-table-column>
            <el-table-column label="Start" width="90" prop="start" sortable
              :sort-method="(a, b) => sortTimeStart(a, b, 'start')" header-align="center">
              <template #default="{ row }">
                <span>{{ formatTime(row.start) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="End" width="90" prop="end" sortable
              :sort-method="(a, b) => sortTimeEnd(a, b, 'end')" header-align="center">
              <template #default="{ row }">
                <span>{{ formatTime(row.end) }}</span>
              </template>
            </el-table-column>
          </el-table-column>
          <el-table-column label="Origin" header-align="center">  
            <el-table-column label="User" width="100" prop="user" sortable show-overflow-tooltip header-align="center">
            </el-table-column>
            <el-table-column label="Date" width="120" prop="date" sortable show-overflow-tooltip header-align="center">
               <template #default="{ row }">
                <span>{{ formatTime(row.date) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="Comment" prop="comment" sortable show-overflow-tooltip header-align="center">
            </el-table-column>
          </el-table-column>
          <el-table-column header-align="right">
            <template #header>
              <div class="admin-header">
                <span><el-switch
                  v-model="showRemoved"
                  size="small"
                  title="Show old removed mask entries"
                  @click.stop
                />Show Removed</span>
              </div>
            </template>
           
            <el-table-column label="Operations" :width="showRemoved ? 80 : 150" header-align="center">
              <template #default="scope">
                <el-button
                  v-if="!scope.row.removed"
                  size="small"
                  icon="Delete"
                  circle
                  :loading="removingRow === scope.row"
                  @click.prevent="removeRow(scope.row)"
                  title="Remove mask entry"
                />
              </template>            
            </el-table-column>
            <el-table-column label="Removed" v-if="showRemoved" width="120" prop="removed" sortable show-overflow-tooltip header-align="center">
            </el-table-column>
          </el-table-column>           
        </el-table>
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
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Warning } from '@element-plus/icons-vue'
import { getFriendlyErrorMessage } from '@/utils/errorMessages'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  plot: {
    type: String,
    default: ''
  },
  sensor: {
    type: String,
    default: ''
  },
})

const emit = defineEmits(['update:modelValue', 'close', 'changed'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const showRemoved = ref(false);
const maskData = ref(null);
const loading = ref(false);
const error = ref(null);

const fetchMaskList = async () => {
  if (!props.plot || !props.sensor) {
    maskData.value = null;
    return;
  }

  loading.value = true;
  error.value = null;

  try {
    const params = new URLSearchParams({
      'station': props.plot,
      'sensor': props.sensor
    });

    const response = await fetch(`/tsdb/masklist?${params.toString()}`);

    if (!response.ok) {
      throw new Error(getFriendlyErrorMessage(response.status))
    }

    maskData.value = await response.json();
  } catch (err) {
    console.error(err);
    error.value = err.message;
    ElMessage.error('Failed to load mask list: ' + err.message);
  } finally {
    loading.value = false;
  }
}

watch([() => props.plot, () => props.sensor], () => {
  if (props.modelValue) {
    fetchMaskList();
  }
});

const invalidMasks = computed(() => maskData.value?.mask ?? []);
const suspectMasks = computed(() => maskData.value?.suspect_mask ?? []);
const masks = computed(() => {
  const allMasks = [
    ...(maskData.value?.mask ?? []),
    ...(maskData.value?.suspect_mask ?? [])
  ];
  
  if (!showRemoved.value) {
    return allMasks.filter(row => !row.removed);
  }
  
  return allMasks;
});

const isWildcard = (value) =>
  value === undefined || value === null || value === '' || value === '*'

const formatTime = (value) => {
  if (isWildcard(value)) return '*'
  return String(value).replace('T', ' ')
}

const sortTimeStart = (a, b, key) => {
  const av = isWildcard(a[key]) ? '1000-01-01' : a[key]
  const bv = isWildcard(b[key]) ? '1000-01-01' : b[key]
  return String(av).localeCompare(String(bv))
}

const sortTimeEnd = (a, b, key) => {
  const av = isWildcard(a[key]) ? '9999-12-31' : a[key]
  const bv = isWildcard(b[key]) ? '9999-12-31' : b[key]
  return String(av).localeCompare(String(bv))
}

const closeDialog = () => {
  dialogVisible.value = false
}

const onClose = () => {
  emit('close')
}

const removingRow = ref(null);

const removeRow = async (row) => {
  if (!row || removingRow.value) return;

  let removeComment = '';
  try {
    const { value } = await ElMessageBox.prompt(
      'Do you really want to remove this mask entry?',
      'Remove mask',
      {
        confirmButtonText: 'Remove',
        cancelButtonText: 'Cancel',
        type: 'warning',
        inputPlaceholder: 'Optional comment',
        draggable: true,
      }
    );
    removeComment = (value || '').trim();
  } catch {
    return;
  }

  removingRow.value = row;

  try {
    const response = await fetch('/tsdb/masklist', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        action: 'remove',
        mask: {
          ...row,
        },
        comment: removeComment,
      }),
    });

    if (!response.ok) {
      throw new Error(getFriendlyErrorMessage(response.status));
    }

    ElMessage.success('Mask entry removed');

    await fetchMaskList();

    emit('changed', {
      action: 'remove',
      station: props.plot,
      sensor: props.sensor,
      mask: row,
    });

  } catch (err) {
    console.error(err);
    ElMessage.error('Failed to remove mask: ' + err.message);
  } finally {
    removingRow.value = null;
  }
};
</script>

<style scoped>
.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 150px;
  color: #606266;
}

.is-loading {
  font-size: 28px;
  color: #409eff;
}

.mask-list-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.mask-info {
  display: flex;
  align-items: center;
  font-size: var(--el-font-size-small);
  color: #606266;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.info-value {
  color: #303133;
  font-weight: 600;
  margin-left: 4px;
}

.info-divider {
  color: #dcdfe6;
  margin: 0 12px;
}

.mask-section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.mask-title {
  font-size: var(--el-font-size-small);
  font-weight: 600;
}

.mask-title-invalid {
  color: #de4242;
}

.mask-title-suspect {
  color: #c5c503;
}



</style>