#!/usr/bin/env node

const fs = require("fs");
const https = require("https");
const os = require("os");
const path = require("path");
const crypto = require("crypto");
const { spawn, spawnSync } = require("child_process");

const PACKAGE_ROOT = path.resolve(__dirname, "..");
const PKG = require(path.join(PACKAGE_ROOT, "package.json"));
const GITHUB_REPO = "dakcoh/commerce-context-mcp";
const JAR_NAME = `context-engine-${PKG.version}.jar`;
const RELEASE_URL = `https://github.com/${GITHUB_REPO}/releases/download/v${PKG.version}/${JAR_NAME}`;
const CHECKSUM_URL = `${RELEASE_URL}.sha256`;
const CACHE_DIR = path.join(os.homedir(), ".commerce-context-mcp");
const JAR_PATH = path.join(CACHE_DIR, JAR_NAME);

function usage() {
  console.log(`commerce-context-mcp ${PKG.version}

Usage:
  commerce-context-mcp [--help]
  commerce-context-mcp [--version]
  commerce-context-mcp download
  commerce-context-mcp doctor

With no command, downloads the release JAR if needed and starts the MCP server
over stdio using Java 17 or newer.`);
}

function parseJavaMajor(output) {
  const match = String(output).match(/version "([^"]+)"/) || String(output).match(/openjdk ([^\s]+)/i);
  if (!match) {
    return null;
  }

  const version = match[1];
  if (version.startsWith("1.")) {
    return Number(version.split(".")[1]);
  }

  return Number(version.split(".")[0]);
}

function javaStatus() {
  const result = spawnSync("java", ["-version"], { encoding: "utf8" });
  const output = `${result.stdout || ""}${result.stderr || ""}`;
  const major = parseJavaMajor(output);

  return {
    ok: result.status === 0 && major !== null && major >= 17,
    major,
    output: output.trim(),
  };
}

function assertJava() {
  const status = javaStatus();
  if (!status.ok) {
    console.error("Java 17 or newer is required.");
    if (status.output) {
      console.error(status.output);
    }
    process.exit(1);
  }
}

function request(url, callback, onError) {
  https
    .get(
      url,
      {
        headers: {
          "User-Agent": `${PKG.name}/${PKG.version}`,
        },
      },
      (response) => {
        if (
          response.statusCode >= 300 &&
          response.statusCode < 400 &&
          response.headers.location
        ) {
          response.resume();
          request(response.headers.location, callback, onError);
          return;
        }

        callback(response);
      }
    )
    .on("error", (error) => {
      onError(error);
    });
}

