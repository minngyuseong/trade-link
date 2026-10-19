import { useSuspenseQuery } from '@tanstack/react-query'
import { ArrowLeft } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'

import { tradingApi } from '@/api/trading-api'
import { MobileShell } from '@/components/mobile-shell'
import { StockLogo } from '@/components/stock-logo'

const formatNumber = (value: number) => new Intl.NumberFormat('ko-KR').format(value)
const formatWon = (value: number) => `${formatNumber(value)}원`

type InfoRowProps = {
  label: string
  value: string
  valueClassName?: string
}

function InfoRow({ label, value, valueClassName = 'text-slate-900' }: InfoRowProps) {
  return (
    <div className="flex items-center justify-between py-4">
      <dt className="text-[15px] font-medium text-slate-500">{label}</dt>
      <dd className={`text-[15px] font-bold ${valueClassName}`}>{value}</dd>
    </div>
  )
}

export function HoldingDetailPage() {
  const { stockCode = '' } = useParams()
  const { data: holding } = useSuspenseQuery({
    queryKey: ['holding', stockCode],
    queryFn: () => tradingApi.getHolding(stockCode),
  })
  const isPositive = holding.profitLoss >= 0
  const profitColor = isPositive ? 'text-red-500' : 'text-blue-500'

  return (
    <MobileShell>
      <header className="sticky top-0 z-10 grid h-20 grid-cols-[48px_1fr_48px] items-center bg-white/90 px-4 backdrop-blur-xl">
        <Link
          aria-label="간이계좌로 돌아가기"
          className="grid size-11 place-items-center rounded-full transition-colors hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand)]"
          to="/account"
        >
          <ArrowLeft className="size-6" />
        </Link>
        <p className="text-center text-base font-bold text-slate-900">보유 종목</p>
      </header>

      <main className="px-6 pb-32 pt-5">
        <div className="flex items-center gap-4">
          <StockLogo size="lg" stock={holding.stock} />
          <div>
            <h1 className="text-2xl font-bold tracking-[-0.03em] text-slate-950">{holding.stock.name}</h1>
            <p className="mt-1 text-sm font-medium text-slate-400">{holding.stock.code} · 국내주식</p>
          </div>
        </div>

        <section className="mt-10 rounded-[28px] bg-slate-100 px-6 py-7">
          <p className="text-base font-semibold text-slate-600">평가금액</p>
          <p className="mt-3 text-[2.2rem] font-bold tracking-[-0.05em] text-slate-950">
            {formatWon(holding.evaluationAmount)}
          </p>
          <p className={`mt-2 text-lg font-semibold ${profitColor}`}>
            {isPositive ? '+' : ''}{formatWon(holding.profitLoss)} ({isPositive ? '+' : ''}{holding.profitRate.toFixed(1)}%)
          </p>
        </section>

        <section className="mt-8">
          <h2 className="text-lg font-bold text-slate-950">내 보유 정보</h2>
          <dl className="mt-3 divide-y divide-slate-100">
            <InfoRow label="보유 수량" value={`${formatNumber(holding.quantity)}주`} />
            <InfoRow label="평균 매입가" value={formatWon(holding.averagePrice)} />
            <InfoRow label="현재가" value={formatWon(holding.stock.price)} />
            <InfoRow label="총 매입금액" value={formatWon(holding.purchaseAmount)} />
            <InfoRow label="평가손익" value={`${isPositive ? '+' : ''}${formatWon(holding.profitLoss)}`} valueClassName={profitColor} />
          </dl>
        </section>

      </main>

      <div className="fixed inset-x-0 bottom-0 z-20 mx-auto w-full max-w-[520px] border-t border-slate-100 bg-white/95 p-4 pb-[calc(1rem+env(safe-area-inset-bottom))] backdrop-blur-xl">
        <Link
          className="flex h-16 w-full items-center justify-center rounded-2xl bg-[var(--brand)] text-lg font-bold text-white shadow-lg transition-[filter] hover:brightness-95 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand)] focus-visible:ring-offset-2"
          to={`/stocks/${holding.stock.code}/order`}
        >
          추가 구매하기
        </Link>
      </div>
    </MobileShell>
  )
}
