<template>
  <el-dialog
    v-model="dialogVisible"
    title="Mask List"
    width="700px"
    :close-on-click-modal="true"
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
          <span class="mask-title mask-title-invalid">Invalid</span>
          <el-tag size="small" type="danger" effect="plain" round>
            {{ invalidMasks.length }}
          </el-tag>
        </div>

        <el-table
          :data="invalidMasks"
          size="small"
          border
          stripe
          max-height="250"
          empty-text="No invalid masks"
        >
          <el-table-column type="index" label="#" width="50" align="center" />
          <el-table-column label="Start" min-width="150" sortable
            :sort-method="(a, b) => sortTime(a, b, 'start')">
            <template #default="{ row }">
              <span :class="{ wildcard: isWildcard(row.start) }">{{ formatTime(row.start) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="End" min-width="150" sortable
            :sort-method="(a, b) => sortTime(a, b, 'end')">
            <template #default="{ row }">
              <span :class="{ wildcard: isWildcard(row.end) }">{{ formatTime(row.end) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="Comment" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">{{ row.comment || '–' }}</template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Suspect Masks -->
      <div class="mask-section">
        <div class="mask-section-header">
          <span class="mask-title mask-title-suspect">Suspect</span>
          <el-tag size="small" type="warning" effect="plain" round>
            {{ suspectMasks.length }}
          </el-tag>
        </div>

        <el-table
          :data="suspectMasks"
          size="small"
          border
          stripe
          max-height="250"
          empty-text="No suspect masks"
        >
          <el-table-column type="index" label="#" width="50" align="center" />
          <el-table-column label="Start" min-width="150" sortable
            :sort-method="(a, b) => sortTime(a, b, 'start')">
            <template #default="{ row }">
              <span :class="{ wildcard: isWildcard(row.start) }">{{ formatTime(row.start) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="End" min-width="150" sortable
            :sort-method="(a, b) => sortTime(a, b, 'end')">
            <template #default="{ row }">
              <span :class="{ wildcard: isWildcard(row.end) }">{{ formatTime(row.end) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="Comment" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">{{ row.comment || '–' }}</template>
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
import { ElMessage } from 'element-plus'
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

const emit = defineEmits(['update:modelValue', 'close'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

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

const invalidMasks = computed(() => maskData.value?.mask ?? [])
const suspectMasks = computed(() => maskData.value?.suspect_mask ?? [])

const isWildcard = (value) =>
  value === undefined || value === null || value === '' || value === '*'

const formatTime = (value) => {
  if (isWildcard(value)) return '*'
  return String(value).replace('T', ' ')
}

const sortTime = (a, b, key) => {
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

.wildcard {
  color: #909399;
  font-style: italic;
}
</style>