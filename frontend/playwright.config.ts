import { defineConfig, devices } from "@playwright/test";

/**
 * E2E config. Assumes the full stack is running (backend + DB + frontend) OR starts the
 * frontend dev server automatically. For the complete student journey the backend must be up
 * with seed data (docker compose up). The smoke test degrades gracefully if content is absent.
 */
export default defineConfig({
  testDir: "./tests-e2e",
  timeout: 30_000,
  retries: 0,
  reporter: [["list"]],
  use: {
    baseURL: process.env.E2E_BASE_URL || "http://localhost:3000",
    trace: "on-first-retry",
  },
  projects: [
    { name: "desktop-chromium", use: { ...devices["Desktop Chrome"] } },
    { name: "mobile-chrome", use: { ...devices["Pixel 7"] } },
  ],
  webServer: process.env.E2E_NO_SERVER
    ? undefined
    : {
        command: "npm run start",
        url: "http://localhost:3000",
        timeout: 60_000,
        reuseExistingServer: true,
      },
});
