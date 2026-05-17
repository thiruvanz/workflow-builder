import { defineConfig, devices } from "@playwright/test";

/**
 * UI E2E: same paths a human uses in the browser (Vite → /ui-api → frontend-service).
 *
 * Prereqs before `npm run test:e2e`:
 * 1. frontend-service on 8088 (bootRun or Docker with 8088 published)
 * 2. workflow, execution, monitoring reachable from that BFF (compose or local ports)
 * 3. Vite dev server on 5173 — started here via webServer, or reuse with PW_SKIP_WEBSERVER=1
 */
export default defineConfig({
  testDir: "e2e",
  timeout: 90_000,
  expect: { timeout: 25_000 },
  forbidOnly: !!process.env.CI,
  retries: 0,
  use: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL || "http://127.0.0.1:5173",
    trace: "on-first-retry",
    screenshot: "only-on-failure",
    video: "retain-on-failure",
  },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
  webServer: process.env.PW_SKIP_WEBSERVER
    ? undefined
    : {
        command: "npm run dev -- --host 127.0.0.1 --port 5173 --strictPort",
        url: "http://127.0.0.1:5173",
        reuseExistingServer: true,
        timeout: 120_000,
      },
});
