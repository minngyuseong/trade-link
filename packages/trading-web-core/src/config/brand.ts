export type BrokerConfig = Readonly<{
  name: string
  shortName: string
  primaryColor: string
  apiBaseUrl: string
  userId: string
  accountNo: string
}>

export const brand = {} as BrokerConfig

let configured = false

export function configureTradingApp(config: BrokerConfig) {
  Object.assign(brand, config)
  configured = true
}

export function getBrandConfig() {
  if (!configured) {
    throw new Error('Trading web core must be configured before it is rendered.')
  }

  return brand
}
