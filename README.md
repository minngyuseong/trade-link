# Trade Link

증권사 2곳, 거래소, 예탁결제원을 모사하는 모노레포입니다.

## Projects

| Project | Stack | Port |
| --- | --- | --- |
| `security-a-web` | React, Vite, Tailwind CSS, Axios | 5173 |
| `security-b-web` | React, Vite, Tailwind CSS, Axios | 5174 |
| `security-a-server` | Spring Boot, Gradle, Java 21 | 8081 |
| `security-b-server` | Spring Boot, Gradle, Java 21 | 8082 |
| `exchange-server` | Spring Boot, Gradle, Java 21 | 8083 |
| `depository-server` | Spring Boot, Gradle, Java 21 | 8084 |

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

프론트엔드는 항상 실제 증권사 서버 API를 사용합니다. 기본 주소는 A가
`http://localhost:8081`, B가 `http://localhost:8082`이며, 다른 주소가 필요하면 각 웹 앱의
`.env`에서 `VITE_API_BASE_URL`을 설정합니다. 서버에 연결할 수 없으면 주문은 실패로 표시됩니다.

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/stocks` | 종목 목록 조회 |
| `GET` | `/api/stocks/{stockCode}` | 종목 상세 조회 |
| `GET` | `/api/account` | 고정 사용자의 계좌 및 보유 종목 조회 |
| `GET` | `/api/account/holdings/{stockCode}` | 보유 종목 상세 조회 |
| `POST` | `/api/orders` | 매수·매도 주문 접수 |

주문 요청은 `Idempotency-Key` 헤더와 `X-User-Id` 헤더를 전송합니다. 주문 본문은
`stockCode`, `quantity`, `price`, `userId`, `idempotencyKey`로 구성되며, 같은 멱등성 키의
재요청은 동일한 주문 결과를 반환해야 합니다.
