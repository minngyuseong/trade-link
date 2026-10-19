import type { Stock } from '@/types/trading'

type StockLogoProps = {
  stock: Stock
  size?: 'sm' | 'md' | 'lg'
}

const sizes = {
  sm: 'size-11 text-sm',
  md: 'size-14 text-lg',
  lg: 'size-16 text-xl',
}

export function StockLogo({ stock, size = 'md' }: StockLogoProps) {
  return (
    <div
      aria-hidden="true"
      className={`grid shrink-0 place-items-center rounded-full font-extrabold text-white shadow-sm ${sizes[size]}`}
      style={{ backgroundColor: stock.logoColor }}
    >
      {stock.shortName}
    </div>
  )
}
