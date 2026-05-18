import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 5173,
    host: '0.0.0.0', // 监听所有地址，方便局域网内以及设备调试
    open: true, // 启动时自动在浏览器中打开
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端接口地址，请根据实际应用端口进行调整
        changeOrigin: true, // 是否跨域
        rewrite: (path) => path.replace(/^\/api/, '') // 根据需要配置是否重写路径
      }
    }
  }
})
