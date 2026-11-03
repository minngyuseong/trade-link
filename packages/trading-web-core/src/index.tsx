import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'

import { TradingApp } from './App'
import { configureTradingApp, type BrokerConfig } from './config/brand'
import './index.css'

export type { BrokerConfig } from './config/brand'

export function renderTradingApp(root: HTMLElement, config: BrokerConfig) {
  configureTradingApp(config)

  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 30_000,
        retry: 1,
      },
    },
  })

  createRoot(root).render(
    <StrictMode>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <TradingApp />
        </BrowserRouter>
      </QueryClientProvider>
    </StrictMode>,
  )
}
