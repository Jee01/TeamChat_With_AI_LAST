# TeamChat_With_AI

## 💭 intro
**DATE : 2024.11.12 ~ 2024.12.11

**배경** : 단체 채팅방 개발과 AI api를 활용한 기능을 구현해 보고 싶었고 컴퓨터 네트워크 강의 수강을 통해 배운 소켓에 대한 개념을 활용하기 적절하다 생각이 들었으며 학교 팀 프로젝트를 진행하며 사용하는 단체 채팅방에 AI가 합쳐져있어 대화 내용의 정리를 간단히 할 수 있는 어시스턴스가 필요하다는 생각에 개발.

**담당 역할** 

## 🧰 사용 기술
- Spring boot 3.4.0
- java 21
- WebSocket
- OpenAI API (Chat Completions)
- Spring Data JPA / Hibernate
- Spring Security
- MySQL / H2
- HTML
- CSS
- JavaScript

## 📂 구조

```
src/main/java/me/project/teamchat_with_ai/
├── TeamChatWithAiApplication.java   # 스프링 부트 실행 진입점
└── chat/
    ├── config/
    │   ├── GptConfig.java           # OpenAI API 키/모델 설정 값 바인딩
    │   ├── SecurityConfig.java      # Spring Security 설정
    │   └── WebSocketConfig.java     # WebSocket 핸들러 등록 ("/chat")
    ├── controller/
    │   ├── GptController.java       # GPT 질의 REST API
    │   ├── MessageController.java   # 채팅 메시지 조회 REST API
    │   ├── RoomController.java      # 채팅방 생성/조회 REST API
    │   ├── SocketHandler.java       # WebSocket 메시지 송수신 처리
    │   └── UserController.java      # IP 기반 유저 조회/생성, 닉네임 변경
    ├── entity/
    │   ├── Message.java             # 메시지 엔티티
    │   ├── MessageDTO.java          # 메시지 조회용 DTO
    │   ├── MessageInputDTO.java     # 클라이언트 → 서버 메시지 DTO
    │   ├── MessageOutputDTO.java    # 서버 → 클라이언트 브로드캐스트 DTO
    │   ├── Room.java                # 채팅방 엔티티
    │   └── User.java                # 유저 엔티티
    ├── repository/
    │   ├── FileRepository.java      # 파일 업로드용 (미구현, 빈 인터페이스)
    │   ├── MessageRepository.java
    │   ├── RoomRepository.java
    │   └── UserRepository.java
    └── service/
        └── OpenAiService.java       # OpenAI API 호출 서비스

src/main/resources/
├── application.properties           # DB / Security 설정
└── static/
    ├── main.html                    # 채팅방 목록 페이지
    └── chatroom.html                # 채팅 화면 (메시지 송수신, AI 질문)
```

## 🛠 기능 구현

### 채팅방 (다중 룸)
- `Room` 엔티티 기반으로 채팅방을 여러 개 생성/조회 가능
- `RoomController`가 채팅방 생성 및 목록 조회 REST API 제공

### 실시간 메시지 송수신 (WebSocket)
- `SocketHandler`가 `TextWebSocketHandler`를 구현하여 `/chat?room_id={roomId}` 엔드포인트로 연결
- 룸 별 세션을 `ConcurrentHashMap<Long, List<WebSocketSession>>`으로 관리
- 접속 시 해당 방의 기존 대화 기록을 DB에서 불러와 전송
- 메시지 수신 시 DB에 저장 후 같은 방에 있는 모든 세션에 브로드캐스트
- 연결 종료 시 세션 목록에서 제거

### 유저 식별 (IP 기반)
- 로그인 없이 클라이언트 IP(`X-Forwarded-For` 또는 `RemoteAddr`)로 유저를 식별
- `POST /api/user/ip` 호출 시 IP로 기존 유저 조회, 없으면 신규 생성
- `PUT /api/user/{userId}/nickname`으로 닉네임 변경 가능 (중복 확인 없음)

### OpenAI API 연동
- `GptConfig`가 `openai.secret-key`, `openai.model` 프로퍼티를 바인딩
- `OpenAiService`가 OpenAI Chat Completions API(`https://api.openai.com/v1/chat/completions`)를 호출
- `GET /gpt/ask?prompt={prompt}`로 질의, 응답 텍스트 반환
- 채팅 화면에서 "AI 활용 질문" 체크 시 질문/답변이 채팅방에 함께 기록됨 (AI 답변은 고정 userId=14 사용)

