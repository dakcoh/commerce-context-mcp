# commerce-context-mcp

[![npm version](https://img.shields.io/npm/v/commerce-context-mcp)](https://www.npmjs.com/package/commerce-context-mcp)
[![license](https://img.shields.io/npm/l/commerce-context-mcp)](https://github.com/dakcoh/commerce-context-mcp/blob/master/LICENSE)

> **한국 국내 이커머스** Java/Spring 백엔드 지식을 Claude Code·Cursor·Claude Desktop 같은 AI 코딩 도구에 연결하는 MCP 서버입니다.

재고·결제·정산·쿠폰의 흔한 함정(오버셀링, 결제 멱등성, 정산 정합성, 선착순 동시성 등)을 **AI가 코드를 만들 때 자동으로 참고**하게 해줍니다.

---

## 1. 먼저 필요한 것

| 항목 | 버전 | 확인 |
|------|------|------|
| Node.js | 16 이상 | `node -v` |
| Java | 17 이상 | `java -version` |

> JAR을 직접 내려받거나 경로를 설정할 필요는 없습니다. 처음 실행할 때 자동으로 받아옵니다.

---

## 2. MCP 클라이언트에 연결하기

사용하는 도구 **한 곳에만** 설정하면 됩니다. (npm 사이트나 Java가 아니라 **MCP 클라이언트** 설정입니다.)

### Claude Code

터미널에서 한 줄이면 끝납니다.

```bash
claude mcp add commerce-context -- npx -y commerce-context-mcp
```

### Cursor · Claude Desktop

아래 내용을 설정 파일에 넣고 프로그램을 **재시작**합니다.

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

설정 파일 위치 (Windows 기준, 없으면 새로 만드세요):

| 클라이언트 | 경로 |
|------------|------|
| Cursor | `C:\Users\<사용자이름>\.cursor\mcp.json` |
| Claude Desktop | `C:\Users\<사용자이름>\AppData\Roaming\Claude\claude_desktop_config.json` |

---

## 3. 설치 상태 확인

```bash
npx -y commerce-context-mcp doctor
```

정상이면 대략 이렇게 나옵니다.

```text
package: commerce-context-mcp@0.0.4
java: ok
release asset: https://github.com/dakcoh/commerce-context-mcp/releases/.../context-engine-0.0.4.jar
release checksum: https://github.com/dakcoh/commerce-context-mcp/releases/.../context-engine-0.0.4.jar.sha256
cached jar: not downloaded
```

`cached jar: not downloaded`도 정상입니다 — `doctor`는 상태만 보고 JAR을 받지 않습니다.

---

## 4. 연결되면 이렇게 물어보세요

- "Spring Boot 쇼핑몰 주문 API 만들 때 빠뜨리기 쉬운 점 알려줘"
- "재고 차감에서 오버셀링 막는 기준 알려줘"
- "결제 웹훅에서 중복 결제를 어떻게 막아?"
- "선착순 쿠폰 발급 동시성 제어 주의점 알려줘"

---

## 제공하는 지식

| 도메인 | 주요 주제 |
|--------|-----------|
| **재고** | 오버셀링, 예약/확정/복구, 동시성(낙관·비관·분산락), 멱등성, Saga |
| **결제** | 상태 머신, 웹훅, 중복 결제, 망취소, 부분 환불, 멱등성 |
| **정산** | 정산 시점·공제·배치, 정합성, 명세서, 세무(부가세·원천징수), 보류, 실지급 |
| **쿠폰/프로모션** | 유효성, 할인 계산, 선착순 발급, 보상 처리, 규칙 엔진 |
| **Java/Spring** | 트랜잭션, JPA, 동시성, API, 메시징, 캐시, 배치, 보안 |

---

## 자주 묻는 것

**Q. 직접 실행했더니 멈춘 것 같아요.**
정상입니다. 이 명령은 MCP 서버를 띄운 뒤 **클라이언트 연결을 기다리는** 상태로 머뭅니다. 보통 직접 실행하지 않고 위처럼 클라이언트 설정에 넣어 씁니다.

**Q. JAR은 어디에 저장되나요?**
직접 옮길 필요 없습니다. 처음 실행 시 사용자 홈에 자동 저장됩니다.
- Windows: `C:\Users\<사용자이름>\.commerce-context-mcp\`
- macOS/Linux: `~/.commerce-context-mcp/`

버전이 올라가면 파일명은 바뀌지만 **설정은 그대로** 두면 항상 최신 버전을 씁니다.

**Q. Java가 없다고 나와요.**
Java 17 이상을 설치한 뒤 `java -version`으로 확인하세요.

**Q. 다운로드만 미리 확인하고 싶어요.**

```bash
npx -y commerce-context-mcp download
```

---

## English

An MCP server launcher for AI coding tools (Claude Code, Cursor, Claude Desktop),
focused on **Korean domestic ecommerce** backend knowledge (VAT, settlement, PG practices).
Configure your client to run `npx -y commerce-context-mcp`. On first run it checks for
Java 17+, downloads the matching GitHub Release JAR into `~/.commerce-context-mcp`,
verifies its SHA-256 checksum, and starts it over stdio.

More: https://github.com/dakcoh/commerce-context-mcp
