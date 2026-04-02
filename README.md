# Beluo Backend

AI 캐릭터 채팅 플랫폼 **Beluo**의 백엔드 서버입니다.<br/>
사용자가 직접 AI 캐릭터를 만들고, 다양한 AI 모델과 채팅할 수 있는 서비스입니다.

## 프로젝트 설계

### 개발환경

**BackEnd**

<img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/spring security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"> <img src="https://img.shields.io/badge/java 17-007396?style=for-the-badge&logoColor=white"> <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">

**Database**

<img src="https://img.shields.io/badge/mongodb-47A248?style=for-the-badge&logo=mongodb&logoColor=white"> <img src="https://img.shields.io/badge/redis-FF4438?style=for-the-badge&logo=redis&logoColor=white">

**AI**

<img src="https://img.shields.io/badge/openai-412991?style=for-the-badge&logoColor=white"> <img src="https://img.shields.io/badge/claude-D97757?style=for-the-badge&logo=anthropic&logoColor=white"> <img src="https://img.shields.io/badge/openrouter-6467F2?style=for-the-badge&logo=openrouter&logoColor=white">

**Infra / Tool**

<img src="https://img.shields.io/badge/render-white?style=for-the-badge&logo=render&logoColor=white&color=black"> <img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/cloudinary-3448C5?style=for-the-badge&logo=cloudinary&logoColor=white"> <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">

---

## 실행 방법

**로컬 실행**

```
./gradlew bootRun
```

**Docker 실행**

```
docker build -t beluo-backend .
docker run -p 8080:8080 --env-file .env beluo-backend
```

---

### 아키텍처 개요

```
Client
  │
  ├── JWT (HttpOnly Cookie)
  ├── Google OAuth2
  │
Spring Boot 3.3.5
  │
  ├── Spring Security (JWT Filter + OAuth2)
  ├── Caffeine Cache (로컬, 5분)
  ├── Redis
  │
  ├── MongoDB Atlas (메인 DB, 소프트 삭제)
  │
  ├── AI 클라이언트 (WebFlux)
  │     ├── OpenAI
  │     ├── Claude (Anthropic)
  │     └── OpenRouter
  │
  ├── Cloudinary (이미지 업로드, 최대 10MB)
  └── Gmail SMTP (이메일 인증)
```

---

## 구현 기능

### 사용자

**인증**

- 이메일 + 비밀번호 회원가입 / 로그인
- Google OAuth2 소셜 로그인
- 이메일 인증 코드 발송 및 검증
- Access Token + Refresh Token (HttpOnly 쿠키 방식, XSS 방어)
- 로그아웃 (토큰 무효화)

**캐릭터**

- 캐릭터 목록 조회 / 키워드 검색
- 캐릭터 상세 조회
- 좋아요 / 좋아요 취소
- 차단 / 차단 해제

**채팅**

- 캐릭터와 대화 세션 생성
- 메시지 전송 (크레딧 차감)
- AI 응답 재생성 (크레딧 차감)
- AI 응답 확정 저장
- 메시지 수정
- 메시지 히스토리 조회 (커서 기반 페이지네이션)

**대화 관리**

- 최근 대화 목록 조회
- 대화 상세 조회
- 대화 이름 수정
- 대화 삭제

**마이페이지**

- 프로필 조회 / 수정 (이미지 포함)
- 회원 탈퇴
- 내가 만든 캐릭터 조회 / 수정 / 삭제
- 좋아요한 캐릭터 조회
- 차단한 캐릭터 조회
- AI 모델 선택 (OpenAI / Claude / OpenRouter)
- 크레딧 현황 조회
- 문의 제출

### 시스템

**크레딧 시스템**

- AI API 사용량 제어를 위한 크레딧 차감 방식
- 채팅 전송 / AI 응답 재생성 시 크레딧 차감

**대화 요약 자동화**

- 장기 대화의 컨텍스트 관리를 위한 자동 요약
- `lastSummarizedAt`, `sinceLastSummaryCount`, `summaryVersion`

**캐릭터 캐싱**

- Caffeine 로컬 캐시로 캐릭터 조회 성능 최적화

---

## API 명세

