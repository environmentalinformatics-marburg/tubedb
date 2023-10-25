<template>
<q-btn flat round dense icon="menu">
  <q-menu transition-show="scale" transition-hide="scale"  class="bg-grey-1 text-grey-8">
    <q-list style="min-width: 100px">
      <q-item v-for="item in items" :key="item.title" :to="item.link" clickable :class="{activeitem: active === item.link}">
        <q-item-section>
          {{item.title}}
        </q-item-section>
      </q-item>
    </q-list>
  </q-menu>
</q-btn>
</template>

<script>

export default {
  name: 'pages-menu',
  props: [
    'active',
  ],
  data () {
    return {
      items: [
        {title: 'Overview', link: '/overview'},
        {title: 'Metadata', link: '/model'},
        {title: 'Diagrams', link: '/diagram'},
        {title: 'Monitoring', link: '/monitoring'},
        {title: 'Plot status', link: '/plot_status'},
      ],
    }
  },
  computed: {
  },
  methods: {
  },
  watch: {
  },
  async mounted() {
  },
};
</script>


<style scoped>

.activeitem {
  background-color: #7373731f;
  color: #313131;
  font-weight: bold;
  border-style: dotted;
  border-color: #0000002e;
  border-width: 2px;
}

</style>
