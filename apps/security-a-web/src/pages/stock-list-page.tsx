import { useSuspenseQuery } from '@tanstack/react-query'
import { ChevronRight, Menu, Search } from 'lucide-react'
import { Link } from 'react-router-dom'

import { tradingApi } from '@/api/trading-api'
import { BottomNavigation } from '@/components/bottom-navigation'
import { MobileShell } from '@/components/mobile-shell'
import { StockLogo } from '@/components/stock-logo'
import { Button } from '@/components/ui/button'
import { brand } from '@/config/brand'

const formatWon = (value: number) => `${new Intl.NumberFormat('ko-KR').format(value)}원`

export function StockListPage() {
  const { data: stocks } = useSuspenseQuery({
    queryKey: ['stocks'],
    queryFn: tradingApi.getStocks,
  })

  return (
    <MobileShell className="relative">
      <header className="sticky top-0 z-10 flex h-20 items-center justify-between bg-white/90 px-6 backdrop-blur-xl">
        <div>
          <p className="text-xs font-semibold tracking-[0.16em] text-slate-400">TRADE LINK</p>
          <p className="mt-0.5 text-lg font-bold text-slate-900">{brand.name}</p>
        </div>
        <div className="flex gap-1">
          <Button aria-label="종목 검색" className="size-10 rounded-full" size="icon" variant="ghost">
            <Search className="size-5" />
          </Button>
          <Button aria-label="메뉴" className="size-10 rounded-full" size="icon" variant="ghost">
            <Menu className="size-5" />
          </Button>
        </div>
      </header>

      <main className="px-6 pb-32 pt-8">
        <section>
          <p className="text-lg font-semibold text-slate-700">주식</p>
          <h1 className="mt-2 text-[2rem] font-bold leading-tight tracking-[-0.04em] text-slate-950">
            어떤 종목을<br />구매할까요?
          </h1>
          <p className="mt-4 text-[15px] leading-6 text-slate-500">
            {brand.name}의 간이 계좌로 주문할 종목을 선택하세요.
          </p>
        </section>

        <section className="mt-14" aria-labelledby="stock-list-title">
          <div className="mb-5 flex items-center justify-between">
            <h2 className="text-lg font-bold text-slate-900" id="stock-list-title">종목</h2>
            <span className="rounded-full bg-slate-100 px-3 py-1.5 text-xs font-semibold text-slate-500">
              국내주식 2
            </span>
          </div>

          <div className="divide-y divide-slate-100">
            {stocks.map((stock) => {
              const isPositive = stock.changeRate >= 0
              return (
                <Link
                  className="group -mx-3 flex items-center gap-4 rounded-2xl px-3 py-5 transition-colors hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand)]"
                  key={stock.code}
                  to={`/stocks/${stock.code}/order`}
                >
                  <StockLogo stock={stock} />
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-lg font-semibold text-slate-900">{stock.name}</p>
                    <p className="mt-1 text-sm text-slate-400">{stock.code}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-lg font-bold tracking-[-0.02em] text-slate-900">{formatWon(stock.price)}</p>
                    <p className={`mt-1 text-sm font-medium ${isPositive ? 'text-red-500' : 'text-blue-500'}`}>
                      {isPositive ? '+' : ''}{stock.changeRate.toFixed(1)}%
                    </p>
                  </div>
                  <ChevronRight className="size-5 text-slate-300 transition-transform group-hover:translate-x-0.5" />
                </Link>
              )
            })}
          </div>
        </section>
      </main>

      <BottomNavigation active="stocks" />
    </MobileShell>
  )
}
