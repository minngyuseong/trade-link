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
  quantity: number
  price: number
  userId: string
  idempotencyKey: string
}

export type OrderResult = {
  orderId: string
  idempotencyKey: string
  stockCode: string
  quantity: number
  price: number
  totalAmount: number
  status: 'ACCEPTED'
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
