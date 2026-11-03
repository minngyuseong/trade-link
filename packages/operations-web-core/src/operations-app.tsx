import {
  Activity,
  ArrowDownLeft,
  ArrowRight,
  ArrowUpRight,
  BarChart3,
  Bell,
  Blocks,
  Building2,
  Check,
  ChevronsUpDown,
  CircleDollarSign,
  Clock3,
  Database,
  FileCheck2,
  Landmark,
  LayoutDashboard,
  RefreshCw,
  Search,
  Server,
  ShieldCheck,
  TrendingUp,
  WalletCards,
} from 'lucide-react'
import { useMemo, useState } from 'react'

import type { OperationsAppConfig } from './index'
import type { ExchangeOrder, Ledger, Settlement, Trade } from './types'
import { useLiveData } from './use-live-data'

const exchangeUrls = ['/admin/orders', '/admin/trades']
const depositoryUrls = ['/admin/settlements', '/admin/ledger']
const won = new Intl.NumberFormat('ko-KR')

function money(value: number) {
  return `${won.format(value)}원`
}

function shortTime(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  }).format(new Date(value))
}

function brokerName(code: string) {
  return code.toUpperCase() === 'A' ? '알파증권' : code.toUpperCase() === 'B' ? '베타증권' : code
}

function EmptyRow({ colSpan, message }: { colSpan: number; message: string }) {
  return (
    <tr>
      <td className="empty-cell" colSpan={colSpan}>{message}</td>
    </tr>
  )
}

function StatusPill({ status }: { status: string }) {
  const complete = status === 'MATCHED' || status === 'SETTLED'
  return (
    <span className={`status-pill ${complete ? 'complete' : 'waiting'}`}>
      <span className="status-dot" />
      {status === 'MATCHED' ? '체결' : status === 'SETTLED' ? '결제 완료' : '대기'}
    </span>
  )
}

function MetricCard({ icon, label, value, detail, tone = 'blue' }: {
  icon: React.ReactNode
  label: string
  value: string
  detail: string
  tone?: 'blue' | 'green' | 'amber' | 'violet'
}) {
  return (
    <article className="metric-card">
      <div className={`metric-icon ${tone}`}>{icon}</div>
      <div className="metric-copy">
        <span>{label}</span>
        <strong>{value}</strong>
        <small>{detail}</small>
      </div>
    </article>
  )
}

function Shell({ config, children, updatedAt, error, refreshing, onRefresh }: {
  config: OperationsAppConfig
  children: React.ReactNode
  updatedAt: Date | null
  error: string | null
  refreshing: boolean
  onRefresh: () => void
}) {
  const exchange = config.kind === 'exchange'
  return (
    <div className={`ops-shell ${exchange ? 'exchange-theme' : 'depository-theme'}`}>
      <aside className="sidebar">
        <div className="brand-lockup">
          <div className="brand-symbol">TL</div>
          <div><strong>Trade Link</strong><span>MARKET INFRA</span></div>
        </div>
        <div className="org-chip">
          <div className="org-icon">{exchange ? <Landmark size={18} /> : <ShieldCheck size={18} />}</div>
          <div><span>운영 기관</span><strong>{exchange ? '한국거래소' : '예탁결제원'}</strong></div>
          <ChevronsUpDown size={15} />
        </div>
        <nav>
          <span className="nav-label">WORKSPACE</span>
          <button className="nav-item active"><LayoutDashboard size={18} />통합 대시보드</button>
          <button className="nav-item"><Activity size={18} />{exchange ? '주문 모니터링' : '결제 모니터링'}</button>
          <button className="nav-item"><BarChart3 size={18} />{exchange ? '체결 내역' : '자산 원장'}</button>
          <span className="nav-label second">SYSTEM</span>
          <button className="nav-item"><Server size={18} />시스템 상태</button>
          <button className="nav-item"><FileCheck2 size={18} />감사 로그</button>
        </nav>
        <div className="sidebar-health">
          <div className="health-head"><span>서비스 상태</span><span className="online"><i />정상</span></div>
          <div className="health-row"><span>{exchange ? '매칭 엔진' : 'DVP 엔진'}</span><strong>ONLINE</strong></div>
          <div className="health-row"><span>데이터베이스</span><strong>ONLINE</strong></div>
        </div>
        <div className="operator">
          <div className="avatar">운</div>
          <div><strong>운영 관리자</strong><span>operations@tradelink</span></div>
        </div>
      </aside>

      <main className="main-area">
        <header className="topbar">
          <div className="mobile-brand"><div className="brand-symbol">TL</div><strong>Trade Link</strong></div>
          <label className="search-box"><Search size={17} /><input aria-label="검색" placeholder="주문번호, 체결번호, 종목 검색" /><kbd>⌘ K</kbd></label>
          <div className="top-actions">
            <span className="live-clock"><i /> LIVE</span>
            <button className="icon-button" aria-label="알림"><Bell size={18} /><em>2</em></button>
          </div>
        </header>

        <div className="content">
          <div className="page-heading">
            <div>
              <div className="eyebrow"><span>{exchange ? 'EXCHANGE' : 'DEPOSITORY'}</span> / OPERATIONS</div>
              <h1>{exchange ? '시장 운영 대시보드' : '결제·소유권 원장'}</h1>
              <p>{exchange ? '실시간 주문 흐름과 체결 현황을 한눈에 모니터링합니다.' : '대금 결제와 증권 소유권 이전을 실시간으로 확인합니다.'}</p>
            </div>
            <div className="heading-actions">
              <div className="updated-at"><span>마지막 동기화</span><strong>{updatedAt ? updatedAt.toLocaleTimeString('ko-KR') : '연결 중'}</strong></div>
              <button className="refresh-button" onClick={onRefresh} disabled={refreshing}>
                <RefreshCw size={16} className={refreshing ? 'spin' : ''} />새로고침
              </button>
            </div>
          </div>
          {error && <div className="error-banner"><Database size={17} /><span>서버 연결을 확인해주세요. {error}</span></div>}
          {children}
        </div>
      </main>
    </div>
  )
}

