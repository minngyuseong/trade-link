import { MobileShell } from '@/components/mobile-shell'
import { Skeleton } from '@/components/ui/skeleton'

export function OrderSkeleton() {
  return (
    <MobileShell>
      <div className="px-5 pb-8 pt-7">
        <div className="flex items-center justify-between">
          <Skeleton className="size-10 rounded-full" />
          <Skeleton className="h-12 w-36 rounded-xl" />
          <Skeleton className="size-10 rounded-xl" />
        </div>
        <Skeleton className="mt-10 h-8 w-52 rounded-lg" />
        <Skeleton className="mt-4 h-32 w-full rounded-3xl" />
        <Skeleton className="mt-3 h-44 w-full rounded-3xl" />
        <Skeleton className="mt-8 h-20 w-full rounded-2xl" />
        <Skeleton className="mt-6 h-52 w-full rounded-2xl" />
      </div>
    </MobileShell>
  )
}
