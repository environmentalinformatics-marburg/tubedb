<template>
  <q-dialog v-model="shown" :maximized="true">
    <q-layout view="hHh lpR fFf" class="bg-grey-1">

      <q-header elevated class="bg-grey-1 text-black">
        <q-bar>
          Status
          <q-space />
          {{ plot }}
          <q-space />
          <q-btn dense flat icon="close" v-close-popup>
            <q-tooltip>Close</q-tooltip>
          </q-btn>
        </q-bar>
        <q-linear-progress indeterminate v-if="loading" />
      </q-header>

      <q-page-container class="text-black" v-if="!loading && !error">
        <q-list>
          <q-item>
            <q-item-section side>
              <q-btn
                flat
                round
                color="primary"
                icon="timeline"
                :href="api('content/visualisation_meta/visualisation_meta.html?pinned_project=' + project.id + '&pinned_plot=' + plot)"
                target="_blank"
                title="Open timeseries diagram in a new tab."
              />
            </q-item-section>
            <q-item-section>
              <span class="info">
                <span v-if="row.first_date !== undefined">Measurements from <span>{{ row.first_date }}</span> <span>{{ row.first_time }}</span></span>
                <span v-if="row.first_date !== undefined"> to <span>{{ row.last_date }}</span> <span>{{ row.last_time }}</span></span>
                <span v-if="row.elapsed_days !== undefined"> Elapsed days <span>{{ row.elapsed_days }}</span></span>
                <span v-if="row.voltage!== undefined"> Latest voltage <span>{{ row.voltage }}</span></span>
                <span v-if="row.author !== undefined"> Latest writer <span>{{ row.author }}</span> at <span>{{ row.datetime }}</span></span>
              </span>
            </q-item-section>
          </q-item>

          <q-item>
            <q-item-section side><q-icon name="cell_tower" /></q-item-section>
            <q-item-section>
              <q-select
                outlined
                v-model="row.status"
                :options="transmissionOptions"
                use-input
                :clearable="isFocusedTransmissionSelect"
                new-value-mode="add-unique"
                :label="transmissionLabel"
                stack-label
                dense
                :input-value="transmissionInputValue"
                @input-value="transmissionInputValue = $event"
                @focus="isFocusedTransmissionSelect = true"
                @blur="isFocusedTransmissionSelect = false"
              />
            </q-item-section>
          </q-item>

          <q-item>
            <q-item-section side><q-icon name="flag" /></q-item-section>
            <q-item-section>
              <q-select
                outlined
                v-model="row.condition"
                :options="conditionOptions"
                use-input
                :clearable="isFocusedConditionSelect"
                new-value-mode="add-unique"
                :label="conditionLabel"
                stack-label
                dense
                :input-value="conditionInputValue"
                @input-value="conditionInputValue = $event"
                @focus="isFocusedConditionSelect = true"
                @blur="isFocusedConditionSelect = false"
              />
            </q-item-section>
          </q-item>

          <q-item>
            <q-item-section side><q-icon name="task" /></q-item-section>
            <q-item-section>
              <q-list bordered separator class="bg-white" v-if="row.tasks && row.tasks.length > 0">
                <q-item v-for="(t, index) in row.tasks" :key="t.id" class="task-item-container">
                  <div class="task-tab-label">
                    <span class="task-label-text">{{ plot }} task {{ t.id }}</span>
                    <span class="task-date">{{ t.created }}</span>
                  </div>
                  <q-item-section>
                    <q-input
                      v-model="t.task"
                      dense
                      outlined
                      :class="{ 'text-grey-5': t.status === 'done' }"
                      @blur="normalizeTasks"
                    />
                  </q-item-section>
                  <q-item-section side>
                    <q-select
                      v-model="t.status"
                      :options="taskStatusOptions"
                      dense
                      outlined
                      emit-value
                      map-options
                      class="q-mx-sm"
                      style="width: 150px;"
                    >
                      <template v-slot:prepend>
                        <q-icon
                          :name="taskStatusOptions.find(opt => opt.value === t.status)?.icon || 'event'"
                          :color="taskStatusOptions.find(opt => opt.value === t.status)?.color || 'grey-5'"
                        />
                      </template>

                      <template v-slot:option="scope">
                        <q-item v-bind="scope.itemProps">
                          <q-item-section avatar>
                            <q-icon :name="scope.opt.icon" :color="scope.opt.color" />
                          </q-item-section>
                          <q-item-section>{{ scope.opt.label }}</q-item-section>
                        </q-item>
                      </template>
                    </q-select>
                  </q-item-section>
                  <q-item-section side>
                    <q-btn flat round dense icon="delete" color="negative" @click="removeTask(index)" />
                  </q-item-section>
                </q-item>
              </q-list>
              
              <q-input
                dense
                outlined
                label="Add new task"
                stack-label
                v-model="newTask"
                @keyup.enter="addTask"
                clearable
                class="q-mt-sm"
              >
                <template v-slot:append>
                  <q-btn flat round dense icon="add" color="primary" @click="addTask">
                    <q-tooltip>Click or press Enter to add task</q-tooltip>
                  </q-btn>
                </template>
              </q-input>
            </q-item-section>
          </q-item>

          <q-item>
            <q-item-section side><q-icon name="text_snippet" /></q-item-section>
            <q-item-section><q-input outlined v-model="row.notes" label="Notes" stack-label dense type="textarea" /></q-item-section>
          </q-item>
        </q-list>

        <q-expansion-item
          dense
          expand-separator
          icon="history"
          label="History"
          :disable="row.history === undefined || row.history.length === 0"
        >
          <q-list v-if="row.history !== undefined && row.history.length > 0">
            <template v-for="e in row.history.slice().reverse()" :key="e.datetime">
              <q-item>
                <q-item-section>
                  <q-item-label><b>{{ e.status }}</b> <i class="q-mx-md">{{ e.condition }}</i> {{ Array.isArray(e.tasks) ? e.tasks.map(t => `${t.task} [${t.status || 'open'}]`).join(', ') : e.tasks }}</q-item-label>
                  <q-item-label caption>{{ e.notes }}</q-item-label>
                </q-item-section>

                <q-item-section top side>
                  <q-item-label caption><i>{{ e.author }}</i> {{ e.datetime }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-separator spaced inset />
            </template>
          </q-list>
        </q-expansion-item>

      </q-page-container>

      <q-footer elevated class="bg-grey-1" v-if="!loading && !error">
        <q-card>
          <q-card-actions align="right">
            <q-btn flat label="Cancel" color="primary" v-close-popup />
            <q-btn flat label="Save" color="primary" @click="onSubmit" :loading="submitting" />
          </q-card-actions>
        </q-card>
      </q-footer>

    </q-layout>
  </q-dialog>
</template>

<script>
import { mapGetters } from 'vuex';

export default {
  props: ['transmissionOptions', 'conditionOptions'],
  components: {},
  data() {
    return {
      shown: false,
      plot: undefined,
      loading: false,
      error: false,
      submitting: false,
      row: {},
      isFocusedTransmissionSelect: false,
      transmissionInputValue: '',
      isFocusedConditionSelect: false,
      conditionInputValue: '',
      newTask: '',
      taskStatusOptions: [
        { label: 'Open', value: 'open', icon: 'visibility', color: 'primary' },
        { label: 'Progress', value: 'progress', icon: 'speed', color: 'orange' },
        { label: 'Done', value: 'done', icon: 'check_circle', color: 'positive' },
        { label: 'Blocked', value: 'blocked', icon: 'block', color: 'negative' },
        { label: 'Cancelled', value: 'cancelled', icon: 'cancel', color: 'grey-7' }
      ]
    };
  },
  computed: {
    ...mapGetters({
      api: 'api',
      apiGET: 'apiGET',
      apiPOST: 'apiPOST',
    }),
    transmissionLabel() {
      if (this.transmissionInputValue && this.transmissionInputValue !== this.row.status) {
        return 'Transmission (Press Enter to confirm)';
      }
      return 'Transmission';
    },
    conditionLabel() {
      if (this.conditionInputValue && this.conditionInputValue !== this.row.condition) {
        return 'Condition (Press Enter to confirm)';
      }
      return 'Condition';
    }
  },
  methods: {
    show(project, plot) {
      this.shown = true;
      this.project = project;
      this.plot = plot;
      this.refresh();
    },
    async refresh() {
      try {
        this.loading = true;
        const params = new URLSearchParams();
        params.append('plot', this.plot);
        params.append('plot_status', '');
        params.append('history', '');
        const response = await this.apiGET(['tsdb', 'status'], { params });
        let rows = response.data;
        rows.forEach(row => {
          {
            const a = row.first_datetime.split('T');
            row.first_date = a[0];
            row.first_time = a[1];
          }
          {
            const a = row.last_datetime.split('T');
            row.last_date = a[0];
            row.last_time = a[1];
          }
        });
        this.row = rows.length === 0 ? {} : rows[0];
        
        this.normalizeTasks();

        if (this.row.plot !== this.plot) {
          this.error = true;
          this.plot = undefined;
          this.row = {};
          this.$q.notify({ message: 'Error loading data.', type: 'negative' });
        }
      } catch (e) {
        this.error = true;
        this.plot = undefined;
        this.rows = [];
        console.log(e);
        this.$q.notify({ message: 'Error loading data.', type: 'negative' });
      } finally {
        this.loading = false;
      }
    },
    getNextTaskId(tasks) {
      if (!tasks || tasks.length === 0) return 1;
      const maxNum = tasks.reduce((max, t) => {
        const num = parseInt(t.id, 10);
        return (!isNaN(num) && num > max) ? num : max;
      }, 0);
      return maxNum + 1;
    },
    normalizeTasks() {
      if (!this.row.tasks) {
        this.row.tasks = [];
      } else if (typeof this.row.tasks === 'string') {
        this.row.tasks = [{
          task: this.row.tasks,
          //created: new Date().toISOString(),  //will not be set for old tasks
          status: 'open',
          id: 1
        }];
      } else if (Array.isArray(this.row.tasks)) {
        const existingNums = new Set();
        this.row.tasks.forEach(t => {
          const num = parseInt(t.id, 10);
          if (!isNaN(num)) existingNums.add(num);
        });

        this.row.tasks = this.row.tasks.map((t, i) => {
          let id = t.id;
          if (!id || isNaN(parseInt(id, 10))) {
            let num = 1;
            while (existingNums.has(num)) num++;
            existingNums.add(num);
            id = num;
          }
          return {
            task: t.task || '',
            //created: t.created || new Date().toISOString(),  // will not be set for old tasks
            created: t.created,
            status: t.status || (t.done ? 'done' : 'open'),
            id: id
          };
        });
      }
    },
    addTask() {
      if (!this.newTask.trim()) return;
      this.normalizeTasks();
      const nextId = this.getNextTaskId(this.row.tasks);
      this.row.tasks.push({
        task: this.newTask.trim(),
        //created: new Date().toISOString(), // will be set in backend
        status: 'open',
        id: nextId
      });
      this.newTask = '';
    },
    removeTask(index) {
      this.row.tasks.splice(index, 1);
    },
    async onSubmit() {
      if (this.newTask && this.newTask.trim() !== '') {
        this.$q.notify({
          message: 'Cannot save: The "Add new task" field is not empty.',
          caption: 'Please add the task to the list first, or clear the field.',
          type: 'negative',
          icon: 'warning',
          actions: [
            {
              label: 'Clear field',
              color: 'white',
              handler: () => {
                this.newTask = '';
              }
            }
          ]
        });
        return;
      }

      try {
        this.submitting = true;
        await this.apiPOST(['tsdb', 'status2'], this.row);
        this.$emit("changed");
        this.shown = false;
      } catch (e) {
        console.log(e);
        this.$q.notify({ message: 'Error submitting data.', type: 'negative' });
      } finally {
        this.submitting = false;
      }
    },
  },
  watch: {
    shown: {
      handler(val) {
        if (!val) {
          this.newTask = '';
          this.transmissionInputValue = '';
          this.conditionInputValue = '';
        }
      }
    },    
    'row.status': {
      immediate: true,
      handler(val) {
        this.transmissionInputValue = val || '';
      }
    },
    'row.condition': {
      immediate: true,
      handler(val) {
        this.conditionInputValue = val || '';
      }
    }
  },
  async mounted() {
  },
}
</script>

<style scoped>
.info span span {
  font-weight: bold;
}

.task-item-container {
  position: relative;
  padding-top: 16px;
}

.task-tab-label {
  position: absolute;
  left: 10px;
  right: 240px; 
  top: 2px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.6rem;
  color: #9e9e9e;
  font-weight: 500;
  letter-spacing: 0.5px;
  pointer-events: none;
  z-index: 1;
  padding: 0 12px;
}
</style>

<style>
</style>