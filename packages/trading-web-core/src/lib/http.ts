import axios from 'axios'

import { getBrandConfig } from '../config/brand'

export const http = axios.create({
  timeout: 5_000,
  headers: {
    'Content-Type': 'application/json',
  },
})

http.interceptors.request.use((request) => {
  const brand = getBrandConfig()
  request.baseURL = brand.apiBaseUrl
  request.headers.set('X-User-Id', brand.userId)
  return request
})
