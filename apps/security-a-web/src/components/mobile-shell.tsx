import type { ReactNode } from 'react'

import { brand } from '@/config/brand'

type MobileShellProps = {
  children: ReactNode
  className?: string
}

export function MobileShell({ children, className = '' }: MobileShellProps) {
  return (
    <div
      className={`mx-auto min-h-svh w-full max-w-[520px] overflow-x-hidden bg-white shadow-[0_0_40px_rgba(15,23,42,0.06)] ${className}`}
      style={{ '--brand': brand.primaryColor } as React.CSSProperties}
    >
      {children}
    </div>
  )
}
