<template>
  <q-page class="column">
    <div class="self-center text-h4" style="margin-top: 20px;">
      {{id}}
    </div>
    <div class="column" style="margin: 20px;">
      <span class="text-subtitle1" v-if="sensor.description !== undefined && sensor.description !== 'no description'">{{sensor.description}}</span><span class="text-subtitle1 text-grey-5" v-else>no description</span>
      <span class="text-subtitle2" v-if="sensor.unit !== undefined && sensor.unit !== 'no unit'">{{sensor.unit}}</span><span class="text-subtitle2 text-grey-5" v-else>no unit</span>
      <span v-if="sensor.category !== undefined && sensor.category !== 'other'" class="text-grey-5"><span>category </span> <span> {{sensor.category}}</span></span>
      <span v-if="sensor.visibility !== undefined && sensor.visibility !== 'public'" class="text-grey-5"><span>visibility </span> <span> {{sensor.visibility}}</span></span>
      <span v-if="sensor.derived !== undefined && sensor.derived"><span class="text-grey-5">derived sensor</span></span>

      <span class="text-subtitle1" v-if="sensor.dependency !== undefined"> 
        <span>dependency </span>
        <span class="text-weight-medium"> <a v-for="name in sensor.dependency" :key="name" :href="'#/model/sensors/' + name" style="margin-left: 10px;">{{name}}</a></span> 
      </span>
  

    </div>
    <div class="column processing-node">
      <span>
        <span class="text-h6">Raw</span> 
        <span class="text-subtitle1"> 
          <span class="text-grey-5" v-if="sensor.raw_source === undefined"> from database </span>
          <span class="text-grey-5" v-if="sensor.raw_source !== undefined"> from </span>
          <span class="text-weight-medium" v-if="sensor.raw_source !== undefined"> <a v-for="name in sensor.raw_source" :key="name" :href="'#/model/sensors/' + name" style="margin-left: 10px;">{{name}}</a></span> 
        </span>
      </span>
      <span v-if="sensor.physical_range !== undefined"><span>physical_range </span> <span> {{sensor.physical_range[0]}} to {{sensor.physical_range[1]}}</span></span>
      <span v-if="sensor.step_range !== undefined"><span>step_range </span> <span> {{sensor.step_range[0]}} to {{sensor.step_range[1]}}</span></span>      
      <span v-if="sensor.raw_func !== undefined"><span>raw_func</span> <span> {{sensor.raw_func}}</span></span>
    </div>
    <div class="column processing-node" v-if="sensor.aggregation_hour !== 'none'">
      <span>
        <span class="text-h6">Hour</span> 
        <span class="text-subtitle1"> 
          <span class="text-grey-5"> from raw by </span> 
          <span class="text-weight-medium"> {{sensor.aggregation_hour}}</span>
        </span>
      </span>
      <span v-if="sensor.empirical_diff !== undefined"><span>empirical_diff_range </span> <span> {{sensor.empirical_diff}}</span></span>
      <span v-if="sensor.interpolation_mse !== undefined"><span>interpolation_mse </span> <span> {{sensor.interpolation_mse}}</span></span>
      <span v-if="sensor.post_hour_func !== undefined"><span>post_hour_func</span> <span> {{sensor.post_hour_func}}</span></span>
    </div>
    <div v-if="sensor.aggregation_hour === 'none'" style="margin: 20px;">
      (no further aggregation defined)
    </div>

    <div class="column processing-node" v-if="sensor.aggregation_hour !== 'none' && sensor.aggregation_day !== 'none'">
      <span>
        <span class="text-h6">Day</span> 
        <span class="text-subtitle1"> 
          <span class="text-grey-5"> from hour by </span> 
          <span class="text-weight-medium"> {{sensor.aggregation_day}}</span>
        </span>
      </span>
      <span v-if="sensor.post_day_func !== undefined"><span>post_day_func</span> <span> {{sensor.post_day_func}}</span></span>
    </div>
    <div v-if="sensor.aggregation_hour !== 'none' && sensor.aggregation_day === 'none'" style="margin: 20px;">
      (no further aggregation defined)
    </div>

    <div class="column processing-node" v-if="sensor.aggregation_hour !== 'none' && sensor.aggregation_day !== 'none' && sensor.aggregation_week !== 'none'">
      <span>
        <span class="text-h6">Week</span> 
        <span class="text-subtitle1"> 
          <span class="text-grey-5"> from day by </span> 
          <span class="text-weight-medium"> {{sensor.aggregation_week}}</span>
        </span>
      </span>
    </div>
    <div v-if="sensor.aggregation_hour !== 'none' && sensor.aggregation_day !== 'none' && sensor.aggregation_week === 'none'" style="margin: 20px;">
      (no further aggregation defined)
    </div>

    <div class="column processing-node" v-if="sensor.aggregation_hour !== 'none' && sensor.aggregation_day !== 'none' && sensor.aggregation_month !== 'none'">
      <span>
        <span class="text-h6">Month</span> 
        <span class="text-subtitle1"> 
          <span class="text-grey-5"> from day by </span> 
          <span class="text-weight-medium"> {{sensor.aggregation_month}}</span>
        </span>
      </span>
    </div>
    <div v-if="sensor.aggregation_hour !== 'none' && sensor.aggregation_day !== 'none' && sensor.aggregation_month === 'none'" style="margin: 20px;">
      (no further aggregation defined)
    </div>

    <div class="column processing-node" v-if="sensor.aggregation_hour !== 'none' && sensor.aggregation_day !== 'none' && sensor.aggregation_month !== 'none' && sensor.aggregation_year !== 'none'">
      <span>
        <span class="text-h6">Year</span> 
        <span class="text-subtitle1"> 
          <span class="text-grey-5"> from month by </span> 
          <span class="text-weight-medium"> {{sensor.aggregation_year}}</span>
        </span>
      </span>
    </div>
        <div v-if="sensor.aggregation_hour !== 'none' && sensor.aggregation_day !== 'none' && sensor.aggregation_month !== 'none' && sensor.aggregation_year === 'none'" style="margin: 20px;">
      (no further aggregation defined)
    </div>

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
    sensor() {
      return this.model === undefined || this.id === undefined || this.model.sensors[this.id] === undefined ? {} : this.model.sensors[this.id];
    }
  },
  
  async mounted() {
    this.$store.dispatch('model/init');
  },  
}
</script>

<style scoped>
.processing-node {
  margin: 20px;
  border: 2px solid #00000029;
  border-radius: 5px; 
}
</style>

