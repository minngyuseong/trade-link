# Trade Link

증권사 2곳, 거래소, 예탁결제원을 모사하는 모노레포입니다.

## Projects

| Project | Stack | Port |
| --- | --- | --- |
| `security-a-web` | React, Vite, Tailwind CSS, Axios | 5173 |
| `security-b-web` | React, Vite, Tailwind CSS, Axios | 5174 |
| `security-a-server` | Spring Boot, Gradle, Java 17 | 8081 |
| `security-b-server` | Spring Boot, Gradle, Java 17 | 8082 |
| `exchange-server` | Spring Boot, Gradle, Java 17 | 8083 |
| `depository-server` | Spring Boot, Gradle, Java 17 | 8084 |

## Frontend

```shell
pnpm install
pnpm dev:security-a
pnpm dev:security-b
```

## Backend

각 서버 디렉터리에서 다음 명령을 실행합니다.

```shell
./gradlew bootRun
```

## Frontend API contract

프론트엔드는 기본적으로 mock API를 사용합니다. 실제 서버 연동 시 각 웹 앱의 `.env`에서
`VITE_USE_MOCK_API=false`와 `VITE_API_BASE_URL`을 설정합니다.

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/stocks` | 종목 목록 조회 |
| `GET` | `/api/stocks/{stockCode}` | 종목 상세 조회 |
| `GET` | `/api/account` | 고정 사용자의 계좌 및 보유 종목 조회 |
| `GET` | `/api/account/holdings/{stockCode}` | 보유 종목 상세 조회 |
| `POST` | `/api/orders` | 매수 주문 접수 |

주문 요청은 `Idempotency-Key` 헤더와 `X-User-Id` 헤더를 전송합니다. 주문 본문은
`stockCode`, `quantity`, `price`, `userId`, `idempotencyKey`로 구성되며, 같은 멱등성 키의
재요청은 동일한 주문 결과를 반환해야 합니다.
