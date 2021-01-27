
const routes = [
  {
    path: '/model',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '/', component: () => import('pages/index.vue') },
      { path: 'sensors/:sensor_id', component: () => import('pages/sensor.vue'), props: route => ({ id: route.params.sensor_id }), },
    ],
  },

  {
    path: '/diagram',
    component: () => import('layouts/DiagramLayout.vue'),
  },

  {
    path: '/',
    redirect: '/model',
  },

  {
    path: '*',
    component: () => import('pages/Error404.vue')
  },
]

export default routes
