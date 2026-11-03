import { useMutation, useQueryClient, useSuspenseQuery } from '@tanstack/react-query'
import { ArrowLeft, CheckCircle2, Delete } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link, Navigate, useParams } from 'react-router-dom'

import { tradingApi } from '../api/trading-api'
import { MobileShell } from '../components/mobile-shell'
import { Button } from '../components/ui/button'
import { brand } from '../config/brand'
import type { OrderResult, OrderStatus } from '../types/trading'

const BUYING_POWER = 12_000_000
type OrderSide = 'BUY' | 'SELL'
type DetailTab = 'order' | 'holding'
type HistoryFilter = 'ALL' | OrderSide

const formatNumber = (value: number) => new Intl.NumberFormat('ko-KR').format(value)
const formatWon = (value: number) => `${formatNumber(value)}원`

const statusLabel: Record<OrderStatus, string> = {
  PENDING: '접수 대기',
  ACCEPTED: '주문 접수',
  PARTIAL: '부분 체결',
  MATCHED: '체결',
  SETTLED: '정산 완료',
}

const statusClass: Record<OrderStatus, string> = {
  PENDING: 'bg-slate-100 text-slate-600',
  ACCEPTED: 'bg-amber-50 text-amber-700',
  PARTIAL: 'bg-sky-50 text-sky-700',
  MATCHED: 'bg-violet-50 text-violet-700',
  SETTLED: 'bg-emerald-50 text-emerald-700',
}

const delay = (milliseconds: number) => new Promise((resolve) => window.setTimeout(resolve, milliseconds))

async function trackOrder(orderId: number, onUpdate: (order: OrderResult) => void) {
  for (let attempt = 0; attempt < 120; attempt += 1) {
    const order = await tradingApi.getOrder(orderId)
    onUpdate(order)
    if (order.status === 'SETTLED') return
    await delay(500)
  }
}

