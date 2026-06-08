# Commerce Context MCP — Operations

> npm 공개 배포, 로컬 개발 검증, JAR 릴리즈 절차를 한 곳에 모은 운영 문서.

## 사용자 설치 확인

공개 사용자는 별도 JAR 경로를 설정하지 않고 `npx`로 실행한다.

```powershell
npx -y commerce-context-mcp --help
npx -y commerce-context-mcp doctor
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
- GitHub Release에서 `context-engine-{version}.jar` 다운로드 (잘린 다운로드 감지·최대 3회 재시도)
- 릴리즈의 `.sha256` 자산으로 무결성 검증 (불일치 시 캐시 삭제 후 실패, 자산 없으면 경고 후 진행)
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
- MCP 도구 33개가 등록되는지
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
Get-ChildItem build\libs\*.jar
java -jar build\libs\<jar-file> --spring.profiles.active=stdio
```

Claude Code 연결 예:

```json
{
  "mcpServers": {
    "commerce-context": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/context-engine/build/libs/context-engine-{version}-SNAPSHOT.jar",
        "--spring.profiles.active=stdio"
      ]
    }
  }
}
```

> `{version}`과 경로는 실제 로컬 환경에 맞게 변경한다.

STDIO 모드에서는 stdout이 MCP 통신에 사용되므로 로그는 `logs/context-engine-stdio.log`에 기록된다.

## 수동 질문 예시

- "Spring Boot 쇼핑몰 주문 API 만들 때 빠뜨리기 쉬운 점 알려줘"
- "재고 차감 로직 만들 때 오버셀링 방지 기준 알려줘"
- "이커머스에서 장바구니와 주문을 왜 분리해야 해?"
- "Java에서 Optional과 예외 경계를 어떻게 잡아야 해?"

## 릴리즈 구조

| 리포 | 공개 여부 | 용도 |
|------|----------|------|
| `dakcoh/commerce-context-mcp` | public | 소스 코드 + JAR 릴리즈 + npm 패키지 |

## 리포 이름 변경 검토

현재 npm 패키지명은 `commerce-context-mcp`이고, GitHub 리포도 `dakcoh/commerce-context-mcp`이다.
패키지명과 리포명이 맞아 외부 사용자가 npm 패키지와 GitHub 프로젝트를 같은 이름으로 찾기 쉽다.

판단 기준:
- `commerce-context-mcp`: npm 패키지명과 맞고, 이커머스 MCP라는 목적이 가장 잘 드러난다.
- `context-engine`: MCP 외에도 core 라이브러리나 지식 엔진으로 확장하기 좋지만 npm 패키지명과는 다르다.
- `context-engine-mcp`: MCP 서버라는 성격이 분명하지만 범용 이름이라 이커머스 타깃이 덜 드러난다.

현재는 `commerce-context-mcp`를 기준 이름으로 유지한다.

리포명을 다시 변경한다면 아래 항목을 함께 수정한다.

- GitHub Repository Settings에서 리포명 변경
- `npm/package.json`의 `homepage`, `bugs.url`, `repository.url`
- `npm/bin/index.js`의 `GITHUB_REPO`
- `.github/workflows/release.yml`의 GitHub Release JAR URL
- `README.md`, `docs/*.md`의 GitHub URL과 리포명
- GitHub Release URL이 새 리포명으로 정상 리다이렉트 또는 다운로드되는지 확인
- `npx -y commerce-context-mcp doctor` 출력의 Release URL 확인
- 원격 저장소 URL 재설정:

```powershell
git remote set-url origin https://github.com/dakcoh/{new-repo-name}.git
```

중요: npm은 같은 버전을 다시 publish할 수 없다.
리포명 변경이나 문서/실행기 변경을 npm 사용자에게 반영하려면 반드시 새 버전으로 Release JAR와 npm 패키지를 함께 배포한다.

## 최초 준비

1. npm 계정을 만든다.
2. 첫 배포는 로컬에서 `npm.cmd publish --access public`로 진행할 수 있다.
3. 자동 배포를 사용하려면 npm `Automation` access token을 발급한다.
4. GitHub Actions Secret에 `NPM_TOKEN`을 등록한다.

## 버전 릴리즈

