# meeting-service
Pagely 독서 모임 도메인을 담당하는 마이크로서비스입니다.

공개 REST(`/api/v1/meetings`)로 모임·가입·일정·출석을 제공하고, 서비스 간 연동은 **Kafka**(발행·구독)와 **Feign**(book-service)을 사용합니다. 도메인 이벤트 발행은 **트랜잭션 아웃박스**를 거칩니다.

## 1. 서비스 기능 소개

- **모임** 생성·목록·상세 조회 (일회성/정기), 모집 상태·정원·모임 상태 전이  
  - 목록·상세 GET은 `@AuthRequired` 없음; 그 외 대부분의 변경·민감 조회는 인증 필요
- **가입** 신청·신청 목록·승인, 모집/정원 등 도메인 정책(`MeetingJoinPolicy` 등) 반영
- **일정** 생성·목록·상세·상태 변경, Quartz로 일정 시작 시각에 맞춘 자동 시작
- **출석** 일정 참석 등록·상태 변경·일정별 출석부·통계·내 출석 이력·통계
- **Kafka**: 트랜잭션 커밋과 함께 Outbox에 적재 후 발행; Consumer는 멱등 처리(`p_processed_event`). 구독 예: `meeting.attendance.status-changed`(출석 변경 후속), `report-created`(타 서비스 독후감 생성 이벤트 → 일정 리포트 반영 등)
- **도메인 부가 로직**: 출석·경고 누적, 독후감 미제출 경고(`MissingReportPenaltyService`), 경고 임계 도달 시 자동 강퇴 등은 서비스·도메인 모델에서 처리
- **내부 API** (`/internal/meetings/...`): 예) 유저 기준 열람 가능 모임·참가 일정 ID (`GET .../access/readable`)
- **모니터링**: Spring Actuator + Prometheus

---


## 2. 기술 스택

| 구분 | 내용 |
|------|------|
| 런타임 | Java 21 |
| 프레임워크 | Spring Boot 3.5.13 |
| 영속성 | Spring Data JPA, Flyway, PostgreSQL |
| 메시징 | Spring Kafka |
| 스케줄 | Quartz |
| 클라우드 | Eureka 클라이언트, OpenFeign |
| 공통 라이브러리 | `com.pagely:common` (인증 어노테이션·응답 래퍼 등) |
| 관측 | Actuator, Prometheus |

---
## 3. 외부 연동·의존성
`application.yml` 안의 `${DB_HOST}` 같은 플레이스홀더에 대응하는 식별자 

| 대상 | 역할 | 환경 변수·프로퍼티 이름 |
|------|------|-------------------------------|
| PostgreSQL | 모임 도메인 저장 | `DB_HOST`, `DB_PORT`, `MEETING_DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` |
| Kafka | Outbox 발행, Consumer 구독(예: `meeting.attendance.status-changed`, `report-created`) | `KAFKA_EXTERNAL_HOST`, `KAFKA_EXTERNAL_PORT` |
| Eureka | 서비스 디스커버리(클라이언트 등록/조회) | `EUREKA_SERVER_URL` |
| book-service | 책 ID 검증(OpenFeign `BookClient`) | YAML 키 `book-service.url` (기본 `http://localhost:19051`) |

일정 생성·일회성 모임 생성 등 책 검증이 호출되는 API는 **book-service**가 응답 가능한 상태여야 한다

독후감 `report-created` 이벤트는 Kafka로만 수신한다. 출석 변경 후속 처리는 `meeting.attendance.status-changed` 구독으로 처리한다

---

## 4. 로컬 실행

### 4.1 선행 조건

- JDK **21**
- PostgreSQL, Kafka, Eureka 등 `application.yml` 에 맞게 실행
- (책 연동 API 테스트 시) book-service 실행
  
### 4.2 설정

- 선택: 프로젝트 루트에 **`.env`** 또는 `.env.properties` — `spring.config.import: optional:classpath:/.env[.properties]`
- GitHub Packages에서 `com.pagely:common` 을 받으려면 Gradle `GPR_USER` / `GPR_KEY` (또는 `~/.gradle/gradle.properties`)


### 4.3 빌드·실행
PS:


```bash
./gradlew bootRun
```

Windows:

```bat
gradlew.bat bootRun
```

- HTTP 포트: **`19011`**
- 헬스: `http://localhost:19011/actuator/health`
- Prometheus 형식 메트릭: `http://localhost:19011/actuator/prometheus`

---

## 5. 패키지 구조

루트 패키지: `com.pagely.meetingservice`

