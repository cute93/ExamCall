# ExamCall 개발 히스토리

## 2026-06-18

### 요구사항
- 같은 WiFi 환경의 여러 Android 태블릿이 상호 연결하여 메시지를 주고받는 앱
- **서버 태블릿**: 명단 추가/삭제, 호출된 이름 크게 표시 + TTS 읽기
- **클라이언트 태블릿**: 서버 명단을 버튼으로 표시, 버튼 클릭 시 서버에 전송
- DHCP 환경 지원 (IP 자동 발견)
- 서버 태블릿 여러 대 동시 운용 가능

### 기술 결정
- **플랫폼**: Android Kotlin + Jetpack Compose (Material3)
- **통신**: UDP 브로드캐스트(포트 9090)로 서버 자동 발견 → WebSocket(포트 8080)으로 실시간 통신
- **서버 내장**: Node.js 별도 서버 없이 태블릿 자체에 WebSocket 서버 내장 (`Java-WebSocket` 라이브러리)
- **참조 프로젝트**: `F:\study\SsamCall\Client` (동일 기술 스택의 선행 프로젝트)

### 생성된 파일 구조
```
F:\study\ExamCall\
├── settings.gradle.kts
├── build.gradle.kts
├── gradlew / gradlew.bat
├── gradle/
│   ├── libs.versions.toml          (Java-WebSocket 1.5.6 추가)
│   └── wrapper/
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/examcall/
        │   ├── MainActivity.kt          (화면 네비게이션: ModeSelect→Server/ServerList→Client)
        │   ├── ServerViewModel.kt       (서버 로직: UDP 브로드캐스트, WebSocket 서버, TTS)
        │   ├── ClientViewModel.kt       (클라이언트 로직: UDP 발견, WebSocket 연결)
        │   ├── model/
        │   │   └── ServerInfo.kt        (ip, name, lastSeen)
        │   ├── network/
        │   │   ├── EmbeddedWebSocketServer.kt   (Java-WebSocket 내장 서버)
        │   │   ├── ClientWebSocketManager.kt    (OkHttp WebSocket 클라이언트)
        │   │   └── UdpDiscovery.kt              (UDP 브로드캐스트/발견)
        │   └── ui/
        │       ├── theme/
        │       ├── ModeSelectScreen.kt      (역할 선택 + 서버 이름 다이얼로그)
        │       ├── ServerListScreen.kt      (발견된 서버 목록, 자동 갱신)
        │       ├── ServerScreen.kt          (명단 관리 + 64sp 호출 표시)
        │       └── ClientScreen.kt          (명단 버튼 그리드 + 서버 변경)
        └── res/
```

### 메시지 프로토콜
| 방향 | 타입 | 내용 |
|------|------|------|
| UDP 브로드캐스트 | — | `ExamCall:[IP]:[서버이름]` (3초 간격) |
| 서버→클라이언트 | `name_list` | `{"type":"name_list","names":["이름1","이름2"]}` |
| 클라이언트→서버 | `call` | `{"type":"call","name":"홍길동"}` |

### 빌드 결과
```
BUILD SUCCESSFUL in 2m 15s
36 actionable tasks: 13 executed, 23 up-to-date
```
경고: `ClientWebSocketManager.kt`의 WebSocketListener 파라미터 이름 불일치 (기능 무관)
