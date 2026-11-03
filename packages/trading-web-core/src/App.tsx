import { Suspense } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'

import { OrderSkeleton } from './components/order-skeleton'
import { StockListSkeleton } from './components/stock-list-skeleton'
import { OrderPage } from './pages/order-page'
import { StockListPage } from './pages/stock-list-page'

export function TradingApp() {
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
        path="/stocks/:stockCode"
      />
      <Route element={<Navigate replace to="/" />} path="/account" />
      <Route element={<Navigate replace to="/" />} path="*" />
    </Routes>
  )
}
