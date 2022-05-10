export default {
  namespaced: true,
  
  state: () => ({
    data: undefined,
    loading: false,
    error: undefined,
  }),
  
  getters: {
  },
  
  mutations: {
    setLoading (state) {
      state.loading = true;
      state.error = undefined;
    },
  
    setData (state, data) {
      state.loading = false;
      //console.log(data);
      for(let station of Object.values(data.stations)) {
        station.sensorSet = new Set(station.sensors);
      }
      for(let plot of Object.values(data.plots)) {
        plot.sensorSet = new Set(plot.sensors);
        let plotstations = [];
        switch(plot.stations.length) {
          case 0: {
            let id = plot.id;
            let sensorSet = plot.sensorSet;
            plotstations.push({id: id, plot: plot.id, station: plot.id, sensorSet: sensorSet});
            break;
          }
          case 1: {
            let stationName = plot.stations[0];
            let id = plot.id + ":" + stationName;
            let sensorSet = data.stations[stationName].sensorSet;
            plotstations.push({id: id, plot: plot.id, station: stationName, sensorSet: sensorSet});
            break;
          }
          default: {
            plotstations.push({id: plot.id, plot: plot.id, merged: true, sensorSet: plot.sensorSet});
            for(let stationName of plot.stations) {
              let id = plot.id + ":" + stationName;
              let sensorSet = data.stations[stationName].sensorSet;
              plotstations.push({id: id, plot: plot.id, station: stationName, sensorSet: sensorSet});
            }
          }
        }
        plot.plotstations = plotstations;
      }
      state.data = data;
    },
  
    setError (state, error) {
      state.loading = false;
      state.error = error;
    },
  },
    
  actions: {
    init ({ state, dispatch }) {
      if (state.data === undefined) {
        dispatch('refresh');
      }
    },
    async refresh ({ commit, rootGetters }) {
      commit('setLoading')
      try {
        const response = await rootGetters.apiGET(['tsdb', 'model']);
        commit('setData', response.data.model);
      } catch {
        commit('setError', 'error');
      }
    },
  },
}