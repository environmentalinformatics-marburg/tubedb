<template>
  <q-layout view="hHh LpR fFf">

    <q-header reveal elevated class="bg-grey-7 text-grey-4">
      <pages-toolbar title="TubeDB Metadata" active="/model"/>
    </q-header>

    <q-drawer show-if-above side="left" behavior="desktop" content-class="bg-grey-4" v-if="model !== undefined" :width="drawerWidth">    
      <div class="fit row">
      <q-scroll-area class="col-grow">  
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
      </q-scroll-area>
      <div v-touch-pan.prevent.mouse="onChangeDrawerWidth" class="drawerChanger">
      </div>
      </div> 
    </q-drawer>

    <q-page-container v-if="model !== undefined">
      <router-view />
    </q-page-container>

    <q-page-container v-if="modelLoading">
      Loading model ...
    </q-page-container>
    <q-page-container v-else-if="modelError !== undefined">
      Error Loading model.
    </q-page-container>

  </q-layout>
</template>

<script>

import { mapState } from 'vuex'

import pagesToolbar from 'components/pages-toolbar.vue';

export default {
  components: {
    pagesToolbar,
  },  
  data() {
    return {
      drawerWidth: 400,
    }
  },
  computed: {
    ...mapState({
      model: state => state.model.data,  
      modelLoading: state => state.model.loading,
      modelError: state => state.model.error,    
    }),
    sensor_id() {
      return this.$route.params.sensor_id;
    },
  },
  methods: {
    onChangeDrawerWidth(e) {
      const delta = e.delta.x;
      this.drawerWidth += delta;
      if(this.drawerWidth < 30) {
        this.drawerWidth = 30;
      }
      if(this.drawerWidth > 800) {
        this.drawerWidth = 800;
      }
    },
  },
  async mounted() {
    this.$store.dispatch('model/init');
  },
}
</script>

<style scoped>

.title {
  background: radial-gradient( circle farthest-corner at center center, #757575, #a4a2a2 ) no-repeat;
  color: #e0e0e0b5 !important;
  text-shadow: 0 0 5px #8c8c8c, 0 0 10px #4a4a4a, 0 0 20px #ffffff1f, 0 0 30px #fff3, 0 0 40px #ffffff38, 0 0 55px #ffffff42, 0 0 70px #fff3;
  transition: 5s;
  user-select: none;
}

.title:hover {
  background: radial-gradient( circle farthest-corner at center center, #757575, #a4a2a2 ) no-repeat;
  text-shadow: 0 0 5px #000, 0 0 10px #000, 0 0 20px #ffffff8a, 0 0 30px rgba(255, 255, 255, 0.514), 0 0 40px #ffffff91, 0 0 55px #ffffff8a, 0 0 70px rgba(255, 255, 255, 0.582);
  user-select: none;
}

.drawerChanger {
  display: flex;
  align-items: center;
  cursor: col-resize;
  width: 6px;
  justify-content: space-between;
  background-color: rgba(0, 0, 0, 0.08);
  background: linear-gradient(to right, rgba(0, 0, 0, 0.06),rgba(255, 255, 255, 0.78),rgba(0, 0, 0, 0.24));
}

.drawerChanger::before, .drawerChanger::after {
  width: 1px;
  height: 60px;
  content: "";
}

.drawerChanger::before {
  background-color: #00000057;
  margin-left: 1px;
}

.drawerChanger::after {
  background-color: #0000008f;
  margin-right: 1px;
}

</style>