### REST API 목록

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/room` | 채팅방 전체 조회 |
| GET | `/api/room/{roomId}` | 특정 채팅방 조회 |
| POST | `/api/room` | 채팅방 생성 |
| GET | `/api/messages/{roomId}` | 특정 방의 메시지 조회 |
| GET | `/api/user/{ipAddress}` | IP로 유저 조회 |
| POST | `/api/user/ip` | 접속 IP로 유저 조회/생성 |
| PUT | `/api/user/{userId}/nickname` | 닉네임 변경 |
| GET | `/gpt/ask?prompt={prompt}` | OpenAI GPT 질의 |
| WS | `/chat?room_id={roomId}` | 실시간 채팅 WebSocket |

### 데이터베이스 (JPA)
- `User` : user_id(PK), nickname(unique), ip_address(unique)
- `Room` : room_id(PK), name(unique)
- `Message` : message_id(PK), room_id(FK), user_id(FK), content, time_at
- `spring.jpa.hibernate.ddl-auto=update`로 실행 시 테이블 자동 생성/갱신 (MySQL 기준), H2도 런타임 의존성으로 포함되어 있음

## 🚀 실행 방법

### 사전 준비
- Java 21
- MySQL (또는 H2로 대체 가능) + OpenAI API Key

### 1. 저장소 클론 및 빌드
```bash
./gradlew clean build
```

### 2. `src/main/resources/application.properties` 설정
기본값은 외부 개발용 서버(`220.92.57.103`)를 가리키고 있으므로, 로컬에서 실행하려면 아래처럼 본인 환경에 맞게 수정한다.

```properties
# MySQL 사용 시
spring.datasource.url=jdbc:mysql://localhost:3306/teamchat
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.security.user.name=admin
spring.security.user.password=admin

# OpenAI API (GptConfig에서 필요, 현재 프로퍼티 파일에 누락되어 있으므로 직접 추가해야 함)
openai.secret-key=sk-xxxxxxxxxxxxxxxx
openai.model=gpt-3.5-turbo
```

### 3. 실행
```bash
./gradlew bootRun
```
- 기본 포트: `8080` (별도 설정 없음, Spring Boot 기본값)
- 접속: `http://localhost:8080/main.html`

### ⚠️ 로컬 실행 시 참고 사항
- `chatroom.html` 내 WebSocket 접속 주소가 `ws://220.92.57.103:8080/chat?room_id=...`로 하드코딩되어 있어, 로컬에서 그대로 실행하면 실제로는 원격 서버에 연결된다. 로컬 테스트를 하려면 `ws://localhost:8080/chat?room_id=...` 혹은 `window.location.host`를 사용하도록 직접 수정해야 한다.
- AI 답변은 userId=14로 고정되어 있으므로, 해당 유저가 DB에 미리 존재해야 정상적으로 표시된다.

## 🔧 개선하고 싶은 점

이번 프로젝트는 대학 수업의 하나의 과제로 진행하였기에 개발 기간이 비교적 부족하여 부족한 점이 많았다. 내가 생각하는 보완할 점은 다음과 같다

+ 보안의 문제점
  + 현재 닉네임 설정은 자유로이 할 수 있어 겹칠 수 있으며 유저가 구분할 수 있는 방법이 없음.
  + IP를 기반으로 본인을 인증하고 있기에 이후 로그인 기능을 추가해야함. 
+ AI POST 요청 사이 GET 간격
  + 현재 AI 기능을 사용시 잠깐동안 질문을 요청한 유저의 웹은 잠깐 멈추는 문제가 생김. 
+ 파일, 이미지 등의 업로드 미구현
+ AI의 이전 대화 기록 미인식
  + 바로 직전에 한 물음에 대해서 기억하질 못해 제대로 된 활용을 위해서는 질문자의 질문 내용이 계속해서 길어진다는 문제점.
 
다양한 문제점을 스스로도 확인할 수 있었다. 이러한 문제점을 하나씩 해결해가며 해당 프로젝트를 좀 더 진행한다면 좀 더 많은 경험을 할 수 있을 것이다.


최근 나의 대학생활이 끝에 가까워진다는 생각이 들며 현실을 직시해야 한다는 생각이 들었다. 사실 이미 내 꿈이 무엇인지에 대해서 잃어버린지는 꽤 오래 지난 것 같다.

본래 하고 싶었던 것들이 힘들다는 이야기만 듣고 타인의 응원을 받지 못하니 시도조차 하기 싫어지는 경우가 많았다. 무시당하는게 싫고 잘하는 것만 보여주고 싶다.

나는 타인의 눈길에 신경을 많이 쓰는 사람이다. 이제 나는 어른이 되었다는 듯이 우리가 살아가는 현실을 언급하며, 내가 하고 있는 일은 만화같은 곳에서 나오는 꿈 같은 것이 아닌 살아가기 위해 선택한 것이라는 생각으로 스스로에게 말했다.

그러나 이번 프로젝트를 진행하며 오랜만에 순수한 코딩의 즐거움을 느끼게 되었다. 내가 만들어 낸 것이 정상적으로 작동하는 모습. 제법 괜찮은 모습으로 실용적인 작동을 하는 모습을 보니 이것저것 추가해주고 싶었다. 2024-12-13, 지금은 잠깐의 시험 기간을 지난 후 25년도가 되었을 때 나는 이 프로젝트를 보다 더 멋나게 만들어주고 싶다. 이것은 단순 과제 때문에 쓰는 조잡한 소감문이 아니다. 아주 뛰어난 백엔드 프로그래머가 되고 싶으나 이러한 목표는 내겐 아직 이르다. 방금 말한 목표는 미래의 내가 이뤄주도록 지금의 나는 제법 괜찮다는 소리를 듣는, 비전공자인 나의 친구들에게는 대단하다는 이야기를 듣고 감탄을 들을 수 있는 백엔드 프로그래머를 목표로 하자. 다음 1년간은 현실보단 내가 하고 싶은 것들을 쌓아가자. 이러한 것들이 내게 다 도움이 되겠지. 
