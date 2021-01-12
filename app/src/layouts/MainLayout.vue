<template>
  <q-layout view="hHh LpR fFf">

    <q-header reveal elevated class="bg-grey-7 text-grey-4">
      <q-toolbar class="fit row wrap justify-center items-start content-start">       
        <div class="text-h5">TubeDB Metadata Explorer</div>
       </q-toolbar>
    </q-header>

    <q-drawer show-if-above side="left" behavior="desktop" elevated content-class="bg-grey-4" v-if="model !== undefined">    
        <q-expansion-item
          icon="explore"
          label="Sensors"
          header-class="text-primary"
          dense
          dense-toggle
          expand-separator
          :content-inset-level="1"
          default-opened
        >
          <div v-for="(sensor, id) in model.sensors" :key="id" @click="$router.push('/model/sensors/' + id);" :class="{'bg-grey-5': id === sensor_id}">
            {{id}}
          </div>
        </q-expansion-item>
  
    </q-drawer>

    <q-page-container v-if="model !== undefined">
      <router-view />
    </q-page-container>

    <q-page-container v-if="model === undefined">
      Loadaing model
    </q-page-container>

  </q-layout>
</template>

<script>

import { mapState } from 'vuex'

export default {
  data () {
    return {
    }
  },
  computed: {
    ...mapState({
      model: state => state.model.data,  
    }),
    sensor_id() {
      return this.$route.params.sensor_id;
    },
  },
  
  async mounted() {
    this.$store.dispatch('model/init');
  },
}
</script>