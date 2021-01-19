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
      <div class="property-grid">
        <template v-if="sensor.physical_range !== undefined">
          <div>physical_range</div> 
          <div>{{sensor.physical_range[0]}} .. {{sensor.physical_range[1]}}</div>
        </template>
        <template v-if="sensor.step_range !== undefined">
          <div>step_range</div> 
          <div>{{sensor.step_range[0]}} .. {{sensor.step_range[1]}}</div>
        </template>
        <template v-if="sensor.raw_func !== undefined">
          <div>raw_func</div> 
          <div>{{sensor.raw_func}}</div>
          <template v-if="sensor.raw_func_parsed !== undefined">
            <div>--> parsed</div> 
            <div>{{sensor.raw_func_parsed}}</div>
            <!--<div> tree</div>
            <div><formula-tree :node="sensor.raw_func_tree" :level="0"/></div>-->
            <div>--> print</div>  
            <formula-print :node="sensor.raw_func_print" :level="0"/>              
          </template>          
        </template> 
      </div>
    </div>
    <div class="column processing-node" v-if="sensor.aggregation_hour !== 'none'">
      <span>
        <span class="text-h6">Hour</span> 
        <span class="text-subtitle1"> 
          <span class="text-grey-5"> from raw by </span> 
          <span class="text-weight-medium"> {{sensor.aggregation_hour}}</span>
        </span>
      </span>
      <div class="property-grid">
        <template v-if="sensor.empirical_diff !== undefined">
          <div>empirical_diff_range</div> 
          <div>{{sensor.empirical_diff}}</div>
        </template>
        <template v-if="sensor.interpolation_mse !== undefined">
          <div>interpolation_mse</div> 
          <div>{{sensor.interpolation_mse}}</div>
        </template>
        <template v-if="sensor.post_hour_func !== undefined">
          <div>post_hour_func</div> 
          <div>{{sensor.post_hour_func}}</div>
          <template v-if="sensor.post_hour_func_parsed !== undefined">
            <div>--> parsed</div> 
            <div>{{sensor.post_hour_func_parsed}}</div>
            <!--<div> tree</div>
            <div><formula-tree :node="sensor.post_hour_func_tree" :level="0"/></div>-->   
            <div>--> print</div>  
            <formula-print :node="sensor.post_hour_func_print" :level="0"/>          
          </template>           
        </template> 
      </div>
    </div>
    <div v-if="sensor.aggregation_hour === 'none'" class="text-grey-5" style="margin: 20px;">
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
      <div class="property-grid">
        <template v-if="sensor.post_day_func !== undefined">
          <div>post_day_func</div> 
          <div>{{sensor.post_day_func}}</div>
          <template v-if="sensor.post_day_func_parsed !== undefined">
            <div>--> parsed</div> 
            <div>{{sensor.post_day_func_parsed}}</div>
            <!--<div> tree</div>
            <div><formula-tree :node="sensor.post_day_func_tree" :level="0"/></div>-->
            <div>--> print</div>  
            <formula-print :node="sensor.post_day_func_print" :level="0"/>        
          </template>           
        </template>
      </div>
    </div>
    <div v-if="sensor.aggregation_hour !== 'none' && sensor.aggregation_day === 'none'" class="text-grey-5" style="margin: 20px;">
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
      <div class="property-grid">
      </div>      
    </div>
    <div v-if="sensor.aggregation_hour !== 'none' && sensor.aggregation_day !== 'none' && sensor.aggregation_week === 'none'" class="text-grey-5" style="margin: 20px;">
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
      <div class="property-grid">
      </div>       
    </div>
    <div v-if="sensor.aggregation_hour !== 'none' && sensor.aggregation_day !== 'none' && sensor.aggregation_month === 'none'" class="text-grey-5" style="margin: 20px;">
      (no further aggregation defined)
    </div>

    <div class="column processing-node" v-if="sensor.aggregation_hour !== 'none' && sensor.aggregation_day !== 'none' && sensor.aggregation_month !== 'none' && sensor.aggregation_year !== 'none'">
      <span>
        <span class="text-h6">Year</span> 
        <span class="text-subtitle1"> 
          <span class="text-grey-5"> from month by </span> 
          <span class="text-weight-medium"> {{sensor.aggregation_year}}</span>
        </span>
      <div class="property-grid">
      </div>         
      </span>
    </div>
        <div v-if="sensor.aggregation_hour !== 'none' && sensor.aggregation_day !== 'none' && sensor.aggregation_month !== 'none' && sensor.aggregation_year === 'none'" class="text-grey-5" style="margin: 20px;">
      (no further aggregation defined)
    </div>

  </q-page>
</template>

<script>
import { mapState } from 'vuex'

//import formulaTree from 'components/formula-tree.vue'
import formulaPrint from 'components/formula-print.vue'


export default {
  name: 'sensor',
  components: {
    //formulaTree,
    formulaPrint,
  },
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

.property-grid {
  display: grid;
  grid-template-columns: max-content max-content;
  gap: 10px 20px;
  margin: 10px;
}

</style>

