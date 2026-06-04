# Commerce Context Engine — Operations

> npm 공개 배포, 로컬 개발 검증, JAR 릴리즈 절차를 한 곳에 모은 운영 문서.

## 사용자 설치 확인

공개 사용자는 별도 JAR 경로를 설정하지 않고 `npx`로 실행한다.

```powershell
npx commerce-context-mcp --help
npx commerce-context-mcp doctor
```

MCP 클라이언트 설정:

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

동작 순서:
- Java 17 이상 확인
- GitHub Release에서 `context-engine-{version}.jar` 다운로드
- 사용자 캐시에 JAR 저장
- STDIO 프로파일로 MCP 서버 실행

## 로컬 검증

```powershell
.\gradlew.bat test --no-daemon
.\gradlew.bat validateKnowledge --no-daemon
```

확인 항목:
- YAML 지식이 `@ConfigurationProperties`로 정상 바인딩되는지
- 지식 ID 중복, 허용 category, 필수 필드, 빈 태그가 없는지
- MCP 도구 31개가 등록되는지
- 검색 도구가 핵심 키워드에 맞는 지식을 반환하는지

## HTTP/SSE 모드

```powershell
.\gradlew.bat bootRun
curl http://localhost:8080/actuator/health
```

Cursor 연결 예:

```json
{
  "mcpServers": {
    "commerce-context": {
      "url": "http://localhost:8080/sse",
      "type": "sse"
    }
  }
}
```

## 개발용 STDIO 모드

일반 사용자는 이 방식을 쓰지 않는다. 릴리즈 전 로컬 JAR를 직접 검증할 때만 사용한다.

```powershell
.\gradlew.bat bootJar --no-daemon
java -jar build\libs\context-engine-0.0.1-SNAPSHOT.jar --spring.profiles.active=stdio
```

Claude Code 연결 예:

```json
{
  "mcpServers": {
    "commerce-context": {
      "command": "java",
      "args": [
        "-jar",
        "C:\\project\\context-engine\\build\\libs\\context-engine-0.0.1-SNAPSHOT.jar",
        "--spring.profiles.active=stdio"
      ]
    }
  }
}
```

STDIO 모드에서는 stdout이 MCP 통신에 사용되므로 로그는 `logs/context-engine-stdio.log`에 기록된다.

## 수동 질문 예시

- "Spring Boot 쇼핑몰 주문 API 만들 때 빠뜨리기 쉬운 점 알려줘"
- "재고 차감 로직 만들 때 오버셀링 방지 기준 알려줘"
- "이커머스에서 장바구니와 주문을 왜 분리해야 해?"
- "Java에서 Optional과 예외 경계를 어떻게 잡아야 해?"

## 릴리즈 구조

| 리포 | 공개 여부 | 용도 |
|------|----------|------|
| `dakcoh/context-engine` | public | 소스 코드 + JAR 릴리즈 + npm 패키지 |

## 최초 준비

1. npm 계정을 만든다.
2. 첫 배포는 로컬에서 `npm.cmd publish --access public`로 진행할 수 있다.
3. 자동 배포를 사용하려면 npm `Automation` access token을 발급한다.
4. GitHub Actions Secret에 `NPM_TOKEN`을 등록한다.

## 버전 릴리즈

```powershell
cd C:\project\context-engine
.\gradlew.bat test --no-daemon
.\gradlew.bat validateKnowledge --no-daemon
.\gradlew.bat bootJar "-Pversion=0.0.1" --no-daemon
```

생성 파일:

```powershell
build\libs\context-engine-0.0.1.jar
```

GitHub Release:

1. `dakcoh/context-engine` 리포에서 release를 만든다.
2. tag와 title은 `v0.0.1`로 맞춘다.
3. `build/libs/context-engine-0.0.1.jar`를 첨부한다.

태그 푸시:

```powershell
git tag v0.0.1
git push origin v0.0.1
```

배포 확인:

```powershell
cd npm
$env:npm_config_cache='C:\project\context-engine\.npm-cache'
npm.cmd pack --dry-run
cd ..
npm.cmd view commerce-context-mcp version
npx.cmd commerce-context-mcp --help
npx.cmd commerce-context-mcp doctor
```

## npm 실행기 동작

`npx commerce-context-mcp`는 아래 순서로 동작한다.

1. Java 17 이상 확인
2. GitHub Release에서 `context-engine-{version}.jar` 다운로드
3. 사용자 캐시 경로에 JAR 저장
4. STDIO 프로파일로 JAR 실행

릴리즈 후 사용자 테스트는 `npx`를 우선 사용한다.
릴리즈 전 내부 검증은 개발용 STDIO 모드로 로컬 JAR를 직접 실행해도 된다.

## npm 실행기 사용자 명령

```powershell
npx commerce-context-mcp --help
npx commerce-context-mcp --version
npx commerce-context-mcp doctor
```

`doctor`는 Java 17 이상 설치 여부, JAR 캐시 상태, 다운로드 예정 URL을 확인한다.
외부 사용자 경험 고도화 계획은 `docs/EXTERNAL_USAGE_PLAN.md`를 따른다.

## 릴리즈 전 점검

- 지식 YAML 내용 업데이트 여부 확인
- `.\gradlew.bat validateKnowledge --no-daemon` 통과
- 전체 테스트 통과
- JAR 빌드 성공
- public GitHub Release에 `context-engine-{version}.jar` 첨부 확인
- `npm` 디렉토리에서 `npm.cmd pack --dry-run` 통과
- `npm.cmd view commerce-context-mcp version` 확인
- `npx.cmd commerce-context-mcp doctor` 확인
- MCP 수동 질문 3개 이상 확인
- `npm/package.json` 버전과 JAR 릴리즈 버전 일치
