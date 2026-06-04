# commerce-context-mcp

Java/Spring 이커머스 백엔드 지식을 AI 코딩 도구에서 사용할 수 있게 해주는 MCP 서버입니다.

Claude Code, Cursor, Claude Desktop 같은 MCP 클라이언트에 연결해서 사용합니다.

## 먼저 필요한 것

아래 두 개가 설치되어 있어야 합니다.

- Node.js 16 이상
- Java 17 이상

설치가 되어 있는지 확인하려면 터미널에서 실행합니다.

```bash
node -v
java -version
```

## 가장 먼저 확인하기

터미널에서 아래 명령을 실행합니다.

```bash
npx -y commerce-context-mcp doctor
```

정상이라면 대략 이런 내용이 나옵니다.

```text
package: commerce-context-mcp@<version>
java: ok
release asset: https://github.com/dakcoh/commerce-context-mcp/releases/download/...
cached jar: not downloaded
```

`cached jar: not downloaded`가 나와도 정상입니다.
`doctor` 명령은 설치 상태만 확인하고 JAR 파일은 다운로드하지 않습니다.

## 어디에 설정하나요?

이 설정은 npm 사이트나 Java 설정에 넣는 것이 아닙니다.
사용하려는 MCP 클라이언트 설정에 넣습니다.

## Claude Code에서 사용하기

Claude Code를 사용한다면 터미널에서 아래 명령을 실행합니다.

```bash
claude mcp add commerce-context -- npx -y commerce-context-mcp
```

이 방법이 가장 간단합니다.
직접 JSON 파일을 수정하지 않아도 됩니다.

## Cursor에서 사용하기

Windows 기준으로 아래 파일을 엽니다.

```text
C:\Users\<사용자이름>\.cursor\mcp.json
```

파일이 없으면 새로 만듭니다.
내용은 아래처럼 작성합니다.

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

저장한 뒤 Cursor를 다시 시작합니다.

## Claude Desktop에서 사용하기

Windows 기준으로 아래 파일을 엽니다.

```text
C:\Users\<사용자이름>\AppData\Roaming\Claude\claude_desktop_config.json
```

파일 내용에 아래 설정을 넣습니다.

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

저장한 뒤 Claude Desktop을 다시 시작합니다.

## JAR 파일은 어디에 저장되나요?

직접 JAR 파일을 옮기거나 설정할 필요는 없습니다.

MCP 클라이언트가 `npx -y commerce-context-mcp`를 실행하면 npm 패키지가 자동으로 JAR 파일을 다운로드합니다.
다운로드된 JAR는 사용자 홈 디렉터리 아래에 저장됩니다.

Windows 예시:

```text
C:\Users\<사용자이름>\.commerce-context-mcp\context-engine-<version>.jar
```

macOS/Linux 예시:

```text
/home/<user>/.commerce-context-mcp/context-engine-<version>.jar
```

버전이 올라가면 파일명도 바뀝니다.
npm 패키지 버전이 `<version>`이면 `context-engine-<version>.jar`를 사용합니다.

하지만 MCP 설정은 바꾸지 않아도 됩니다.

```json
"args": ["-y", "commerce-context-mcp"]
```

위 설정을 그대로 두면 최신 npm 버전이 실행되고, 그 버전에 맞는 JAR를 자동으로 사용합니다.

## 터미널에서 직접 실행하면 왜 멈춘 것처럼 보이나요?

아래 명령은 일반 프로그램처럼 결과를 출력하고 끝나는 명령이 아닙니다.

```bash
npx -y commerce-context-mcp
```

이 명령은 MCP 서버를 실행하고, MCP 클라이언트가 연결되기를 기다립니다.
그래서 터미널에서 직접 실행하면 아무 일도 안 일어나는 것처럼 보일 수 있습니다.
정상 동작입니다.

사용자는 보통 이 명령을 직접 실행하지 않고, Cursor나 Claude 같은 MCP 클라이언트 설정에 넣어서 사용합니다.

## 자주 헷갈리는 것

`java -jar <cached-jar> ...`를 그대로 입력하면 안 됩니다.

`<cached-jar>`는 설명용 표시입니다.
실제 경로를 뜻합니다.

예를 들어 직접 실행한다면 이런 형태여야 합니다.

```bash
java -jar C:\Users\<사용자이름>\.commerce-context-mcp\context-engine-<version>.jar --spring.profiles.active=stdio
```

하지만 일반 사용자는 직접 실행하지 않아도 됩니다.
MCP 클라이언트 설정에 `npx -y commerce-context-mcp`만 넣으면 됩니다.

## 문제가 생기면

Java가 없다고 나오면 Java 17 이상을 설치합니다.

```bash
java -version
```

다운로드만 따로 확인하고 싶다면 아래 명령을 실행합니다.

```bash
npx -y commerce-context-mcp download
```

JAR가 다운로드됐는지 확인하려면 Windows에서 아래 폴더를 확인합니다.

```text
C:\Users\<사용자이름>\.commerce-context-mcp
```

## English reference

This package is an MCP server launcher. Configure your MCP client to run
`npx -y commerce-context-mcp`. The launcher downloads the matching GitHub Release
JAR into `~/.commerce-context-mcp` and starts it with Java.

More: https://github.com/dakcoh/commerce-context-mcp
