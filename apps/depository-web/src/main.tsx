import { renderOperationsApp } from '@trade-link/operations-web-core'

const root = document.getElementById('root')
if (!root) throw new Error('Root element was not found.')

renderOperationsApp(root, {
  kind: 'depository',
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8084',
})
