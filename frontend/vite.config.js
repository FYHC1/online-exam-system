import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import { execSync } from 'child_process'

function getApiTarget() {
  if (process.env.VITE_API_PROXY_TARGET) {
    return process.env.VITE_API_PROXY_TARGET
  }

  if (process.platform === 'linux' && process.env.WSL_DISTRO_NAME) {
    try {
      const windowsHost = execSync("ip route | awk '/default/ {print $3; exit}'", {
        encoding: 'utf8'
      }).trim()

      if (windowsHost) {
        return `http://${windowsHost}:8080`
      }
    } catch {
      // Fall back to localhost when the WSL gateway cannot be detected.
    }
  }

  return 'http://localhost:8080'
}

const apiTarget = getApiTarget()

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: apiTarget,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  }
})
