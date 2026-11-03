export type ExchangeOrder = {
  exchangeOrderId: number
  userId: number
  brokerCode: string
  brokerOrderId: number
  stockCode: string
  side: 'BUY' | 'SELL'
  price: number
  quantity: number
  status: 'WAITING' | 'MATCHED' | string
  matched: boolean
  createdAt: string
}

export type Trade = {
  tradeId: number
  stockCode: string
  price: number
  quantity: number
  buyBrokerCode: string
  buyBrokerOrderId: number
  sellBrokerCode: string
  sellBrokerOrderId: number
  createdAt: string
}

export type Settlement = {
  tradeId: number
  stockCode: string
  price: number
  quantity: number
  buyBrokerCode: string
  buyBrokerOrderId: number
  sellBrokerCode: string
  sellBrokerOrderId: number
  status: string
  settledAt: string
}

export type Ledger = {
  cash: Array<{ brokerCode: string; amount: number }>
  stocks: Array<{ brokerCode: string; stockCode: string; quantity: number }>
}
