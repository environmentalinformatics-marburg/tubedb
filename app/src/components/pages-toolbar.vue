<template>

<q-toolbar class="bg-grey-1 text-grey-8">
  <pages-menu :active="active" />
  <q-space></q-space>
  <q-toolbar-title class="title">
    {{title}}
  </q-toolbar-title>
  <q-space></q-space>
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
  color: #3a3a3ab5 !important;
  text-shadow: 0 0 5px #8c8c8c3f, 0 0 10px #4a4a4a46, 0 0 20px #ffffff1f, 0 0 30px #fff3, 0 0 40px #ffffff38, 0 0 55px #ffffff42, 0 0 70px #fff3;
  transition: 5s;
  user-select: none;
}

.title:hover {
  text-shadow: 0 0 5px rgba(0, 0, 0, 0.233), 0 0 10px rgba(0, 0, 0, 0.267), 0 0 20px #ffffff3d, 0 0 30px rgba(255, 255, 255, 0.205), 0 0 40px #ffffff44, 0 0 55px #ffffff41, 0 0 70px rgba(255, 255, 255, 0.212);
  user-select: none;
}

</style>
