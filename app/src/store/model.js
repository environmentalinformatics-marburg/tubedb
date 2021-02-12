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
      console.log(data);
      for(let plot of Object.values(data.plots)) {
        plot.sensorSet = new Set(plot.sensors);
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
        var response = await rootGetters.apiGET(['tsdb', 'model']);
        commit('setData', response.data.model);
      } catch {
        commit('setError', 'error');
      }
    },
  },
}