function formatBytes(bytes) {
  if (bytes < 1024) {
    return `${bytes} B`;
  }

  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`;
  }

  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

const MAX_DOWNLOAD_ATTEMPTS = 3;

// 하나의 다운로드 시도. 성공 시 resolve, 회복 가능한 오류는 reject 한다.
// 잘린 다운로드(수신 바이트 != content-length)를 손상 JAR로 캐시하지 않도록 검증한다.
function downloadAttempt() {
  fs.mkdirSync(CACHE_DIR, { recursive: true });
  const tempPath = `${JAR_PATH}.tmp`;
  fs.rmSync(tempPath, { force: true });

  console.error(`Downloading ${RELEASE_URL}`);
  return new Promise((resolve, reject) => {
    request(RELEASE_URL, (response) => {
      if (response.statusCode !== 200) {
        response.resume();
        reject(new Error(`Release asset is not available. HTTP ${response.statusCode}`));
        return;
      }

      const file = fs.createWriteStream(tempPath);
      const total = Number(response.headers["content-length"] || 0);
      let downloaded = 0;
      let lastPercent = -1;
      let lastLoggedAt = Date.now();

      response.on("data", (chunk) => {
        downloaded += chunk.length;

        if (total > 0) {
          const percent = Math.floor((downloaded / total) * 100);
          if (percent >= lastPercent + 10 || percent === 100) {
            lastPercent = percent;
            console.error(
              `Download progress: ${percent}% (${formatBytes(downloaded)} / ${formatBytes(total)})`
            );
          }
          return;
        }

        const now = Date.now();
        if (now - lastLoggedAt >= 3000) {
          lastLoggedAt = now;
          console.error(`Downloaded ${formatBytes(downloaded)}...`);
        }
      });

      response.on("error", (error) => {
        file.destroy();
        fs.rmSync(tempPath, { force: true });
        reject(error);
      });

      response.pipe(file);
      file.on("finish", () => {
        file.close(() => {
          // 무결성 검증: 서버가 알려준 길이만큼 받지 못했으면 손상으로 간주한다.
          if (total > 0 && downloaded !== total) {
            fs.rmSync(tempPath, { force: true });
            reject(new Error(
              `Incomplete download: received ${downloaded} of ${total} bytes`
            ));
            return;
          }
          fs.renameSync(tempPath, JAR_PATH);
          console.error(`Cached JAR: ${JAR_PATH}`);
          resolve();
        });
      });
      file.on("error", (error) => {
        fs.rmSync(tempPath, { force: true });
        reject(new Error(`Failed to save ${JAR_NAME}: ${error.message}`));
      });
    }, reject);
  });
}

async function downloadJar() {
  for (let attempt = 1; attempt <= MAX_DOWNLOAD_ATTEMPTS; attempt += 1) {
    try {
      await downloadAttempt();
      return;
    } catch (error) {
      const message = error && error.message ? error.message : String(error);
      if (attempt < MAX_DOWNLOAD_ATTEMPTS) {
        console.error(`Download attempt ${attempt} failed: ${message}. Retrying...`);
        continue;
      }
      console.error(`Failed to download ${JAR_NAME} after ${MAX_DOWNLOAD_ATTEMPTS} attempts: ${message}`);
      process.exit(1);
    }
  }
}

// 작은 텍스트 자산(.sha256)을 받아온다. 자산이 없으면(404) null 을 반환한다.
function fetchText(url) {
  return new Promise((resolve, reject) => {
    request(
      url,
      (response) => {
        if (response.statusCode === 404) {
          response.resume();
          resolve(null);
          return;
        }
        if (response.statusCode !== 200) {
          response.resume();
          reject(new Error(`HTTP ${response.statusCode}`));
          return;
        }
        let body = "";
        response.setEncoding("utf8");
        response.on("data", (chunk) => {
          body += chunk;
        });
        response.on("end", () => resolve(body));
        response.on("error", reject);
      },
      reject
    );
  });
}

function sha256OfFile(filePath) {
  return crypto.createHash("sha256").update(fs.readFileSync(filePath)).digest("hex");
}

// 다운로드한 JAR 을 릴리즈에 게시된 .sha256 과 대조한다.
// 체크섬 자산이 없으면(구버전 릴리즈) 경고만 남기고 진행해 하위 호환을 유지한다.
async function verifyChecksum() {
  let checksumText;
  try {
    checksumText = await fetchText(CHECKSUM_URL);
  } catch (error) {
    console.error(`Warning: could not fetch checksum (${error.message}). Skipping integrity check.`);
    return;
  }

  if (!checksumText) {
    console.error("Warning: no .sha256 published for this release. Skipping integrity check.");
    return;
  }

  const match = checksumText.trim().match(/[a-fA-F0-9]{64}/);
  if (!match) {
    console.error("Warning: malformed checksum file. Skipping integrity check.");
    return;
  }

  const expected = match[0].toLowerCase();
  const actual = sha256OfFile(JAR_PATH).toLowerCase();
  if (actual !== expected) {
    fs.rmSync(JAR_PATH, { force: true });
    console.error(`Checksum mismatch for ${JAR_NAME}. Deleted the corrupt cache; please retry.`);
    console.error(`  expected: ${expected}`);
    console.error(`  actual:   ${actual}`);
    process.exit(1);
  }

  console.error(`Checksum verified (sha256: ${actual}).`);
}

async function ensureJar() {
  if (!fs.existsSync(JAR_PATH)) {
    await downloadJar();
    await verifyChecksum();
    return;
  }

  console.error(`Using cached JAR: ${JAR_PATH}`);
}

async function doctor() {
  const status = javaStatus();
  console.log(`package: ${PKG.name}@${PKG.version}`);
  console.log(`java: ${status.ok ? "ok" : "missing or too old"}`);
  if (status.output) {
    console.log(status.output.split(/\r?\n/)[0]);
  }
  console.log(`release asset: ${RELEASE_URL}`);
  console.log(`release checksum: ${CHECKSUM_URL}`);
  console.log(`cached jar: ${fs.existsSync(JAR_PATH) ? JAR_PATH : "not downloaded"}`);

  process.exit(status.ok ? 0 : 1);
}

async function downloadOnly() {
  assertJava();
  await ensureJar();
}

async function startServer(args) {
  assertJava();
  await ensureJar();
  console.error("Starting MCP server over stdio. This process stays running while the MCP client is connected.");

  // cwd 를 캐시 디렉터리로 고정한다.
  // 서버는 로그를 상대경로 logs/ 에 기록하므로, cwd 를 지정하지 않으면
  // MCP 클라이언트가 실행된 위치(사용자 프로젝트 루트 등)에 logs/ 가 생긴다.
  const child = spawn(
    "java",
    ["-jar", JAR_PATH, "--spring.profiles.active=stdio", ...args],
    {
      stdio: "inherit",
      cwd: CACHE_DIR,
    }
  );

  child.on("exit", (code, signal) => {
    if (signal) {
      process.kill(process.pid, signal);
      return;
    }

    process.exit(code || 0);
  });
}

async function main() {
  const args = process.argv.slice(2);
  const command = args[0];

  if (command === "--help" || command === "-h") {
    usage();
    return;
  }

  if (command === "--version" || command === "-v") {
    console.log(PKG.version);
    return;
  }

  if (command === "doctor") {
    await doctor();
    return;
  }

  if (command === "download" || command === "--download-only") {
    await downloadOnly();
    return;
  }

  await startServer(args);
}

main().catch((error) => {
  console.error(error && error.stack ? error.stack : String(error));
  process.exit(1);
});
