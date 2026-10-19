import { useMutation, useSuspenseQuery } from '@tanstack/react-query'
import { ArrowLeft, CheckCircle2, Delete, FlaskConical, WalletCards, WifiOff } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'

import { SimulatedNetworkLossError, tradingApi } from '@/api/trading-api'
import { MobileShell } from '@/components/mobile-shell'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { brand } from '@/config/brand'

const BUYING_POWER = 12_000_000
const formatNumber = (value: number) => new Intl.NumberFormat('ko-KR').format(value)
const formatWon = (value: number) => `${formatNumber(value)}원`

export function OrderPage() {
  const { stockCode = '' } = useParams()
  const { data: stock } = useSuspenseQuery({
    queryKey: ['stock', stockCode],
    queryFn: () => tradingApi.getStock(stockCode),
  })
  const [quantityText, setQuantityText] = useState('')
  const [simulateLoss, setSimulateLoss] = useState(false)
  const [idempotencyKey, setIdempotencyKey] = useState(() => crypto.randomUUID())
  const [lostResponse, setLostResponse] = useState(false)

  const quantity = Number(quantityText || 0)
  const maxQuantity = Math.floor(BUYING_POWER / stock.price)
  const totalAmount = quantity * stock.price
  const canOrder = quantity > 0 && quantity <= maxQuantity

  const orderMutation = useMutation({
    mutationFn: () =>
      tradingApi.placeOrder(
        {
          stockCode: stock.code,
          quantity,
          price: stock.price,
          userId: brand.userId,
          idempotencyKey,
        },
        { simulateResponseLoss: simulateLoss },
      ),
    onSuccess: () => setLostResponse(false),
    onError: (error) => setLostResponse(error instanceof SimulatedNetworkLossError),
  })

  const resetOrderAttempt = () => {
    if (orderMutation.isSuccess || orderMutation.isError) {
      orderMutation.reset()
      setLostResponse(false)
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
    setQuantityText(String(Math.max(1, Math.floor(maxQuantity * ratio))))
  }

  const keypad = useMemo(() => ['1', '2', '3', '4', '5', '6', '7', '8', '9', '00', '0'], [])

  return (
    <MobileShell>
      <header className="sticky top-0 z-20 grid h-20 grid-cols-[48px_1fr_48px] items-center bg-white/90 px-4 backdrop-blur-xl">
        <Link
          aria-label="종목 목록으로 돌아가기"
          className="grid size-11 place-items-center rounded-full transition-colors hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand)]"
          to="/"
        >
          <ArrowLeft className="size-6" />
        </Link>
        <div className="text-center">
          <p className="text-sm font-medium text-slate-500">{stock.name}</p>
          <p className="mt-0.5 text-lg font-bold text-red-500">{formatWon(stock.price)}</p>
        </div>
        <WalletCards className="mx-auto size-6 text-slate-400" />
      </header>

      <main className="px-5 pb-36 pt-4">
        <div className="flex items-center gap-2 px-1 text-lg font-bold text-slate-900">
          <span className="grid size-7 place-items-center rounded-full bg-[var(--brand)] text-sm text-white">{brand.shortName}</span>
          간이계좌
          <span className="ml-auto text-sm font-medium text-slate-400">주문 연습</span>
        </div>

        <section className="mt-5 rounded-[28px] bg-slate-100 px-6 py-6">
          <div className="flex items-center gap-3 text-lg">
            <span className="font-bold text-slate-900">구매할 가격</span>
            <span className="text-slate-300">|</span>
            <span className="font-medium text-slate-400">현재가</span>
          </div>
          <p className="mt-8 text-[2rem] font-bold tracking-[-0.04em] text-slate-950">{formatWon(stock.price)}</p>
        </section>

        <section className="mt-3 rounded-[28px] bg-slate-100 px-6 py-6">
          <p className="text-lg font-bold text-slate-900">수량</p>
          <p className={`mt-6 min-h-12 text-[2rem] font-bold tracking-[-0.04em] ${quantity ? 'text-slate-950' : 'text-slate-300'}`}>
            {quantity ? `${formatNumber(quantity)}주` : '몇 주 구매할까요?'}
          </p>
          <div className="mt-4 flex items-center justify-between text-sm font-medium text-slate-400">
            <span>구매가능 {formatWon(BUYING_POWER)} · 최대 {maxQuantity}주</span>
            <span>{quantity ? formatWon(totalAmount) : '0원'}</span>
          </div>
        </section>

        <section className="mt-5 rounded-2xl border border-amber-200 bg-amber-50 p-4">
          <label className="flex cursor-pointer items-start gap-3" htmlFor="simulate-network-loss">
            <Checkbox
              checked={simulateLoss}
              className="mt-0.5 size-5 data-checked:border-amber-500 data-checked:bg-amber-500"
              id="simulate-network-loss"
              onCheckedChange={(checked) => setSimulateLoss(checked === true)}
            />
            <span className="min-w-0 flex-1">
              <span className="flex items-center gap-2 font-bold text-amber-950">
                <WifiOff className="size-4" /> 강제로 네트워크 유실
              </span>
              <span className="mt-1 block text-sm leading-5 text-amber-800/80">
                서버가 주문을 처리한 뒤 응답만 유실시켜 멱등 재시도를 연습합니다.
              </span>
            </span>
          </label>
        </section>

        {orderMutation.isSuccess && (
          <div className="mt-4 flex gap-3 rounded-2xl bg-emerald-50 p-4 text-emerald-800" role="status">
            <CheckCircle2 className="mt-0.5 size-5 shrink-0" />
            <div>
              <p className="font-bold">주문이 접수됐어요</p>
              <p className="mt-1 text-sm">주문번호 {orderMutation.data.orderId.slice(0, 8)}</p>
            </div>
          </div>
        )}

        {lostResponse && (
          <div className="mt-4 flex gap-3 rounded-2xl bg-rose-50 p-4 text-rose-800" role="alert">
            <FlaskConical className="mt-0.5 size-5 shrink-0" />
            <div>
              <p className="font-bold">응답이 유실됐어요</p>
              <p className="mt-1 text-sm leading-5">
                서버 처리 여부는 알 수 없습니다. 체크를 끈 뒤 같은 멱등성 키로 다시 보내보세요.
              </p>
            </div>
          </div>
        )}

        {orderMutation.isError && !lostResponse && (
          <div className="mt-4 rounded-2xl bg-rose-50 p-4 text-sm font-medium text-rose-700" role="alert">
            주문 요청에 실패했습니다. 잠시 후 다시 시도해주세요.
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

        <div className="mt-4 rounded-xl bg-slate-50 px-4 py-3 text-center font-mono text-[11px] text-slate-400">
          idempotency-key · {idempotencyKey}
        </div>
      </main>

      <div className="fixed inset-x-0 bottom-0 z-20 mx-auto w-full max-w-[520px] border-t border-slate-100 bg-white/95 p-4 pb-[calc(1rem+env(safe-area-inset-bottom))] backdrop-blur-xl">
        <Button
          className="h-16 w-full rounded-2xl bg-[var(--brand)] text-lg font-bold text-white shadow-lg shadow-blue-500/15 hover:bg-[var(--brand)] hover:brightness-95"
          disabled={!canOrder || orderMutation.isPending || orderMutation.isSuccess}
          onClick={() => orderMutation.mutate()}
        >
          {orderMutation.isPending
            ? '주문 전송 중...'
            : lostResponse
              ? '같은 주문 다시 보내기'
              : quantity
                ? `${formatWon(totalAmount)} 구매하기`
                : '구매하기'}
        </Button>
      </div>
    </MobileShell>
  )
}
