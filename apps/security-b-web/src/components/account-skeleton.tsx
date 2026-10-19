import { MobileShell } from '@/components/mobile-shell'
import { Skeleton } from '@/components/ui/skeleton'

export function AccountSkeleton() {
  return (
    <MobileShell>
      <div className="px-6 pb-28 pt-8">
        <Skeleton className="h-8 w-36 rounded-xl" />
        <Skeleton className="mt-14 h-7 w-24 rounded-lg" />
        <Skeleton className="mt-3 h-12 w-64 rounded-xl" />
        <Skeleton className="mt-3 h-6 w-40 rounded-lg" />
        <Skeleton className="mt-8 h-24 w-full rounded-3xl" />
        <Skeleton className="mt-12 h-7 w-32 rounded-lg" />
        <div className="mt-5 space-y-7">
          {[0, 1].map((item) => (
            <div className="flex items-center gap-4" key={item}>
              <Skeleton className="size-14 rounded-full" />
              <div className="flex-1 space-y-2">
                <Skeleton className="h-6 w-28 rounded-lg" />
                <Skeleton className="h-4 w-16 rounded-lg" />
              </div>
              <Skeleton className="h-12 w-28 rounded-lg" />
            </div>
          ))}
        </div>
      </div>
    </MobileShell>
  )
}
