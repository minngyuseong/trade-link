import { Skeleton } from '@/components/ui/skeleton'
import { MobileShell } from '@/components/mobile-shell'

export function StockListSkeleton() {
  return (
    <MobileShell>
      <div className="px-6 pb-28 pt-8">
        <Skeleton className="h-8 w-36 rounded-xl" />
        <Skeleton className="mt-14 h-7 w-28 rounded-lg" />
        <Skeleton className="mt-3 h-11 w-72 rounded-xl" />
        <div className="mt-14 space-y-8">
          {[0, 1].map((item) => (
            <div className="flex items-center gap-4" key={item}>
              <Skeleton className="size-14 rounded-full" />
              <div className="flex-1 space-y-2">
                <Skeleton className="h-6 w-28 rounded-lg" />
                <Skeleton className="h-4 w-20 rounded-lg" />
              </div>
              <div className="space-y-2">
                <Skeleton className="ml-auto h-6 w-28 rounded-lg" />
                <Skeleton className="ml-auto h-4 w-16 rounded-lg" />
              </div>
            </div>
          ))}
        </div>
      </div>
    </MobileShell>
  )
}
