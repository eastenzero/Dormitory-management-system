import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 15174,
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:18082',
        changeOrigin: true,
      },
    },
  },
  preview: {
    port: 15174,
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:18082',
        changeOrigin: true,
      },
    },
  },
})
