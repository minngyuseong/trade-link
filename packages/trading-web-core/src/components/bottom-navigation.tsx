import { ChartNoAxesColumnIncreasing, UserRound } from 'lucide-react'
import { Link } from 'react-router-dom'

type BottomNavigationProps = {
  active: 'stocks' | 'account'
}

const itemClass = 'relative flex min-w-24 flex-col items-center gap-1 py-2 transition-colors'

export function BottomNavigation({ active }: BottomNavigationProps) {
  return (
    <nav className="fixed inset-x-0 bottom-0 z-20 mx-auto flex h-[84px] w-full max-w-[520px] items-center justify-around border-t border-slate-100 bg-white/95 px-8 pb-[env(safe-area-inset-bottom)] backdrop-blur-xl">
      <Link className={`${itemClass} ${active === 'stocks' ? 'text-[var(--brand)]' : 'text-slate-400'}`} to="/">
        {active === 'stocks' && <span className="absolute -top-2 h-1 w-8 rounded-full bg-[var(--brand)]" />}
        <ChartNoAxesColumnIncreasing className="size-6" />
        <span className="text-xs font-bold">주식</span>
      </Link>
      <Link className={`${itemClass} ${active === 'account' ? 'text-[var(--brand)]' : 'text-slate-400'}`} to="/account">
        {active === 'account' && <span className="absolute -top-2 h-1 w-8 rounded-full bg-[var(--brand)]" />}
        <UserRound className="size-6" />
        <span className="text-xs font-bold">간이계좌</span>
      </Link>
    </nav>
  )
}
