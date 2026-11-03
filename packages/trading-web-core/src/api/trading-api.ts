import { getBrandConfig } from '../config/brand'
import { http } from '../lib/http'
import type { Account, Holding, OrderResult, PlaceOrderRequest, Stock } from '../types/trading'

const stocks: Stock[] = [
  {
    code: '000660',
    name: 'SK하이닉스',
    shortName: 'SK',
    price: 3_000_000,
    changeRate: 1.8,
    logoColor: '#e8344e',
  },
  {
    code: '005930',
    name: '삼성전자',
    shortName: 'S',
    price: 400_000,
    changeRate: -0.7,
    logoColor: '#1428a0',
  },
]

const mockOrders = new Map<string, OrderResult>()

const createHolding = (stock: Stock, quantity: number, averagePrice: number): Holding => {
  const purchaseAmount = quantity * averagePrice
  const evaluationAmount = quantity * stock.price
  const profitLoss = evaluationAmount - purchaseAmount

  return {
    stock,
    quantity,
    averagePrice,
    purchaseAmount,
    evaluationAmount,
    profitLoss,
    profitRate: (profitLoss / purchaseAmount) * 100,
  }
}

const mockHoldings = [
  createHolding(stocks[0], 2, 2_700_000),
  createHolding(stocks[1], 5, 420_000),
]

const createMockAccount = (): Account => ({
  userId: getBrandConfig().userId,
  cashBalance: 12_000_000,
  stockEvaluationAmount: mockHoldings.reduce((total, holding) => total + holding.evaluationAmount, 0),
  totalAsset: 20_000_000,
  totalProfitLoss: mockHoldings.reduce((total, holding) => total + holding.profitLoss, 0),
  totalProfitRate: 6.67,
  holdings: mockHoldings,
})

const delay = (milliseconds: number) =>
  new Promise((resolve) => window.setTimeout(resolve, milliseconds))

export class SimulatedNetworkLossError extends Error {
  constructor() {
    super('서버가 주문을 처리한 직후 응답이 유실되었습니다.')
    this.name = 'SimulatedNetworkLossError'
  }
}

async function getStocks() {
  const brand = getBrandConfig()
  if (!brand.useMockApi) {
    const { data } = await http.get<Stock[]>('/api/stocks')
    return data
  }

  await delay(850)
  return stocks
}

async function getStock(stockCode: string) {
  const brand = getBrandConfig()
  if (!brand.useMockApi) {
    const { data } = await http.get<Stock>(`/api/stocks/${stockCode}`)
    return data
  }

  await delay(550)
  const stock = stocks.find(({ code }) => code === stockCode)
  if (!stock) {
    throw new Error('종목을 찾을 수 없습니다.')
  }
  return stock
}

async function getAccount() {
  const brand = getBrandConfig()
  if (!brand.useMockApi) {
    const { data } = await http.get<Account>('/api/account')
    return data
  }

  await delay(750)
  return createMockAccount()
}

async function getHolding(stockCode: string) {
  const brand = getBrandConfig()
  if (!brand.useMockApi) {
    const { data } = await http.get<Holding>(`/api/account/holdings/${stockCode}`)
    return data
  }

  await delay(550)
  const holding = mockHoldings.find(({ stock }) => stock.code === stockCode)
  if (!holding) {
    throw new Error('보유 종목을 찾을 수 없습니다.')
  }
  return holding
}

async function placeOrder(
  request: PlaceOrderRequest,
  options: { simulateResponseLoss: boolean },
) {
  const brand = getBrandConfig()
  let result: OrderResult

  if (brand.useMockApi) {
    await delay(700)
    result =
      mockOrders.get(request.idempotencyKey) ??
      ({
        orderId: crypto.randomUUID(),
        idempotencyKey: request.idempotencyKey,
        stockCode: request.stockCode,
        quantity: request.quantity,
        price: request.price,
        totalAmount: request.price * request.quantity,
        status: 'ACCEPTED',
      } satisfies OrderResult)
    mockOrders.set(request.idempotencyKey, result)
  } else {
    const response = await http.post<OrderResult>('/orders', {
      ...request,
      userId: Number(request.userId) || 1,
      accountNo: brand.accountNo,
    }, {
      headers: { 'Idempotency-Key': request.idempotencyKey },
    })
    result = response.data
  }

  if (options.simulateResponseLoss) {
    throw new SimulatedNetworkLossError()
  }

  return result
}

export const tradingApi = { getStocks, getStock, getAccount, getHolding, placeOrder }
