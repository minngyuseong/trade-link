export type Stock = {
  code: string
  name: string
  shortName: string
  price: number
  changeRate: number
  logoColor: string
}

export type PlaceOrderRequest = {
  stockCode: string
  side: 'BUY' | 'SELL'
  quantity: number
  price: number
  userId: string
  idempotencyKey: string
}

export type OrderStatus = 'PENDING' | 'ACCEPTED' | 'PARTIAL' | 'MATCHED' | 'SETTLED'

export type OrderResult = {
  id: number
  orderId: string
  userId: number
  accountNo: string
  stockCode: string
  side: 'BUY' | 'SELL'
  quantity: number
  price: number
  status: OrderStatus
  exchangeOrderId: number | null
  tradeId: number | null
}

export type Holding = {
  stock: Stock
  quantity: number
  averagePrice: number
  purchaseAmount: number
  evaluationAmount: number
  profitLoss: number
  profitRate: number
}

export type Account = {
  userId: string
  cashBalance: number
  stockEvaluationAmount: number
  totalAsset: number
  totalProfitLoss: number
  totalProfitRate: number
  holdings: Holding[]
}