- `MeetingserviceApplication`: `@SpringBootApplication`, `@EnableFeignClients`, `@EnableScheduling` (Outbox 폴러·Quartz 등 스케줄 사용)

| 경로 | 역할 |
|------|------|
| `meeting.presentation` | REST 컨트롤러·요청/응답 DTO |
| `meeting.application` | 서비스(조회/명령), port 인터페이스 |
| `meeting.domain` | 엔티티·리포지토리 인터페이스·policy·도메인 이벤트·에러 코드 |
| `meeting.infrastructure` | JPA 어댑터, Kafka(Producer/Consumer/Outbox), Quartz, Feign 클라이언트 |

---

## 6. 핵심 도메인 흐름





[ 피그마 사진 ]





1. **가입**  
   사용자가 모임에 신청 → (정책) 모집·정원·재신청 등 검증 → `p_meeting_recruit` 저장 → 승인 시 모임 멤버 생성·모집 상태 동기화 등

2. **일정**  
   정기 모임은 호스트가 일정 추가(책 검증 가능) → 상태 전이(예: `SCHEDULED` ↔ `ONGOING` ↔ `FINISHED`) 시 출석·모임 상태·Quartz Job 등과 연동

3. **출석**  
   멤버가 일정에 참석 등록 → 진행 중 일정에서 호스트가 출석 상태 변경 → 이벤트 발행 등(후속 Consumer/정책과 연계)

4. **이벤트 발행(Outbox 적재)**  
   도메인 서비스가 `EventPublisher.publish()` 호출 → 트랜잭션 내 `p_outbox` 저장(`KafkaEventPublisher`). 커밋 직전/비트랜잭션 경로는 구현 참고. Outbox 폴러(`OutboxPoller`, `@Scheduled` + `outbox.poll-interval-ms`)가 미발행 건을 읽어 **Kafka로 전송**한다

5. **Kafka 발행 토픽(도메인 이벤트 → Outbox → 동일 토픽명)**  
   `KafkaEventPublisher.resolveTopic` 기준: `meeting.created`, `meeting.schedule.created`, `meeting.schedule.status-changed`, `meeting.attendance.joined`, `meeting.attendance.status-changed`

6. **소비**  
   `@KafkaListener`: `meeting.attendance.status-changed`(출석 변경 후속·경고/강퇴 로직), `report-created`(타 서비스 독후감 생성 → `p_meeting_schedule_report` 등). 
   `p_processed_event`에 `(consumer_name, event_id)` 멱등 저장으로 중복 처리 방지

---

## 7. 비즈니스 정책 (Policy)

패키지: `com.pagely.meetingservice.meeting.domain.policy`

| 클래스 | 역할(요지) |
|--------|------------|
| `MeetingJoinPolicy` | 모집 가능 여부, 정원, 호스트 권한, 재신청 등 |
| `MeetingSchedulePolicy` | 일회성 모임의 추가 일정 금지, 일정-모임 소속, ONGOING 중복 전이 방지, 일정 생성·상태 변경 시 호스트 권한 등 |
| `MeetingAttendancePolicy` | 일정-모임 소속, 활성 멤버, 중복 참석, 진행 중 일정, 호스트 출석 변경 등 |

---

## 8. 데이터베이스 (Flyway)

- 경로: `src/main/resources/db/migration`
- `hibernate.ddl-auto: validate` — 스키마는 마이그레이션과 일치해야 기동

| 버전 | 내용(요지) |
|------|------------|
| V1 | 핵심 테이블·ENUM·인덱스 |
| V2 | 개발용 시드 데이터(고정 UUID·일시 포함) |
| V3 | 이벤트 소비 멱등용 `p_processed_event` |
| V4 | 독후감 연동 `p_meeting_schedule_report` |
| V5 | Outbox `p_outbox` |

**주의:** 이미 적용된 마이그레이션 파일을 수정하면 Flyway **체크섬 오류**가 날 수 있음

시드 데이터의 **모집·일정 일시**가 과거면 API 동작이 제한될 수 있어 로컬 테스트 시 DB 조정 필요

---

## 9. 메시징·아웃박스

### 설정


### 발행 토픽

`KafkaEventPublisher`

- `meeting.created`
- `meeting.schedule.created`
- `meeting.schedule.status-changed`
- `meeting.attendance.joined`
- `meeting.attendance.status-changed`

### 구독 토픽 (`@KafkaListener`)

---

## 10. 테스트

```bash
./gradlew test
```

Windows:

```bat
gradlew.bat test
```

docker-compose 작성 후 추가


