export const brand = {
  name: import.meta.env.VITE_BROKER_NAME ?? '트레이드 증권 B',
  shortName: import.meta.env.VITE_BROKER_SHORT_NAME ?? 'B',
  primaryColor: import.meta.env.VITE_BRAND_COLOR ?? '#7c3aed',
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8082',
  userId: import.meta.env.VITE_USER_ID ?? 'security-b-user',
  useMockApi: import.meta.env.VITE_USE_MOCK_API !== 'false',
} as const
