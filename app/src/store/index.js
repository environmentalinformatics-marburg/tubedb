import Vue from 'vue'
import Vuex from 'vuex'

import model from './model.js'

Vue.use(Vuex)

const isDev = process.env.DEV;

export default function (/* { ssrContext } */) {
  const Store = new Vuex.Store({
    strict: process.env.DEBUGGING,
    modules: {
      model,
    },
    getters: {
      api: (state) => (...parts) => {
        var path = parts.join('/');
        return isDev ? ('http://localhost:8080/' + path) : ('/' + path);
      },
      apiGET: (state, getters) => (parts, params) => {
        //console.log(parts);
        var path = getters.api(...parts);
        //console.log(path);
        //console.log(Vue.prototype.$axios);
        return Vue.prototype.$axios.get(path, params);
      },
    },    
  })

  return Store
}
