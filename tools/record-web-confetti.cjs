#!/usr/bin/env node

const fs = require("node:fs");
const path = require("node:path");
const { pathToFileURL } = require("node:url");
const { chromium } = require("playwright");

async function main() {
  const outputPath = process.argv[2];
  if (!outputPath) {
    throw new Error("Usage: node tools/record-web-confetti.cjs <output-video-path>");
  }

  const absoluteOut = path.resolve(outputPath);
  const outputDir = path.dirname(absoluteOut);
  fs.mkdirSync(outputDir, { recursive: true });

  const repoRoot = path.resolve(__dirname, "..");
  const htmlPath = path.join(repoRoot, "ConfettiPrototype", "Resources", "confetti.html");
  if (!fs.existsSync(htmlPath)) {
    throw new Error(`Missing web source: ${htmlPath}`);
  }

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    viewport: { width: 700, height: 1000 },
    recordVideo: {
      dir: outputDir,
      size: { width: 700, height: 1000 },
    },
  });

  const page = await context.newPage();
  const fileUrl = pathToFileURL(htmlPath).toString();
  await page.goto(fileUrl, { waitUntil: "load" });
  await page.waitForSelector(".confetti-trigger", { timeout: 15000 });
  await page.waitForTimeout(500);

  await page.click(".confetti-trigger");
  await page.waitForTimeout(6000);

  const recordedPath = await page.video().path();
  await context.close();
  await browser.close();

  fs.copyFileSync(recordedPath, absoluteOut);
  console.log(`Recorded web confetti video: ${absoluteOut}`);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
