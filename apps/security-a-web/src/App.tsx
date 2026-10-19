import { Suspense } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'

import { AccountSkeleton } from '@/components/account-skeleton'
import { OrderSkeleton } from '@/components/order-skeleton'
import { StockListSkeleton } from '@/components/stock-list-skeleton'
import { AccountPage } from '@/pages/account-page'
import { HoldingDetailPage } from '@/pages/holding-detail-page'
import { OrderPage } from '@/pages/order-page'
import { StockListPage } from '@/pages/stock-list-page'

function App() {
  return (
    <Routes>
      <Route
        element={
          <Suspense fallback={<StockListSkeleton />}>
            <StockListPage />
          </Suspense>
        }
        path="/"
      />
      <Route
        element={
          <Suspense fallback={<OrderSkeleton />}>
            <OrderPage />
          </Suspense>
        }
        path="/stocks/:stockCode/order"
      />
      <Route
        element={
          <Suspense fallback={<AccountSkeleton />}>
            <AccountPage />
          </Suspense>
        }
        path="/account"
      />
      <Route
        element={
          <Suspense fallback={<AccountSkeleton />}>
            <HoldingDetailPage />
          </Suspense>
        }
        path="/account/holdings/:stockCode"
      />
      <Route element={<Navigate replace to="/" />} path="*" />
    </Routes>
  )
}

export default App
