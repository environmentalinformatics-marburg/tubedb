import { createStore } from 'vuex'
import axios from 'axios'

import model from './model.js'

const isDev = process.env.DEV;

export default function (/* { ssrContext } */) {
  const Store = createStore({
    modules: {
      model,
    },
    getters: {
      api: (state) => (...parts) => {
        const path = parts.join('/');
        return isDev ? ('http://127.0.0.1:8080/' + path) : ('../../' + path);
      },
      apiGET: (state, getters) => (parts, config) => {
        //console.log(parts);
        const path = getters.api(...parts);
        //console.log(path);
        //console.log(Vue.prototype.$axios);
        return axios.get(path, config);
      },
      apiPOST: (state, getters) => (parts, data, config) => {
        const path = getters.api(...parts);
        return axios.post(path, data, config);
      },      
    },
    strict: process.env.DEBUGGING
  })

  return Store
}