export function OrderPage() {
  const { stockCode = '' } = useParams()
  const queryClient = useQueryClient()
  const { data: stock } = useSuspenseQuery({
    queryKey: ['stock', stockCode],
    queryFn: () => tradingApi.getStock(stockCode),
  })
  const { data: account } = useSuspenseQuery({
    queryKey: ['account'],
    queryFn: tradingApi.getAccount,
    refetchOnMount: 'always',
  })
  const { data: orders } = useSuspenseQuery({
    queryKey: ['orders'],
    queryFn: tradingApi.getOrders,
    refetchOnMount: 'always',
  })
  const [tab, setTab] = useState<DetailTab>('order')
  const [historyFilter, setHistoryFilter] = useState<HistoryFilter>('ALL')
  const [lastAcceptedOrder, setLastAcceptedOrder] = useState<OrderResult | null>(null)
  const [quantityText, setQuantityText] = useState('')
  const [idempotencyKey, setIdempotencyKey] = useState(() => crypto.randomUUID())

  const quantity = Number(quantityText || 0)
  const holding = account.holdings.find(({ stock: holdingStock }) => holdingStock.code === stock.code)
  const availableStockQuantity = holding?.quantity ?? 0
  const maxBuyQuantity = Math.floor(BUYING_POWER / stock.price)
  const totalAmount = quantity * stock.price
  const canBuy = quantity > 0 && quantity <= maxBuyQuantity
  const canSell = quantity > 0 && quantity <= availableStockQuantity
  const stockOrders = orders.filter((order) =>
    order.stockCode === stock.code && (historyFilter === 'ALL' || order.side === historyFilter),
  )

  const orderMutation = useMutation({
    mutationFn: (side: OrderSide) =>
      tradingApi.placeOrder({
        stockCode: stock.code,
        side,
        quantity,
        price: stock.price,
        userId: brand.userId,
        idempotencyKey,
      }),
    onSuccess: (acceptedOrder) => {
      setLastAcceptedOrder(acceptedOrder)
      setQuantityText('')
      setIdempotencyKey(crypto.randomUUID())
      queryClient.setQueryData<OrderResult[]>(['orders'], (current = []) => [
        acceptedOrder,
        ...current.filter((order) => order.id !== acceptedOrder.id),
      ])
      void trackOrder(acceptedOrder.id, (updatedOrder) => {
        queryClient.setQueryData<OrderResult[]>(['orders'], (current = []) => [
          updatedOrder,
          ...current.filter((order) => order.id !== updatedOrder.id),
        ])
        if (updatedOrder.status === 'SETTLED') {
          void queryClient.invalidateQueries({ queryKey: ['account'] })
          void queryClient.invalidateQueries({ queryKey: ['holding', stock.code] })
        }
      }).catch(() => {
        void queryClient.invalidateQueries({ queryKey: ['orders'] })
      })
    },
  })

  const resetOrderAttempt = () => {
    if (orderMutation.isError) {
      orderMutation.reset()
      setIdempotencyKey(crypto.randomUUID())
    }
  }

  const appendDigit = (digit: string) => {
    resetOrderAttempt()
    setQuantityText((current) => {
      const next = `${current}${digit}`.replace(/^0+/, '')
      return next.slice(0, 3)
    })
  }

  const removeDigit = () => {
    resetOrderAttempt()
    setQuantityText((current) => current.slice(0, -1))
  }

  const chooseRatio = (ratio: number) => {
    resetOrderAttempt()
    setQuantityText(String(Math.max(1, Math.floor(maxBuyQuantity * ratio))))
  }

  const keypad = useMemo(() => ['1', '2', '3', '4', '5', '6', '7', '8', '9', '00', '0'], [])

  if (!holding || holding.quantity <= 0) {
    return <Navigate replace to="/" />
  }

  return (
    <MobileShell>
      <header className="sticky top-0 z-20 bg-white/95 backdrop-blur-xl">
        <div className="grid h-20 grid-cols-[48px_1fr_48px] items-center px-4">
          <Link
            aria-label="보유 종목 목록으로 돌아가기"
            className="grid size-11 place-items-center rounded-full transition-colors hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand)]"
            to="/"
          >
            <ArrowLeft className="size-6" />
          </Link>
          <div className="text-center">
            <p className="text-base font-bold text-slate-950">{stock.name}</p>
            <p className="mt-0.5 text-sm font-semibold text-red-500">{formatWon(stock.price)}</p>
          </div>
          <span className="mx-auto grid size-8 place-items-center rounded-full bg-[var(--brand)] text-xs font-bold text-white">
            {brand.shortName}
          </span>
        </div>
        <div className="grid grid-cols-2 border-b border-slate-100 px-5">
          {([['order', '주문'], ['holding', '내 주식']] as const).map(([value, label]) => (
            <button
              className={`relative h-14 text-base font-bold transition-colors ${tab === value ? 'text-slate-950' : 'text-slate-400'}`}
              key={value}
              onClick={() => setTab(value)}
              type="button"
            >
              {label}
              {tab === value && <span className="absolute inset-x-5 bottom-0 h-0.5 rounded-full bg-slate-950" />}
            </button>
          ))}
        </div>
      </header>

      {tab === 'order' ? (
        <>
      <main className="px-5 pb-36 pt-5">
        <section className="rounded-[28px] bg-slate-100 px-6 py-6">
          <div className="flex items-center gap-3 text-lg">
            <span className="font-bold text-slate-900">주문 가격</span>
            <span className="text-slate-300">|</span>
            <span className="font-medium text-slate-400">현재가</span>
          </div>
          <p className="mt-8 text-[2rem] font-bold tracking-[-0.04em] text-slate-950">{formatWon(stock.price)}</p>
        </section>

        <section className="mt-3 rounded-[28px] bg-slate-100 px-6 py-6">
          <p className="text-lg font-bold text-slate-900">수량</p>
          <p className={`mt-6 min-h-12 text-[2rem] font-bold tracking-[-0.04em] ${quantity ? 'text-slate-950' : 'text-slate-300'}`}>
            {quantity ? `${formatNumber(quantity)}주` : '몇 주 주문할까요?'}
          </p>
          <div className="mt-4 flex items-center justify-between text-sm font-medium text-slate-400">
            <span>
              구매 최대 {maxBuyQuantity}주 · 판매 최대 {formatNumber(availableStockQuantity)}주
            </span>
            <span>{quantity ? formatWon(totalAmount) : '0원'}</span>
          </div>
        </section>

        {lastAcceptedOrder && (
          <div className="mt-4 flex gap-3 rounded-2xl bg-emerald-50 p-4 text-emerald-800" role="status">
            <CheckCircle2 className="mt-0.5 size-5 shrink-0" />
            <div>
              <p className="font-bold">주문이 접수됐어요. 바로 다음 주문을 넣을 수 있어요.</p>
              <p className="mt-1 text-sm">주문번호 {lastAcceptedOrder.orderId} · 체결 상태는 내 주식에서 확인하세요.</p>
            </div>
          </div>
        )}

        {orderMutation.isError && (
          <div className="mt-4 rounded-2xl bg-rose-50 p-4 text-sm font-medium text-rose-700" role="alert">
            주문 처리에 실패했습니다. 잠시 후 다시 시도해 주세요.
          </div>
        )}

        <div className="mt-7 grid grid-cols-4 gap-2">
          {[0.1, 0.25, 0.5, 1].map((ratio) => (
            <Button
              className="h-12 rounded-2xl border-slate-200 text-base font-bold text-slate-600"
              key={ratio}
              onClick={() => chooseRatio(ratio)}
              variant="outline"
            >
              {ratio === 1 ? '최대' : `${ratio * 100}%`}
            </Button>
          ))}
        </div>

        <div className="mt-5 grid grid-cols-3 gap-y-1">
          {keypad.map((key) => (
            <Button
              className="h-16 rounded-2xl text-3xl font-medium text-slate-800 hover:bg-slate-100"
              key={key}
              onClick={() => appendDigit(key)}
              variant="ghost"
            >
              {key}
            </Button>
          ))}
          <Button
            aria-label="한 자리 지우기"
            className="h-16 rounded-2xl text-slate-600 hover:bg-slate-100"
            onClick={removeDigit}
            variant="ghost"
          >
            <Delete className="size-7" />
          </Button>
        </div>
      </main>

      <div className="fixed inset-x-0 bottom-0 z-20 mx-auto grid w-full max-w-[520px] grid-cols-2 gap-3 border-t border-slate-100 bg-white/95 p-4 pb-[calc(1rem+env(safe-area-inset-bottom))] backdrop-blur-xl">
        <Button
          className="h-16 rounded-2xl bg-blue-600 text-lg font-bold text-white shadow-lg shadow-blue-600/15 hover:bg-blue-600 hover:brightness-95"
          disabled={!canSell || orderMutation.isPending}
          onClick={() => orderMutation.mutate('SELL')}
        >
          {orderMutation.isPending && orderMutation.variables === 'SELL' ? '판매 처리 중...' : '판매하기'}
        </Button>
        <Button
          className="h-16 rounded-2xl bg-red-500 text-lg font-bold text-white shadow-lg shadow-red-500/15 hover:bg-red-500 hover:brightness-95"
          disabled={!canBuy || orderMutation.isPending}
          onClick={() => orderMutation.mutate('BUY')}
        >
          {orderMutation.isPending && orderMutation.variables === 'BUY' ? '구매 처리 중...' : '구매하기'}
        </Button>
      </div>
        </>
      ) : (
        <main className="px-5 pb-12 pt-6">
          <section className="grid grid-cols-2 gap-3">
            <div className="rounded-[24px] bg-slate-100 px-5 py-5">
              <p className="text-sm font-semibold text-slate-500">보유수량</p>
              <p className="mt-3 text-2xl font-bold text-slate-950">{formatNumber(availableStockQuantity)}주</p>
            </div>
            <div className="rounded-[24px] bg-slate-100 px-5 py-5">
              <p className="text-sm font-semibold text-slate-500">총평가금액</p>
              <p className="mt-3 text-2xl font-bold text-slate-950">{formatWon(holding?.evaluationAmount ?? 0)}</p>
            </div>
          </section>

          <section className="mt-10" aria-labelledby="order-history-title">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold text-slate-950" id="order-history-title">주문내역</h2>
              <span className="text-sm font-semibold text-slate-400">{stockOrders.length}건</span>
            </div>
            <div className="mt-4 flex gap-2">
              {([['ALL', '전체'], ['BUY', '구매'], ['SELL', '판매']] as const).map(([value, label]) => (
                <button
                  className={`rounded-full px-4 py-2 text-sm font-bold ${historyFilter === value ? 'bg-slate-950 text-white' : 'bg-slate-100 text-slate-500'}`}
                  key={value}
                  onClick={() => setHistoryFilter(value)}
                  type="button"
                >
                  {label}
                </button>
              ))}
            </div>

            <div className="mt-5 divide-y divide-slate-100">
              {stockOrders.length === 0 ? (
                <div className="py-16 text-center text-sm font-medium text-slate-400">아직 주문내역이 없습니다.</div>
              ) : stockOrders.map((order) => (
                <article className="py-5" key={order.id}>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className={`text-base font-bold ${order.side === 'BUY' ? 'text-red-500' : 'text-blue-600'}`}>
                        {order.side === 'BUY' ? '구매' : '판매'}
                      </span>
                      <span className="text-sm text-slate-400">주문 #{order.orderId}</span>
                    </div>
                    <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${statusClass[order.status]}`}>
                      {statusLabel[order.status]}
                    </span>
                  </div>
                  <div className="mt-3 flex items-end justify-between">
                    <div>
                      <p className="text-lg font-bold text-slate-950">{formatNumber(order.quantity)}주</p>
                      <p className="mt-1 text-sm text-slate-400">주당 {formatWon(order.price)}</p>
                    </div>
                    <p className="text-base font-bold text-slate-700">{formatWon(order.price * order.quantity)}</p>
                  </div>
                </article>
              ))}
            </div>
          </section>
        </main>
      )}
    </MobileShell>
  )
}
