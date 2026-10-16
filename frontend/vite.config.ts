import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // En desarrollo la SPA habla con Spring a través del proxy: mismo origen,
      // así la cookie httpOnly de sesión viaja sin configurar CORS.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: './src/prueba/setup.ts',
    globals: true,
  },
})
