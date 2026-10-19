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
