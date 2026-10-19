export const brand = {
  name: import.meta.env.VITE_BROKER_NAME ?? '트레이드 증권 A',
  shortName: import.meta.env.VITE_BROKER_SHORT_NAME ?? 'A',
  primaryColor: import.meta.env.VITE_BRAND_COLOR ?? '#3182f6',
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8081',
  userId: import.meta.env.VITE_USER_ID ?? 'security-a-user',
  useMockApi: import.meta.env.VITE_USE_MOCK_API !== 'false',
} as const
