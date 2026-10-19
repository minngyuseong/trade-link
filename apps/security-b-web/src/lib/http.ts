import axios from 'axios'

import { brand } from '@/config/brand'

export const http = axios.create({
  baseURL: brand.apiBaseUrl,
  timeout: 5_000,
  headers: {
    'Content-Type': 'application/json',
    'X-User-Id': brand.userId,
  },
})