### 인증 `/api/auth`
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/refresh` | 토큰 갱신 | X |
| POST | `/verify/send` | 이메일 인증 코드 발송 | X |
| POST | `/verify/check` | 이메일 인증 코드 확인 | X |
| POST | `/login` | 이메일 로그인 | X |
| POST | `/join` | 이메일 회원가입 | X |
| POST | `/oauth2/join` | OAuth2 회원가입 추가 정보 입력 | X |
| POST | `/logout` | 로그아웃 | O |

### 캐릭터 `/api/character`
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| GET | `/` | 캐릭터 목록 조회 | X |
| GET | `/list?keyword=` | 키워드 검색 | X |
| GET | `/{id}/summary` | 캐릭터 상세 조회 | X |
| POST | `/create` | 캐릭터 생성 | O |
| POST | `/like/{id}` | 좋아요 | O |
| DELETE | `/like/{id}` | 좋아요 취소 | O |
| POST | `/blocked/{id}` | 차단 | O |
| DELETE | `/blocked/{id}` | 차단 해제 | O |

### 채팅 `/api/chat`
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/send` | 메시지 전송 | O |
| POST | `/regenerate` | AI 응답 재생성 | O |
| POST | `/confirm` | AI 응답 확정 저장 | O |
| GET | `/messages/{sessionId}?before=` | 메시지 히스토리 조회 | O |
| PATCH | `/edit` | 메시지 수정 | O |

### 대화 `/api/conversation`
| Method | URL | 설명 | 인증 |
|--------|-----|------|----|
| GET | `/list` | 대화 목록 조회 | X  |
| GET | `/create/{characterId}` | 대화 세션 생성 | O  |
| GET | `/detail/{sessionId}` | 대화 상세 조회 | O  |
| PATCH | `/edit` | 대화 이름 수정 | O  |
| DELETE | `/delete/{id}` | 대화 삭제 | O  |

### 마이페이지 `/api/mypage`
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| GET | `/overview` | 프로필 요약 조회 | O |
| GET | `/profile` | 프로필 상세 조회 | O |
| PATCH | `/profile` | 프로필 수정 | O |
| DELETE | `/profile` | 회원 탈퇴 | O |
| GET | `/characters` | 내 캐릭터 목록 | O |
| GET | `/characters/{id}` | 내 캐릭터 상세 | O |
| POST | `/characters/{id}` | 캐릭터 수정 | O |
| DELETE | `/characters/{id}` | 캐릭터 삭제 | O |
| GET | `/liked` | 좋아요한 캐릭터 | O |
| GET | `/blocked` | 차단한 캐릭터 | O |
| GET | `/model` | 크레딧 / AI 모델 조회 | O |
| POST | `/model` | AI 모델 선택 | O |
| POST | `/inquiry` | 문의 제출 | O |

---

## 환경 변수

환경 변수는 `.env.example`을 참고해 `.env` 파일을 생성하세요.

```bash
cp /src/main/resources/.env.example .env
```

---

## 프롬프트 파일

프롬프트는 보안상 gitignore 처리되어 있습니다. 아래 4개 파일을 생성하세요.

```
src/main/resources/static/system_prompt.txt
src/main/resources/static/character_prompt.txt
src/main/resources/static/summary_prompt.txt
src/main/resources/static/summary_short_prompt.txt
```

| 파일 | 사용처 | 설명 |
|------|--------|------|
| `system_prompt.txt` | PromptService | 캐릭터 롤플레이 행동 원칙 |
| `character_prompt.txt` | CharacterService | 캐릭터 설정 텍스트 → JSON 변환 파서 |
| `summary_prompt.txt` | SummaryService | 대화 요약 생성 프롬프트 |
| `summary_short_prompt.txt` | PromptService | 요약 데이터 기반 감정 상태 주입 프롬프트 |

---

## 실행 방법

**로컬 실행**

```bash
./gradlew bootRun
```

**Docker 실행**

```bash
docker build -t beluo-backend .
docker run -p 8080:8080 --env-file .env beluo-backend
```

---

## 관련 레포지토리

Frontend: [beluo-frontend](https://github.com/sele906/beluo-frontend.git)

