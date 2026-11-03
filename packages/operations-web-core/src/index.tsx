import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

import { OperationsApp } from './operations-app'
import './styles.css'

export type OperationsAppConfig = {
  kind: 'exchange' | 'depository'
  apiBaseUrl: string
}

export function renderOperationsApp(root: HTMLElement, config: OperationsAppConfig) {
  createRoot(root).render(
    <StrictMode>
      <OperationsApp config={config} />
    </StrictMode>,
  )
}
