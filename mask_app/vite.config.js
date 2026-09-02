import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  base: '/content/mask_app/',
  build: {
    outDir: '../webcontent/mask_app',
    emptyOutDir: true,
    chunkSizeWarningLimit: 2000,
  },
  server: {
    proxy: {
      '/tsdb': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      }
    }
  }
})