npm에 한 번 publish된 버전은 덮어쓸 수 없다.
릴리즈할 때마다 아직 npm에 없는 새 버전을 정한다.

```powershell
cd <repo-root>
$version = "<new-version>"
.\gradlew.bat test --no-daemon
.\gradlew.bat validateKnowledge --no-daemon
.\gradlew.bat bootJar "-Pversion=$version" --no-daemon
```

생성 파일:

```powershell
build\libs\context-engine-<new-version>.jar
```

GitHub Release:

1. `dakcoh/commerce-context-mcp` 리포에서 release를 만든다.
2. tag와 title은 `v<new-version>`으로 맞춘다.
3. `build/libs/context-engine-<new-version>.jar`를 첨부한다.
4. Release를 Publish한다.

Release가 Publish되면 `.github/workflows/release.yml`이 실행된다.
워크플로우는 지식 검증과 JAR 빌드를 다시 수행한 뒤, GitHub Release에 첨부된 JAR을 내려받아
`context-engine-{version}.jar.sha256` 체크섬 자산을 생성·업로드하고, npm 패키지를 publish한다.
체크섬은 CI 빌드 산출물이 아니라 릴리즈에 올라간 JAR 자체에서 계산하므로 사용자가 받는 파일과 항상 일치한다.
이때 `npm/package.json`의 버전은 GitHub Actions runner 안에서 Release tag 버전으로 동기화된다.
따라서 저장소의 기본 `npm/package.json` 버전은 릴리즈 tag와 항상 같을 필요는 없다.

배포 확인:

```powershell
cd npm
$env:npm_config_cache='.npm-cache'
npm.cmd pack --dry-run
cd ..
npm.cmd view commerce-context-mcp version
npx.cmd -y commerce-context-mcp --help
npx.cmd -y commerce-context-mcp doctor
npx.cmd -y commerce-context-mcp download
```

## npm 실행기 동작

`npx -y commerce-context-mcp`는 아래 순서로 동작한다.

1. Java 17 이상 확인
2. GitHub Release에서 `context-engine-{version}.jar` 다운로드 (잘린 다운로드 감지·최대 3회 재시도)
3. 릴리즈의 `context-engine-{version}.jar.sha256` 으로 무결성 검증
   - 해시 불일치: 손상 캐시 삭제 후 종료(코드 1)
   - `.sha256` 자산 없음(구버전 릴리즈): 경고만 남기고 진행
4. 사용자 캐시 경로에 JAR 저장
5. STDIO 프로파일로 JAR 실행

릴리즈 후 사용자 테스트는 `npx`를 우선 사용한다.
릴리즈 전 검증은 개발용 STDIO 모드로 로컬 JAR를 직접 실행해도 된다.

## npm 실행기 사용자 명령

```powershell
npx -y commerce-context-mcp --help
npx -y commerce-context-mcp --version
npx -y commerce-context-mcp doctor
npx -y commerce-context-mcp download
```

`doctor`는 Java 17 이상 설치 여부, JAR 캐시 상태, 다운로드 예정 URL을 확인한다.
`download`는 MCP 서버를 실행하지 않고 JAR 다운로드와 캐시 저장만 확인한다.
외부 사용자 경험 고도화 항목은 이 문서의 릴리즈 전 점검과 npm 실행기 사용자 명령을 기준으로 관리한다.

## 릴리즈 전 점검

- 지식 YAML 내용 업데이트 여부 확인
- `.\gradlew.bat validateKnowledge --no-daemon` 통과
- 전체 테스트 통과
- JAR 빌드 성공
- public GitHub Release에 `context-engine-{version}.jar` 첨부 확인
- 워크플로우가 `context-engine-{version}.jar.sha256` 자산을 업로드했는지 확인 (npm 무결성 검증의 기준)
- `npm` 디렉토리에서 `npm.cmd pack --dry-run` 통과
- `npm.cmd view commerce-context-mcp version` 확인
- `npx.cmd -y commerce-context-mcp doctor` 확인
- `npx.cmd -y commerce-context-mcp download` 확인
- MCP 수동 질문 3개 이상 확인
- GitHub Actions 로그의 `npm version:` 값과 Release tag/JAR 버전 일치
