#!/usr/bin/env node

const fs = require("fs");
const https = require("https");
const os = require("os");
const path = require("path");
const { spawn, spawnSync } = require("child_process");

const PACKAGE_ROOT = path.resolve(__dirname, "..");
const PKG = require(path.join(PACKAGE_ROOT, "package.json"));
const GITHUB_REPO = "dakcoh/commerce-context-mcp";
const JAR_NAME = `context-engine-${PKG.version}.jar`;
const RELEASE_URL = `https://github.com/${GITHUB_REPO}/releases/download/v${PKG.version}/${JAR_NAME}`;
const CACHE_DIR = path.join(os.homedir(), ".commerce-context-mcp");
const JAR_PATH = path.join(CACHE_DIR, JAR_NAME);

function usage() {
  console.log(`commerce-context-mcp ${PKG.version}

Usage:
  commerce-context-mcp [--help]
  commerce-context-mcp [--version]
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

function request(url, callback) {
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
          request(response.headers.location, callback);
          return;
        }

        callback(response);
      }
    )
    .on("error", (error) => {
      console.error(`Failed to download ${JAR_NAME}: ${error.message}`);
      process.exit(1);
    });
}

function downloadJar() {
  fs.mkdirSync(CACHE_DIR, { recursive: true });
  const tempPath = `${JAR_PATH}.tmp`;

  console.error(`Downloading ${RELEASE_URL}`);
  return new Promise((resolve) => {
    request(RELEASE_URL, (response) => {
      if (response.statusCode !== 200) {
        response.resume();
        console.error(`Release asset is not available. HTTP ${response.statusCode}`);
        process.exit(1);
      }

      const file = fs.createWriteStream(tempPath);
      response.pipe(file);
      file.on("finish", () => {
        file.close(() => {
          fs.renameSync(tempPath, JAR_PATH);
          resolve();
        });
      });
      file.on("error", (error) => {
        fs.rmSync(tempPath, { force: true });
        console.error(`Failed to save ${JAR_NAME}: ${error.message}`);
        process.exit(1);
      });
    });
  });
}

async function ensureJar() {
  if (!fs.existsSync(JAR_PATH)) {
    await downloadJar();
  }
}

async function doctor() {
  const status = javaStatus();
  console.log(`package: ${PKG.name}@${PKG.version}`);
  console.log(`java: ${status.ok ? "ok" : "missing or too old"}`);
  if (status.output) {
    console.log(status.output.split(/\r?\n/)[0]);
  }
  console.log(`release asset: ${RELEASE_URL}`);
  console.log(`cached jar: ${fs.existsSync(JAR_PATH) ? JAR_PATH : "not downloaded"}`);

  process.exit(status.ok ? 0 : 1);
}

async function startServer(args) {
  assertJava();
  await ensureJar();

  const child = spawn(
    "java",
    ["-jar", JAR_PATH, "--spring.profiles.active=stdio", ...args],
    {
      stdio: "inherit",
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

  await startServer(args);
}

main().catch((error) => {
  console.error(error && error.stack ? error.stack : String(error));
  process.exit(1);
});
