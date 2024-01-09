<template>
    <q-dialog v-model="shown" :maximized="true">
      <q-layout view="hHh lpR fFf" class="bg-grey-1">

<q-header elevated class="bg-grey-1 text-black">
  <q-bar>
    Status
    <q-space />
    {{plot}}
    <q-space />
    <q-btn dense flat icon="close" v-close-popup>
      <q-tooltip>Close</q-tooltip>
    </q-btn>
  </q-bar>
  <q-linear-progress indeterminate v-if="loading"/>
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
          <span v-if="row.first_date !== undefined">Measurements from <span>{{row.first_date}}</span> <span>{{row.first_time}}</span></span>
          <span v-if="row.first_date !== undefined"> to <span>{{row.last_date}}</span> <span>{{row.last_time}}</span></span>
          <span v-if="row.elapsed_days !== undefined"> Elapsed days <span>{{row.elapsed_days}}</span></span>
          <span v-if="row.voltage!== undefined"> Latest voltage <span>{{row.voltage}}</span></span>
          <span v-if="row.author !== undefined"> Status author <span>{{row.author}}</span> date <span>{{row.datetime}}</span></span>
        </span>
      </q-item-section>
    </q-item>

    <q-item>
      <q-item-section side><q-icon name="cell_tower" /></q-item-section>
      <q-item-section><q-input outlined v-model="row.status" label="Status" stack-label dense /></q-item-section>
    </q-item>

    <q-item>
      <q-item-section side><q-icon name="task" /></q-item-section>
      <q-item-section><q-input outlined v-model="row.tasks" label="Tasks" stack-label dense /></q-item-section>
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
          <q-item-label><b>{{e.status}}</b> {{e.tasks}}</q-item-label>
          <q-item-label caption>{{e.notes}}</q-item-label>
        </q-item-section>

        <q-item-section top side>
          <q-item-label caption><i>{{e.author}}</i> {{e.datetime}}</q-item-label>
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
  props: [
  ],
  components: {
  },
  data() {
    return {
      shown: false,
      plot: undefined,
      loading: false,
      error: false,
      submitting: false,
      row: {},
    };
  },
  computed: {
    ...mapGetters({
      api: 'api',
      apiGET: 'apiGET',
      apiPOST: 'apiPOST',
    }),
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
        const response = await this.apiGET(['tsdb', 'status'], {params});
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
        if(this.row.plot !== this.plot) {
          this.error = true;
          this.plot = undefined;
          this.row = {};
          this.$q.notify({message: 'Error loading data.', type: 'negative'});
        }
      } catch(e) {
        this.error = true;
        this.plot = undefined;
        this.rows = [];
        console.log(e);
        this.$q.notify({message: 'Error loading data.', type: 'negative'});
      } finally {
        this.loading = false;
      }
    },
    async onSubmit() {
      try {
        this.submitting = true;
        await this.apiPOST(['tsdb', 'status'], this.row);
        this.$emit("changed");
        this.shown = false;
      } catch(e) {
        console.log(e);
        this.$q.notify({message: 'Error submitting data.', type: 'negative'});
      } finally {
        this.submitting = false;
      }
    },
  },
  watch: {
  },
  async mounted() {
  },
}
</script>

<style scoped>

.info span span {
  font-weight: bold;
}

</style>

<style>


</style>
