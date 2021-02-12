<template>
<q-item-section>
  <q-list v-if="model !== undefined">

    <q-item tag="label" >
      <q-item-section>
        <q-select v-model="selectedProjectsModel" :options="projects" option-value="id" for="id" option-label="title" label="Projects" stack-label borderless dense options-dense options-cover :multiple="multiTimeseries">
          <template v-if="selectedProjects.length > 0" v-slot:append>
            <q-icon name="cancel" @click.stop="selectedProjectsModel = null" class="cursor-pointer" />
          </template>
        </q-select>
      </q-item-section>
    </q-item>

    <template v-if="selectedProjects.length > 0">
      <q-item tag="label">
        <q-item-section>
          <q-select v-model="selectedGroupsModel" :options="groups" option-value="id" for="id" option-label="title" label="Groups" stack-label borderless dense options-dense options-cover :multiple="multiTimeseries">
            <template v-if="selectedGroups.length > 0" v-slot:append>
              <q-icon name="cancel" @click.stop="selectedGroupsModel = null" class="cursor-pointer" />
            </template>
          </q-select>            
        </q-item-section>
      </q-item>

      <template v-if="selectedGroups.length > 0">
        <q-item tag="label" >
          <q-item-section>
            <q-select v-model="selectedPlotsModel" :options="plots" option-value="id" for="id" option-label="id" label="Plots" stack-label borderless dense options-dense options-cover :multiple="multiTimeseries">
              <template v-if="selectedPlots.length > 0" v-slot:append>
                <q-icon name="cancel" @click.stop="selectedPlotsModel = null" class="cursor-pointer" />
              </template>
            </q-select>  
          </q-item-section>
        </q-item>

        <template v-if="selectedPlots.length > 0">
          <q-item tag="label" >
            <q-item-section>
              <q-select v-model="selectedSensorsModel" :options="sensors" option-value="id" for="id" option-label="id" label="Sensors" stack-label borderless dense options-dense options-cover :multiple="multiTimeseries">
                <template v-if="selectedSensors.length > 0" v-slot:append>
                  <q-icon name="cancel" @click.stop="selectedSensorsModel = null" class="cursor-pointer" />
                </template>
              </q-select>                
            </q-item-section>
          </q-item>

          <template v-if="selectedSensors.length > 0">
            
          </template>
          <div v-else>
            no sensor selected
          </div>

        </template>
        <div v-else>
          no group selected
        </div>
      </template>
      <div v-else>
        no group selected
      </div>
    </template>             
    <div v-else>
      no project selected
    </div>
  </q-list>         
</q-item-section>
</template>

<script>
import { mapState, mapGetters } from 'vuex';

export default {
  name: 'timeseries-selector',
  props: [
    'multiTimeseries',
  ],
  data () {
    return {
      selectedProjectsModel: null,
      selectedGroupsModel: null,
      selectedPlotsModel: null,
      selectedSensorsModel: null,
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
    selectedProjects() {
      if(this.selectedProjectsModel === null) {
        return [];
      }
      if(this.multiTimeseries) {
        return this.selectedProjectsModel;
      } else {
        return [this.selectedProjectsModel];
      }
    },
    selectedGroups() {
      if(this.selectedGroupsModel === null) {
        return [];
      }
      if(this.multiTimeseries) {
        return this.selectedGroupsModel;
      } else {
        return [this.selectedGroupsModel];
      }
    },
    selectedPlots() {
      if(this.selectedPlotsModel === null) {
        return [];
      }
      if(this.multiTimeseries) {
        return this.selectedPlotsModel;
      } else {
        return [this.selectedPlotsModel];
      }
    },
    selectedSensors() {
      if(this.selectedSensorsModel === null) {
        return [];
      }
      if(this.multiTimeseries) {
        return this.selectedSensorsModel;
      } else {
        return [this.selectedSensorsModel];
      }
    },
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
      console.log("a");
      if(this.model === undefined || this.selectedGroups.length === 0) {
        return [];
      }
      var plotNameSet = new Set();
      for (let group of this.selectedGroups) {
        for(let plotName of group.plots) {
          plotNameSet.add(plotName);
        }
      }
      let result = [...plotNameSet].map(plotName => this.model.plots[plotName]);
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
      console.log("b");
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
      console.log(result);
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