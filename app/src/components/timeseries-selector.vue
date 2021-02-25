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
            <q-select v-model="selectedPlotsModel" :options="plots" label="Plots" stack-label borderless dense options-dense options-cover :multiple="multiTimeseries">
              <template v-if="selectedPlots.length > 0" v-slot:append>
                <q-icon name="cancel" @click.stop="selectedPlotsModel = null" class="cursor-pointer" />
              </template>
            </q-select>
            <q-item-label caption v-if="plotstations.length === 1 && selectedPlots.length === 1  && plotstations[0].id !== selectedPlots[0].id">
              {{plotstations[0].id}}
            </q-item-label>  
          </q-item-section>
        </q-item>

        <template v-if="selectedPlots.length > 0">
          <q-item tag="label" v-if="plotstations.length > 1 && selectedPlotsHaveMultipleStations">
            <q-item-section>
              <q-select v-model="selectedPlotstationsModel" :options="plotstations" option-value="id" for="id" option-label="id" label="Plot-Stations" stack-label borderless dense options-dense options-cover :multiple="multiTimeseries">
                <template v-if="selectedPlotstationsModel !== null" v-slot:append>
                  <q-icon name="cancel" @click.stop="selectedPlotstationsModel = null" class="cursor-pointer" />
                </template>
                <template v-slot:option="{itemProps, itemEvents, opt}">
                  <q-item v-bind="itemProps" v-on="itemEvents">
                    <q-item-section>
                      <q-item-label v-if="opt.merged"><b>{{opt.plot}}</b> (merged)</q-item-label>
                      <q-item-label v-else-if="opt.plot === opt.station"><b>{{opt.plot}}</b></q-item-label>
                      <q-item-label v-else>{{opt.plot}}&nbsp;&nbsp;&nbsp;<b>{{opt.station}}</b></q-item-label>
                    </q-item-section>
                  </q-item>
                </template>                
              </q-select>                
            </q-item-section>
          </q-item>

          <template v-if="selectedPlotstations.length > 0">
            
            <q-item tag="label" >
              <q-item-section v-if="sensors.length > 0">
                <q-select v-model="selectedSensorsModel" :options="sensors" option-value="id" for="id" option-label="id" label="Sensors" stack-label borderless dense options-dense options-cover :multiple="multiTimeseries">
                  <template v-if="selectedSensors.length > 0" v-slot:append>
                    <q-icon name="cancel" @click.stop="selectedSensorsModel = null" class="cursor-pointer" />
                  </template>
                </q-select>                
              </q-item-section>
              <q-item-section v-if="sensors.length === 0">
                no sensors              
              </q-item-section>
            </q-item>

            <template v-if="selectedSensors.length > 0">
            
            </template>
            <div v-else>
              no sensor selected
            </div>

          </template>
          <div v-else>
            no plot-station selected
          </div>         

        </template>
        <div v-else>
          no plot selected
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
      selectedPlotstationsModel: null,
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
        return this.selectedPlotsModel.map(plotName => this.model.plots[plotName]);
      } else {
        return [this.model.plots[this.selectedPlotsModel]];
      }
    },
    selectedPlotstations() {
      if(this.selectedPlotsHaveMultipleStations) {
        if(this.selectedPlotstationsModel === null) {
          if(this.plotstations.length === 0) {
            return [];
          } else {
            return [this.plotstations[0]];
          }
        }
        if(this.multiTimeseries) {
          return this.selectedPlotstationsModel;
        } else {
          return [this.selectedPlotstationsModel];
        }
      } else {
        return this.selectedPlots.map(plot => this.plotstations.find(plotstation => plotstation.plot === plot.id));
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
      if(this.model === undefined || this.selectedGroups.length === 0) {
        return [];
      }
      var plotNameSet = new Set();
      for (let group of this.selectedGroups) {
        for(let plotName of group.plots) {
          plotNameSet.add(plotName);
        }
      }
      //console.log(plotNameSet);
      /*let result = [...plotNameSet].map(plotName => {
        let p = this.model.plots[plotName];
        return {id: p.id};
      });*/
      let result = [...plotNameSet];
      /*result = result.filter(plot => {
        //console.log(plot.id);
        return plot !== undefined;
      });*/
      result.sort((a, b) => {
        var nameA = a.toLowerCase();
        var nameB = b.toLowerCase();
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
    selectedPlotsHaveMultipleStations() {
      return this.selectedPlots.some(plot => plot.stations.length > 1);
    },
    plotstations() {
      /*var ps = [];
      for (let plot of this.selectedPlots) {
        switch(plot.stations.length) {
          case 0: {
            let id = plot.id;
            let sensorSet = plot.sensorSet;
            ps.push({id: id, plot: plot.id, station: plot.id, sensorSet: sensorSet});
            break;
          }
          case 1: {
            let stationName = plot.stations[0];
            let id = plot.id + ":" + stationName;
            let sensorSet = this.model.stations[stationName].sensorSet;
            ps.push({id: id, plot: plot.id, station: stationName, sensorSet: sensorSet});
            break;
          }
          default: {
            ps.push({id: plot.id, plot: plot.id, merged: true, sensorSet: plot.sensorSet});
            for(let stationName of plot.stations) {
              let id = plot.id + ":" + stationName;
              let sensorSet = this.model.stations[stationName].sensorSet;
              ps.push({id: id, plot: plot.id, station: stationName, sensorSet: sensorSet});
            }
          }
        }
      }
      return ps;*/
      /*var ps = [];
      for (let plot of this.selectedPlots) {
        ps = ps.concat(plot.plotstations);
      }
      return ps;*/
      return this.selectedPlots.flatMap(plot => plot.plotstations);
    },
    sensors() {
      if(this.model === undefined || this.selectedPlotstations === undefined || this.selectedPlotstations.length === 0) {
        return [];
      }
      var sensorNames = new Set();
      for (let plotstation of this.selectedPlotstations) {
        for(let sensorName of plotstation.sensorSet) {
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
      this.$emit('plot-sensor-changed', {plots: this.selectedPlotstations, sensors: this.selectedSensors});
    },
  },
  watch: {
    projects: {
      handler() {
        this.selectedProjectsModel = null;
        console.log("A");
        if(this.projects.length === 1) {
          console.log("A");
          if(this.multiTimeseries) {
            this.selectedProjectsModel = [this.projects[0]];
          } else {
            this.selectedProjectsModel = this.projects[0];
          }
        }
      },
      immediate: true,      
    },
    groups: {
      handler() {
        this.selectedGroupsModel = null;
        if(this.groups.length === 1) {
          if(this.multiTimeseries) {
            this.selectedGroupsModel = [this.groups[0]];
          } else {
            this.selectedGroupsModel = this.groups[0];
          }
        }
      },
      immediate: true,  
    },
    plots: {
      handler(plots) {
        this.selectedPlotsModel = null;
        if(plots.length === 1) {
          if(this.multiTimeseries) {
            this.selectedPlotsModel = [plots[0]];
          } else {
            this.selectedPlotsModel = plots[0];
          }
        }
      },
      immediate: true, 
    },
    /*plotstations: {
      handler() {
        this.selectedPlotstationsModel = null;
        if(this.plotstations.length === 1) {
          if(this.multiTimeseries) {
            this.selectedPlotstationsModel = [this.plotstations[0]];
          } else {
            this.selectedPlotstationsModel = this.plotstations[0];
          }
        }
      },
      immediate: true, 
    },*/
    sensors: {
      handler() {
        if(this.sensors.length === 0) {
          this.selectedSensorsModel = null;
        } else if(this.sensors.length === 1) {
          if(this.multiTimeseries) {
            this.selectedSensorsModel = [this.sensors[0]];
          } else {
            this.selectedSensorsModel = this.sensors[0];
          }
        } else if(this.selectedSensorsModel !== null) {
          if(this.multiTimeseries) {
            this.selectedSensorsModel = this.selectedSensorsModel.filter(selectedSensor => this.sensors.some(sensor => sensor.id === selectedSensor.id));
            if(this.selectedSensorsModel.length === 0) {
              this.selectedSensorsModel = null;
            }
          } else {
            this.selectedSensorsModel = this.sensors.some(sensor => sensor.id === this.selectedSensorsModel.id) ? this.selectedSensorsModel : null;
          }
        }
      },
      immediate: true,
    },
    selectedPlots: {
      handler(selectedPlots, oldSelectedPlots) {
        let stayingSelectedPlots = selectedPlots.filter(selectedPlot => oldSelectedPlots === undefined || oldSelectedPlots.some(oldSelectedPlot => selectedPlot.id === oldSelectedPlot.id));        
        console.log("stayingSelectedPlots: " + JSON.stringify(stayingSelectedPlots.map(stayingSelectedPlot => stayingSelectedPlot.id)));
        let newSelectedPlots = selectedPlots.filter(selectedPlot => oldSelectedPlots === undefined || !oldSelectedPlots.some(oldSelectedPlot => selectedPlot.id === oldSelectedPlot.id));
        console.log("newSelectedPlots: " + JSON.stringify(newSelectedPlots.map(newSelectedPlot => newSelectedPlot.id)));
        console.log("selectedPlotStations: " + JSON.stringify(this.selectedPlotStations === undefined || this.selectedPlotStations.map(selectedPlotStation => selectedPlotStation.id)));
        console.log("selectedPlots: " + JSON.stringify(selectedPlots.map(selectedPlot => selectedPlot.id)));
        console.log("oldSelectedPlots: " + JSON.stringify(oldSelectedPlots === undefined || oldSelectedPlots.map(oldSelectedPlot => oldSelectedPlot.id)));
        if(this.selectedPlotstationsModel !== null) {
          if(this.multiTimeseries) {
            newSelectedPlots = newSelectedPlots.filter(newSelectedPlot => !this.selectedPlotstationsModel.some(selectedPlotstation => selectedPlotstation.plot === newSelectedPlot.id));
          } else {
            newSelectedPlots = newSelectedPlots.filter(newSelectedPlot => this.selectedPlotstationsModel.plot !== newSelectedPlot.id);
          }
        }
        let newSelectedPlotStations = newSelectedPlots.map(newSelectedPlot => this.plotstations.find(plotstation => plotstation.plot === newSelectedPlot.id));
        console.log("newSelectedPlotStations: " + JSON.stringify(newSelectedPlotStations.map(newSelectedPlotStation => newSelectedPlotStation.id)));
        let currSelectedPlotStations = [];        
        if(this.selectedPlotstationsModel !== null) {
          if(this.multiTimeseries) {          
            currSelectedPlotStations = this.selectedPlotstationsModel.filter(selectedPlotstation => stayingSelectedPlots.some(stayingSelectedPlot => selectedPlotstation.plot === stayingSelectedPlot.id));
          } else {
            if(stayingSelectedPlots.some(stayingSelectedPlot => this.selectedPlotstationsModel.plot === stayingSelectedPlot.id)) {
              currSelectedPlotStations = [this.selectedPlotstationsModel];
            }
          }
        }  
        console.log("currSelectedPlotStations: " + JSON.stringify(currSelectedPlotStations.map(currSelectedPlotStation => currSelectedPlotStation.id)));      
        currSelectedPlotStations = currSelectedPlotStations.concat(newSelectedPlotStations);
        console.log("currSelectedPlotStations: " + JSON.stringify(currSelectedPlotStations.map(currSelectedPlotStation => currSelectedPlotStation.id)));        
        if(currSelectedPlotStations.length > 0) {
          if(this.multiTimeseries) {
            this.selectedPlotstationsModel = currSelectedPlotStations;
          } else {
            this.selectedPlotstationsModel = currSelectedPlotStations[0];
          }        
        } else {
          this.selectedPlotstationsModel = null;
        }
        //this.selectedPlotstationsModel = null;
      },
      immediate: true,
    },
    selectedPlotstations() {
      console.log("selectedPlotstations changed");
      this.$nextTick(() => this.onPlotSensorChanged());
    },    
    selectedSensors() {      
      this.$nextTick(() => this.onPlotSensorChanged());
    }
  },  
  async mounted() {
    this.$store.dispatch('model/init');
  },  
};
</script>

<style scoped>



</style>