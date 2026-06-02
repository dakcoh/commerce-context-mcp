# 릴리즈 가이드

> `context-engine` (private) 소스에서 `commerce-context-mcp` (public) 배포까지의 전체 절차

---

## 리포 구조

| 리포 | 공개 여부 | 용도 |
|------|----------|------|
| `dakcoh/context-engine` | **private** | 소스 코드 |
| `dakcoh/commerce-context-mcp` | **public** | JAR 릴리즈 + npm 패키지 |

---

## 사전 준비 (최초 1회)

### 1. npm 계정 + 토큰 발급
1. https://www.npmjs.com/signup 에서 계정 생성
2. npmjs.com → 우상단 프로필 → **Access Tokens** → **Generate New Token**
3. 타입: `Automation` 선택 → 토큰 복사

### 2. GitHub Secret 등록
`context-engine` (private 리포) → **Settings → Secrets and variables → Actions**
- `NPM_TOKEN` = 위에서 복사한 npm 토큰

### 3. public 릴리즈 리포 생성
GitHub에서 `commerce-context-mcp` 리포를 **public**으로 생성 (빈 리포로 시작 OK)

---

## 릴리즈 절차 (버전 올릴 때마다)

### Step 1. JAR 빌드 (로컬)

```bash
cd C:\project\context-engine

# 버전 번호 결정 (예: 0.0.1)
.\gradlew.bat bootJar "-Pversion=0.0.1" --no-daemon

# 생성 위치 확인
ls build/libs/context-engine-0.0.1.jar
```

### Step 2. public 리포에 GitHub Release 생성 + JAR 업로드

1. https://github.com/dakcoh/commerce-context-mcp/releases/new 접속
2. **Tag**: `v0.0.1` 입력 (새 태그 생성)
3. **Title**: `v0.0.1`
4. **Description**: 변경 사항 작성
5. **Attach binaries**: `build/libs/context-engine-0.0.1.jar` 드래그 앤 드롭
6. **Publish release** 클릭

### Step 3. private 리포에 태그 푸시 → npm 자동 배포

```bash
# private 소스 리포에 태그 푸시
git tag v0.0.1
git push origin v0.0.1

# → GitHub Actions 자동 실행:
#   1. 전체 테스트 78개 통과 확인
#   2. npm publish (commerce-context-mcp@0.0.1)
```

### Step 4. 확인

```bash
# npm에 배포됐는지 확인
npm view commerce-context-mcp version

# 직접 테스트
npx commerce-context-mcp
```

---

## 사용자 세팅 (배포 완료 후)

**Claude Code** (`.claude/settings.json`):
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

**Cursor** (`.cursor/mcp.json`):
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

---

## 버전 업 시 체크리스트

```
[ ] 지식 YAML 내용 업데이트 (필요 시)
[ ] 테스트 통과 확인: ./gradlew test
[ ] JAR 빌드: ./gradlew bootJar "-Pversion=X.Y.Z"
[ ] commerce-context-mcp 리포에 GitHub Release + JAR 업로드
[ ] git tag vX.Y.Z && git push origin vX.Y.Z
[ ] npm view commerce-context-mcp version 으로 배포 확인
```
