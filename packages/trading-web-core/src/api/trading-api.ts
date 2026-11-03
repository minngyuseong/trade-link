import { http } from '../lib/http'
import { getBrandConfig } from '../config/brand'
import type { Account, Holding, OrderResult, PlaceOrderRequest, Stock } from '../types/trading'

async function getStocks() {
  const { data } = await http.get<Stock[]>('/api/stocks')
  return data
}

async function getStock(stockCode: string) {
  const { data } = await http.get<Stock>(`/api/stocks/${stockCode}`)
  return data
}

async function getAccount() {
  const { data } = await http.get<Account>('/api/account')
  return data
}

async function getHolding(stockCode: string) {
  const { data } = await http.get<Holding>(`/api/account/holdings/${stockCode}`)
  return data
}

async function placeOrder(request: PlaceOrderRequest) {
  const brand = getBrandConfig()
  const response = await http.post<OrderResult>('/api/orders', {
    ...request,
    userId: Number(request.userId) || 1,
    accountNo: brand.accountNo,
  }, {
    headers: { 'Idempotency-Key': request.idempotencyKey },
  })
  return response.data
}

async function getOrder(orderId: number) {
  const { data } = await http.get<OrderResult>(`/api/orders/${orderId}`)
  return data
}

async function getOrders() {
  const { data } = await http.get<OrderResult[]>('/api/account/orders')
  return data
}

export const tradingApi = { getStocks, getStock, getAccount, getHolding, placeOrder, getOrder, getOrders }
