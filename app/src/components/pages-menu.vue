<template>
<q-btn color="primary" round icon="menu">
  <q-menu transition-show="scale" transition-hide="scale">
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
  background-color: #0202021f;
}

</style>