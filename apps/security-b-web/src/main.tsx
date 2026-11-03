import { renderTradingApp } from '@trade-link/trading-web-core'

import { brand } from './config/brand'

const root = document.getElementById('root')

if (!root) {
  throw new Error('Root element was not found.')
}

renderTradingApp(root, brand)
