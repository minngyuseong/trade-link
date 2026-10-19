import { useSuspenseQuery } from '@tanstack/react-query'
import { ChevronRight, Menu } from 'lucide-react'
import { Link } from 'react-router-dom'

import { tradingApi } from '@/api/trading-api'
import { BottomNavigation } from '@/components/bottom-navigation'
import { MobileShell } from '@/components/mobile-shell'
import { StockLogo } from '@/components/stock-logo'
import { Button } from '@/components/ui/button'
import { brand } from '@/config/brand'

const formatNumber = (value: number) => new Intl.NumberFormat('ko-KR').format(value)
const formatWon = (value: number) => `${formatNumber(value)}원`

export function AccountPage() {
  const { data: account } = useSuspenseQuery({
    queryKey: ['account', brand.userId],
    queryFn: tradingApi.getAccount,
  })
  const isPositive = account.totalProfitLoss >= 0

  return (
    <MobileShell className="relative">
      <header className="sticky top-0 z-10 flex h-20 items-center justify-between bg-white/90 px-6 backdrop-blur-xl">
        <div>
          <p className="text-xs font-semibold tracking-[0.16em] text-slate-400">TRADE LINK</p>
          <p className="mt-0.5 text-lg font-bold text-slate-900">{brand.name}</p>
        </div>
        <Button aria-label="메뉴" className="size-10 rounded-full" size="icon" variant="ghost">
          <Menu className="size-5" />
        </Button>
      </header>

      <main className="px-6 pb-32 pt-8">
        <section>
          <p className="text-lg font-semibold text-slate-700">내 투자</p>
          <h1 className="mt-2 text-[2.35rem] font-bold tracking-[-0.05em] text-slate-950">
            {formatWon(account.totalAsset)}
          </h1>
          <p className={`mt-2 text-lg font-semibold ${isPositive ? 'text-red-500' : 'text-blue-500'}`}>
            {isPositive ? '+' : ''}{formatWon(account.totalProfitLoss)} ({isPositive ? '+' : ''}{account.totalProfitRate.toFixed(1)}%)
          </p>
        </section>

        <section className="mt-8 flex items-center justify-between rounded-[24px] bg-slate-100 px-5 py-5">
          <div>
            <p className="text-sm font-semibold text-slate-500">주문 가능 금액</p>
            <p className="mt-1.5 text-xl font-bold text-slate-900">{formatWon(account.cashBalance)}</p>
          </div>
          <div className="rounded-full bg-white px-3 py-1.5 text-xs font-bold text-slate-500 shadow-sm">
            {brand.shortName} 간이계좌
          </div>
        </section>

        <section className="mt-12" aria-labelledby="holdings-title">
          <div className="mb-4 flex items-end justify-between">
            <div>
              <h2 className="text-xl font-bold text-slate-950" id="holdings-title">보유 중인 주식</h2>
              <p className="mt-1 text-sm text-slate-400">평가금 {formatWon(account.stockEvaluationAmount)}</p>
            </div>
            <span className="rounded-full bg-slate-100 px-3 py-1.5 text-xs font-semibold text-slate-500">
              {account.holdings.length}종목
            </span>
          </div>

          <div className="divide-y divide-slate-100">
            {account.holdings.map((holding) => {
              const holdingIsPositive = holding.profitLoss >= 0
              return (
                <Link
                  className="group -mx-3 flex items-center gap-4 rounded-2xl px-3 py-5 transition-colors hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand)]"
                  key={holding.stock.code}
                  to={`/account/holdings/${holding.stock.code}`}
                >
                  <StockLogo stock={holding.stock} />
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-lg font-semibold text-slate-900">{holding.stock.name}</p>
                    <p className="mt-1 text-sm text-slate-400">{holding.quantity}주</p>
                  </div>
                  <div className="text-right">
                    <p className="text-lg font-bold tracking-[-0.02em] text-slate-900">{formatWon(holding.evaluationAmount)}</p>
                    <p className={`mt-1 text-sm font-medium ${holdingIsPositive ? 'text-red-500' : 'text-blue-500'}`}>
                      {holdingIsPositive ? '+' : ''}{formatWon(holding.profitLoss)} ({holdingIsPositive ? '+' : ''}{holding.profitRate.toFixed(1)}%)
                    </p>
                  </div>
                  <ChevronRight className="size-5 text-slate-300 transition-transform group-hover:translate-x-0.5" />
                </Link>
              )
            })}
          </div>
        </section>
      </main>

      <BottomNavigation active="account" />
    </MobileShell>
  )
}
