# External Usage

> 외부 사용자는 npm에 배포된 `commerce-context-mcp`를 `npx`로 실행한다.

## 현재 배포 상태

`commerce-context-mcp@0.0.1`은 npm에 공개되어 있다.
JAR는 GitHub Release에서 다운로드된다.

```powershell
npm.cmd view commerce-context-mcp version
npx.cmd commerce-context-mcp --help
npx.cmd commerce-context-mcp doctor
```

## 사용자 경험

### 1. 기본 실행

```bash
npx commerce-context-mcp
```

동작:
- Java 17 이상 확인
- GitHub Release에서 JAR 다운로드
- 사용자 캐시에 저장
- STDIO MCP 서버로 실행

### 2. 도움말

```bash
npx commerce-context-mcp --help
```

사용자가 설치 전에 필요한 Java 버전, MCP 설정 예시, 진단 명령을 볼 수 있어야 한다.

### 3. 버전 확인

```bash
npx commerce-context-mcp --version
```

npm 패키지 버전을 출력한다.

### 4. 진단

```bash
npx commerce-context-mcp doctor
```

확인 항목:
- Node.js 버전
- Java 설치 여부와 major version
- JAR 캐시 존재 여부
- 다운로드 예정 URL
- Claude/Cursor 설정 예시

`doctor`는 네트워크 다운로드를 강제하지 않는다.
릴리즈 파일 존재 확인 같은 온라인 검증은 나중에 `doctor --online`으로 확장한다.

## MCP 클라이언트 설정

### Claude Code / Cursor 공통 STDIO 설정

```json
{
  "mcpServers": {
    "commerce-context": {
      "command": "npx",
      "args": ["-y", "commerce-context-mcp"]
    }
  }
}
```

## 좋은 첫 질문

- "Spring Boot 쇼핑몰 주문 API 만들 때 빠뜨리기 쉬운 점 알려줘"
- "재고 차감 로직에서 오버셀링을 막는 기준 알려줘"
- "결제 웹훅 처리에서 중복 결제를 어떻게 막아?"
- "이커머스에서 장바구니와 주문은 왜 분리해야 해?"
- "Java에서 Optional과 예외 경계를 어떻게 잡아야 해?"

## npm 실행기 명령 목록

| 명령 | 목적 |
|------|------|
| `commerce-context-mcp` | STDIO MCP 서버 실행 |
| `commerce-context-mcp --help` | 도움말 출력 |
| `commerce-context-mcp --version` | npm 패키지 버전 출력 |
| `commerce-context-mcp doctor` | 로컬 실행 환경 점검 |

## 후속 개선 후보

- `doctor --online`: GitHub Release JAR 접근 가능 여부 확인
- `cache clean`: 다운로드된 JAR 캐시 제거
- `serve --http --port 8080`: npm 실행기에서 HTTP/SSE 모드 실행
- 설치 문서에 Claude Code, Cursor, VS Code별 예시 추가
- 실제 `npx` 기반 smoke test 자동화

## Core 모듈 분리와의 관계

`docs/CORE_MODULE_PLAN.md`는 장기 고도화 계획으로 유지한다.
외부 사용성은 npm 배포로 1차 완료했다.
이후에는 릴리즈 smoke test, 설치 문서 보강, core 구조화 API 도입 순서로 개선한다.
