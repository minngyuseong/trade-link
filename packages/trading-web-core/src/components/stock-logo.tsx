import Samsung from '@thesvg/react/samsung'
import SkHynix from '@thesvg/react/sk-hynix'

import type { Stock } from '../types/trading'

type StockLogoProps = {
  stock: Stock
  size?: 'sm' | 'md' | 'lg'
}

const sizes = {
  sm: {
    container: 'size-11 text-sm',
    samsung: 'w-8',
    skHynix: 'w-9',
  },
  md: {
    container: 'size-14 text-lg',
    samsung: 'w-10',
    skHynix: 'w-11',
  },
  lg: {
    container: 'size-16 text-xl',
    samsung: 'w-12',
    skHynix: 'w-13',
  },
}

export function StockLogo({ stock, size = 'md' }: StockLogoProps) {
  const sizeClasses = sizes[size]
  const brandLogo = stock.code === '005930'
    ? <Samsung className={sizeClasses.samsung} />
    : stock.code === '000660'
      ? <SkHynix className={sizeClasses.skHynix} />
      : null

  if (brandLogo) {
    return (
      <div
        aria-hidden="true"
        className={`grid shrink-0 place-items-center rounded-2xl border border-slate-200 bg-white shadow-sm ${sizeClasses.container}`}
      >
        {brandLogo}
      </div>
    )
  }

  return (
    <div
      aria-hidden="true"
      className={`grid shrink-0 place-items-center rounded-full font-extrabold text-white shadow-sm ${sizeClasses.container}`}
      style={{ backgroundColor: stock.logoColor }}
    >
      {stock.shortName}
    </div>
  )
}