function ExchangeDashboard({ config }: { config: OperationsAppConfig }) {
  const { data, error, loading, updatedAt, refresh } = useLiveData<[ExchangeOrder[], Trade[]]>(exchangeUrls, config.apiBaseUrl)
  const [query, setQuery] = useState('')
  const orders = data?.[0] ?? []
  const trades = data?.[1] ?? []
  const waiting = orders.filter((order) => order.status === 'WAITING')
  const volume = trades.reduce((sum, trade) => sum + trade.price * trade.quantity, 0)
  const filteredOrders = orders.filter((order) => `${order.exchangeOrderId}${order.stockCode}${order.brokerCode}`.toLowerCase().includes(query.toLowerCase()))
  const orderBook = useMemo(() => {
    const levels = new Map<number, { buy: number; sell: number }>()
    for (const order of waiting) {
      const level = levels.get(order.price) ?? { buy: 0, sell: 0 }
      level[order.side === 'BUY' ? 'buy' : 'sell'] += order.quantity
      levels.set(order.price, level)
    }
    return [...levels.entries()].sort((a, b) => b[0] - a[0]).slice(0, 7)
  }, [waiting])

  return (
    <Shell config={config} updatedAt={updatedAt} error={error} refreshing={loading} onRefresh={() => void refresh()}>
      <section className="metrics-grid">
        <MetricCard icon={<Blocks size={21} />} label="누적 주문" value={won.format(orders.length)} detail="최근 100건 기준" />
        <MetricCard icon={<Clock3 size={21} />} label="대기 주문" value={won.format(waiting.length)} detail="매칭 엔진 처리 대기" tone="amber" />
        <MetricCard icon={<Check size={21} />} label="체결 건수" value={won.format(trades.length)} detail="정상 체결 완료" tone="green" />
        <MetricCard icon={<TrendingUp size={21} />} label="체결 대금" value={money(volume)} detail="조회 구간 누적" tone="violet" />
      </section>

      <section className="dashboard-grid exchange-grid">
        <article className="panel market-panel">
          <div className="panel-head"><div><h2>실시간 호가</h2><p>미체결 주문의 가격대별 잔량</p></div><span className="live-badge"><i /> LIVE</span></div>
          <div className="orderbook-head"><span>매수 잔량</span><span>가격</span><span>매도 잔량</span></div>
          <div className="orderbook">
            {orderBook.length === 0 && <div className="orderbook-empty">현재 대기 중인 호가가 없습니다.</div>}
            {orderBook.map(([price, level]) => (
              <div className="orderbook-row" key={price}>
                <div className="depth buy" style={{ '--depth': `${Math.min(level.buy * 8, 100)}%` } as React.CSSProperties}><span>{level.buy ? won.format(level.buy) : '—'}</span></div>
                <strong>{won.format(price)}</strong>
                <div className="depth sell" style={{ '--depth': `${Math.min(level.sell * 8, 100)}%` } as React.CSSProperties}><span>{level.sell ? won.format(level.sell) : '—'}</span></div>
              </div>
            ))}
          </div>
        </article>

        <article className="panel recent-trades">
          <div className="panel-head"><div><h2>최근 체결</h2><p>매칭 엔진에서 확정된 거래</p></div><button className="text-button">전체 보기 <ArrowRight size={14} /></button></div>
          <div className="trade-feed">
            {trades.length === 0 && <div className="feed-empty">체결된 거래가 없습니다.</div>}
            {trades.slice(0, 6).map((trade) => (
              <div className="trade-item" key={trade.tradeId}>
                <div className="stock-mark">{trade.stockCode.slice(-2)}</div>
                <div className="trade-main"><strong>{trade.stockCode}</strong><span>체결 #{trade.tradeId} · {shortTime(trade.createdAt)}</span></div>
                <div className="trade-value"><strong>{money(trade.price)}</strong><span>{won.format(trade.quantity)}주</span></div>
              </div>
            ))}
          </div>
        </article>
      </section>

      <article className="panel table-panel">
        <div className="panel-head table-title"><div><h2>주문 흐름</h2><p>양 증권사에서 접수된 전체 주문</p></div><label className="table-search"><Search size={15} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="주문 검색" /></label></div>
        <div className="table-wrap"><table><thead><tr><th>거래소 주문</th><th>접수 시각</th><th>증권사</th><th>종목</th><th>구분</th><th className="right">주문가</th><th className="right">수량</th><th>상태</th></tr></thead>
          <tbody>{filteredOrders.length === 0 ? <EmptyRow colSpan={8} message="접수된 주문이 없습니다." /> : filteredOrders.slice(0, 12).map((order) => (
            <tr key={order.exchangeOrderId}><td className="mono">#{order.exchangeOrderId}</td><td>{shortTime(order.createdAt)}</td><td><span className="broker-tag">{order.brokerCode}</span>{brokerName(order.brokerCode)}</td><td><strong>{order.stockCode}</strong></td><td><span className={`side ${order.side.toLowerCase()}`}>{order.side === 'BUY' ? <ArrowDownLeft size={13} /> : <ArrowUpRight size={13} />}{order.side === 'BUY' ? '매수' : '매도'}</span></td><td className="right mono">{money(order.price)}</td><td className="right mono">{won.format(order.quantity)}</td><td><StatusPill status={order.status} /></td></tr>
          ))}</tbody></table></div>
      </article>
    </Shell>
  )
}

