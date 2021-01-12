<template>
  <q-page class="flex flex-center">
    SENSOR <b>{{id}}</b>
    <br>
    <div>{{model.sensors[id]}}</div>
  </q-page>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'sensor',
  props: ['id'],
  computed: {
    ...mapState({
      model: state => state.model.data,  
    }),
  },
  
  async mounted() {
    this.$store.dispatch('model/init');
  },  
}
</script>
