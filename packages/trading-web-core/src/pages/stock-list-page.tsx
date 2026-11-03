import { useSuspenseQuery } from '@tanstack/react-query'
import { ChevronRight } from 'lucide-react'
import { Link } from 'react-router-dom'

import { tradingApi } from '../api/trading-api'
import { MobileShell } from '../components/mobile-shell'
import { StockLogo } from '../components/stock-logo'
import { brand } from '../config/brand'

const formatNumber = (value: number) => new Intl.NumberFormat('ko-KR').format(value)
const formatWon = (value: number) => `${formatNumber(value)}원`

export function StockListPage() {
  const { data: account } = useSuspenseQuery({
    queryKey: ['account'],
    queryFn: tradingApi.getAccount,
    refetchOnMount: 'always',
  })

  return (
    <MobileShell className="relative">
      <header className="sticky top-0 z-10 flex h-20 items-center bg-white/90 px-6 backdrop-blur-xl">
        <div>
          <p className="text-xs font-semibold tracking-[0.16em] text-slate-400">TRADE LINK</p>
          <p className="mt-0.5 text-lg font-bold text-slate-900">{brand.name}</p>
        </div>
      </header>

      <main className="px-6 pb-10 pt-8">
        <section>
          <p className="text-lg font-semibold text-slate-700">내 주식</p>
          <h1 className="mt-2 text-[2rem] font-bold leading-tight tracking-[-0.04em] text-slate-950">
            보유 중인 종목
          </h1>
          <p className="mt-4 text-[15px] leading-6 text-slate-500">
            총 평가금액 {formatWon(account.stockEvaluationAmount)}
          </p>
        </section>

        <section className="mt-10" aria-labelledby="stock-list-title">
          <div className="mb-5 flex items-center justify-between">
            <h2 className="text-lg font-bold text-slate-900" id="stock-list-title">보유 종목</h2>
            <span className="rounded-full bg-slate-100 px-3 py-1.5 text-xs font-semibold text-slate-500">
              {account.holdings.length}종목
            </span>
          </div>

          <div className="divide-y divide-slate-100">
            {account.holdings.filter((holding) => holding.quantity > 0).map((holding) => (
                <Link
                  className="group -mx-3 flex items-center gap-4 rounded-2xl px-3 py-5 transition-colors hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand)]"
                  key={holding.stock.code}
                  to={`/stocks/${holding.stock.code}`}
                >
                  <StockLogo stock={holding.stock} />
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-lg font-semibold text-slate-900">{holding.stock.name}</p>
                    <p className="mt-1 text-sm text-slate-400">{formatNumber(holding.quantity)}주</p>
                  </div>
                  <div className="text-right">
                    <p className="text-lg font-bold tracking-[-0.02em] text-slate-900">{formatWon(holding.evaluationAmount)}</p>
                    <p className="mt-1 text-xs font-medium text-slate-400">총평가액</p>
                  </div>
                  <ChevronRight className="size-5 text-slate-300 transition-transform group-hover:translate-x-0.5" />
                </Link>
              ))}
          </div>
        </section>
      </main>
    </MobileShell>
  )
}