function DepositoryDashboard({ config }: { config: OperationsAppConfig }) {
  const { data, error, loading, updatedAt, refresh } = useLiveData<[Settlement[], Ledger]>(depositoryUrls, config.apiBaseUrl)
  const settlements = data?.[0] ?? []
  const ledger = data?.[1] ?? { cash: [], stocks: [] }
  const totalValue = settlements.reduce((sum, item) => sum + item.price * item.quantity, 0)
  const totalShares = settlements.reduce((sum, item) => sum + item.quantity, 0)
  const brokers = [...new Set([...ledger.cash.map((item) => item.brokerCode), ...ledger.stocks.map((item) => item.brokerCode)])]

  return (
    <Shell config={config} updatedAt={updatedAt} error={error} refreshing={loading} onRefresh={() => void refresh()}>
      <section className="metrics-grid">
        <MetricCard icon={<FileCheck2 size={21} />} label="결제 완료" value={`${won.format(settlements.length)}건`} detail="DVP 동시 결제" tone="green" />
        <MetricCard icon={<CircleDollarSign size={21} />} label="이전 대금" value={money(totalValue)} detail="매수사 → 매도사" />
        <MetricCard icon={<WalletCards size={21} />} label="이전 증권" value={`${won.format(totalShares)}주`} detail="매도사 → 매수사" tone="violet" />
        <MetricCard icon={<Building2 size={21} />} label="참가기관" value={`${brokers.length}개사`} detail="원장 연결 정상" tone="amber" />
      </section>

      <section className="dvp-banner">
        <div className="dvp-copy"><span className="dvp-kicker"><ShieldCheck size={14} /> DELIVERY VERSUS PAYMENT</span><h2>대금과 증권이 동시에 이동합니다</h2><p>체결된 거래를 기반으로 증권사 간 현금 결제와 소유권 이전을 원자적으로 처리합니다.</p></div>
        <div className="dvp-flow">
          <div className="flow-node"><div className="flow-icon buy"><CircleDollarSign size={22} /></div><span>매수 증권사</span><strong>대금 지급</strong></div>
          <div className="flow-line"><span>동시 결제</span><i /><ArrowRight size={19} /></div>
          <div className="flow-center"><ShieldCheck size={26} /><strong>KSD</strong><span>원장 확정</span></div>
          <div className="flow-line reverse"><span>소유권 이전</span><i /><ArrowRight size={19} /></div>
          <div className="flow-node"><div className="flow-icon sell"><Blocks size={22} /></div><span>매도 증권사</span><strong>증권 인도</strong></div>
        </div>
      </section>

      <section className="dashboard-grid ledger-grid">
        <article className="panel settlement-panel">
          <div className="panel-head"><div><h2>최근 소유권 이전</h2><p>정산 완료 순서로 표시됩니다.</p></div><span className="secure-badge"><ShieldCheck size={14} />원장 검증됨</span></div>
          <div className="settlement-list">
            {settlements.length === 0 && <div className="feed-empty">완료된 결제 내역이 없습니다.</div>}
            {settlements.slice(0, 6).map((item, index) => (
              <div className="settlement-item" key={item.tradeId}>
                <div className="timeline"><span><Check size={13} /></span>{index < Math.min(settlements.length, 6) - 1 && <i />}</div>
                <div className="settlement-content"><div className="settlement-top"><div><strong>체결 #{item.tradeId}</strong><span>{shortTime(item.settledAt)}</span></div><StatusPill status={item.status} /></div>
                  <div className="ownership-transfer"><div><small>FROM</small><strong><span className="broker-tag">{item.sellBrokerCode}</span>{brokerName(item.sellBrokerCode)}</strong></div><div className="transfer-arrow"><span>{item.stockCode} · {won.format(item.quantity)}주</span><ArrowRight size={18} /></div><div className="to"><small>TO</small><strong><span className="broker-tag">{item.buyBrokerCode}</span>{brokerName(item.buyBrokerCode)}</strong></div></div>
                </div>
              </div>
            ))}
          </div>
        </article>

        <article className="panel assets-panel">
          <div className="panel-head"><div><h2>참가기관 자산</h2><p>예탁결제원 보관 원장 기준</p></div><Database size={18} className="muted-icon" /></div>
          <div className="broker-assets">
            {brokers.length === 0 && <div className="feed-empty">등록된 원장 정보가 없습니다.</div>}
            {brokers.map((broker) => {
              const cash = ledger.cash.find((item) => item.brokerCode === broker)?.amount ?? 0
              const stocks = ledger.stocks.filter((item) => item.brokerCode === broker)
              return <div className="broker-card" key={broker}><div className="broker-card-head"><div className="broker-logo">{broker}</div><div><strong>{brokerName(broker)}</strong><span>기관코드 {broker}</span></div><span className="connected"><i />연결됨</span></div><div className="cash-balance"><span>결제 예수금</span><strong>{money(cash)}</strong></div><div className="stock-holdings">{stocks.slice(0, 3).map((stock) => <div key={stock.stockCode}><span>{stock.stockCode}</span><strong>{won.format(stock.quantity)}주</strong></div>)}{stocks.length === 0 && <small>보유 증권 없음</small>}</div></div>
            })}
          </div>
        </article>
      </section>

      <article className="panel table-panel">
        <div className="panel-head table-title"><div><h2>결제 감사 원장</h2><p>모든 증권·대금 이전 기록</p></div><button className="export-button">감사 보고서</button></div>
        <div className="table-wrap"><table><thead><tr><th>체결번호</th><th>완료 시각</th><th>종목</th><th>소유권 이전</th><th className="right">체결가</th><th className="right">수량</th><th className="right">결제대금</th><th>상태</th></tr></thead>
          <tbody>{settlements.length === 0 ? <EmptyRow colSpan={8} message="결제 원장 기록이 없습니다." /> : settlements.slice(0, 12).map((item) => <tr key={item.tradeId}><td className="mono">#{item.tradeId}</td><td>{shortTime(item.settledAt)}</td><td><strong>{item.stockCode}</strong></td><td><span className="transfer-inline"><b>{item.sellBrokerCode}</b><ArrowRight size={14} /><b>{item.buyBrokerCode}</b></span></td><td className="right mono">{money(item.price)}</td><td className="right mono">{won.format(item.quantity)}</td><td className="right mono"><strong>{money(item.price * item.quantity)}</strong></td><td><StatusPill status={item.status} /></td></tr>)}</tbody></table></div>
      </article>
    </Shell>
  )
}

export function OperationsApp({ config }: { config: OperationsAppConfig }) {
  return config.kind === 'exchange' ? <ExchangeDashboard config={config} /> : <DepositoryDashboard config={config} />
}
