<template>

<q-toolbar class="title fit row wrap justify-between">       
  <pages-menu :active="active" /> 
  <div class="text-h5">{{title}}</div>
  <div></div>
</q-toolbar> 

</template>

<script>

import pagesMenu from 'components/pages-menu.vue';

export default {
  name: 'pages-toolbar',
  props: [
    'title',
    'active',
  ],
  components: {
    pagesMenu,
  },  
  data () {
    return {
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

</style>