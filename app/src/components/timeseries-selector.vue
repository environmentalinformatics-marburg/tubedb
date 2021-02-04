<template>
<q-item-section>
  <q-list v-if="model !== undefined">

    <q-item tag="label" >
      <q-item-section>
        <q-select v-model="selectedProjects" :options="projects" option-label="title" label="Projects" stack-label borderless dense options-dense options-cover multiple/>
      </q-item-section>
    </q-item>

    <q-item tag="label" >
      <q-item-section>
        <q-select v-model="selectedGroups" :options="groups" option-label="title" label="Groups" stack-label borderless dense options-dense options-cover multiple/>
      </q-item-section>
    </q-item>

    <q-item tag="label" >
      <q-item-section>
        <q-select v-model="selectedPlots" :options="plots" option-label="id" label="Plots" stack-label borderless dense options-dense options-cover multiple/>
      </q-item-section>
    </q-item>

    <q-item tag="label" >
      <q-item-section>
        <q-select v-model="selectedSensors" :options="sensors" option-label="id" label="Sensors" stack-label borderless dense options-dense options-cover multiple/>
      </q-item-section>
    </q-item>             

  </q-list>         
</q-item-section>
</template>

<script>
import { mapState, mapGetters } from 'vuex';

export default {
  name: 'timeseries-selector',
  props: [
  ],
  data () {
    return {
      selectedProjects: [],
      selectedGroups: [],
      selectedPlots: [],
      selectedSensors: [],
    }
  },  
  computed: {
    ...mapState({
      model: state => state.model.data,  
      modelLoading: state => state.model.loading,
      modelError: state => state.model.error,    
    }),
    ...mapGetters({
      api: 'api',
      apiGET: 'apiGET',
      apiPOST: 'apiPOST',
    }),
    projects() {
      if(this.model === undefined) {
        return [];
      }
      let result = Object.values(this.model.projects);
      result.sort((a, b) => {
        var nameA = a.title.toLowerCase();
        var nameB = b.title.toLowerCase();
        if (nameA < nameB) {
          return -1;
        }
        if (nameA > nameB) {
          return 1;
        }
        return 0;
      });      
      return result;      
    },
    groups() {
      if(this.model === undefined || this.selectedProjects.length === 0) {
        return [];
      }
      var result = [];
      for (let project of this.selectedProjects) {
        let g = project.groups.map(group => this.model.groups[group]);
        result = result.concat(g);
      }
      result.sort((a, b) => {
        var nameA = a.title.toLowerCase();
        var nameB = b.title.toLowerCase();
        if (nameA < nameB) {
          return -1;
        }
        if (nameA > nameB) {
          return 1;
        }
        return 0;
      });      
      return result;
    },
    plots() {
      if(this.model === undefined || this.selectedGroups.length === 0) {
        return [];
      }
      var plotNames = new Set();
      for (let group of this.selectedGroups) {
        for(let plotName of group.plots) {
          plotNames.add(plotName);
        }
      }
      let result = [...plotNames].map(plotName => this.model.plots[plotName]);
      result.sort((a, b) => {
        var nameA = a.id.toLowerCase();
        var nameB = b.id.toLowerCase();
        if (nameA < nameB) {
          return -1;
        }
        if (nameA > nameB) {
          return 1;
        }
        return 0;
      });
      return result;
    },      
    sensors() {
      if(this.model === undefined || this.selectedPlots.length === 0) {
        return [];
      }
      var sensorNames = new Set();
      for (let plot of this.selectedPlots) {
        for(let sensorName of plot.sensors) {
          sensorNames.add(sensorName);
        }
      }
      let result = [...sensorNames].map(sensorName => this.model.sensors[sensorName]);
      result.sort((a, b) => {
        var nameA = a.id.toLowerCase();
        var nameB = b.id.toLowerCase();
        if (nameA < nameB) {
          return -1;
        }
        if (nameA > nameB) {
          return 1;
        }
        return 0;
      });
      return result;
    },      
  },
  methods: {
    onPlotSensorChanged() {
      this.$emit('plot-sensor-changed', {plots: this.selectedPlots, sensors: this.selectedSensors});
    },
  },
  watch: {
    selectedPlots() {
      this.onPlotSensorChanged();
    },
    selectedSensors() {
      this.onPlotSensorChanged();
    }
  },  
  async mounted() {
    this.$store.dispatch('model/init');
  },  
};
</script>

<style scoped>



</style>