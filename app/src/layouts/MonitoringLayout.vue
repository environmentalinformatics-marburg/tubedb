<template>
  <q-layout view="hHh LpR fFf">

    <q-header reveal elevated class="bg-grey-7 text-grey-4">
      <pages-toolbar title="TubeDB Monitoring" active="/monitoring"/>
    </q-header>

    <q-page-container class="row justify-center">
      <q-page padding class="column justify-center">
        <q-btn @click="refresh">refresh</q-btn>
        <q-table         
          dense
          :columns="columns"
          :rows="rows"
          row-key="plot"                
        />

      </q-page>
    </q-page-container>

  </q-layout>
</template>

<script>

import { mapGetters } from 'vuex';

import pagesToolbar from 'components/pages-toolbar.vue';

export default {
  components: {
    pagesToolbar,
  },
  data () {
    return {
      data: undefined,
    }
  },
  computed: {
    ...mapGetters({
      apiGET: 'apiGET',
    }),
    columns() {
      let result = [{
        name: 'plot',
        field: 'plot',
        label: 'Plot',
      }];
      if(this.data !== undefined) {
        this.data.sensors.forEach(sensor => {
          const timename = sensor + '.datetime';
          result.push({
            name: timename,
            field: timename,
            label: 'datetime',
          });
          result.push({
            name: sensor,
            field: sensor,
            label: sensor,
          });
        });
      }
      return result;
    }, 
    rows() {
      if(this.data === undefined) {
        return [];
      }
      const sensors = this.data.sensors;
      let result = this.data.measurements.map(e => {
        let row = {
          plot: e.plot,
        };
        const datetimes = e.datetime;
        const values = e.value;
        for (let i = 0; i < sensors.length; i++) {
          const sensor = sensors[i];
          const timename = sensor + '.datetime';
          row[timename] = datetimes[i];
          row[sensor] = values[i];
        }     
        return row;
      });
      return result;
    },       
  },
  methods: {
    async refresh() {
      try {
        const params = new URLSearchParams();
        params.append('plot', 'BALE001');
        params.append('plot', 'BALE002');
        params.append('plot', 'BALE003');
        params.append('sensor', 'Ta_200');
        params.append('sensor', 'rH_200');
        const response = await this.apiGET(['tsdb', 'monitoring'], {params});
        this.data = response.data;
      } catch(e) {
        this.data = undefined;
        console.log(e);
        this.$q.notify({message: 'Error loading data.', type: 'negative'});
      }
    },
  },
  watch: {
  },
  async mounted() {
  },
}
</script>

<style scoped>

.overview-item {
  margin-bottom: 20px;
}

</style>
