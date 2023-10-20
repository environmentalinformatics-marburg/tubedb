<template>
  <q-layout view="hHh LpR fFf">

    <q-header reveal elevated class="bg-grey-7 text-grey-4">
      <pages-toolbar title="TubeDB Web App" active="/overview"/>
    </q-header>

    <q-page-container class="row justify-center">
      <q-page padding class="column justify-center">

        <q-card bordered class="overview-item">
          <q-card-section>
            <div class="text-h6">Metadata</div>
          </q-card-section>
          <q-card-actions>
            <q-btn outline to="/model" label="View sensors" />
          </q-card-actions>
          <q-separator inset />
          <q-card-section>
            Besides actual time series data, TubeDB contains metadata about plots, stations and sensors.
          </q-card-section>
        </q-card>

        <q-card bordered class="overview-item">
          <q-card-section>
            <div class="text-h6">Visualisations</div>
          </q-card-section>
          <q-card-actions>
            <q-btn outline to="/diagram" label="View diagrams" />
          </q-card-actions>
          <q-separator inset />
          <q-card-section>
            Interactive visualisations of time series data.
          </q-card-section>
        </q-card>

        <q-card bordered class="overview-item">
          <q-card-section>
            <div class="text-h6">Monitoring</div>
          </q-card-section>
          <q-card-actions>
            <q-btn outline to="/monitoring" label="View station and sensor status" />
          </q-card-actions>
          <q-separator inset />
          <q-card-section>
            Monitoring operability of measuring stations and sensors.
          </q-card-section>
        </q-card>

        <q-card bordered class="overview-item">
          <q-card-section>
            <div class="text-h6">Plot status</div>
          </q-card-section>
          <q-card-actions>
            <q-btn outline to="/plot_status" label="View plot status" />
          </q-card-actions>
          <q-separator inset />
          <q-card-section>
            Plot status.
          </q-card-section>
        </q-card>

      </q-page>
    </q-page-container>

  </q-layout>
</template>

<script>

import pagesToolbar from 'components/pages-toolbar.vue';

export default {
  components: {
    pagesToolbar,
  },
  data () {
    return {

    }
  },
  computed: {
  },
  methods: {
  },
  watch: {
  },
  async mounted() {
  },
}
</script>

<style scoped>

.overview-item {
  margin-bottom: 20px;
}

</style>
