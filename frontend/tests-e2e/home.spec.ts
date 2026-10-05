import { test, expect } from "@playwright/test";

/**
 * Smoke tests that do not require backend content. They verify the shell renders, is
 * accessible-ish, and that navigation works. Safe to run even if the API is down.
 */
test.describe("home shell", () => {
  test("renders hero and primary actions", async ({ page }) => {
    await page.goto("/");
    await expect(page.getByRole("heading", { name: /Bihar Class 9 Learning Hub/i })).toBeVisible();
    await expect(page.getByRole("link", { name: /Explore Class 9/i })).toBeVisible();
    await expect(page.getByRole("link", { name: /View Syllabus/i })).toBeVisible();
  });

  test("has a skip link for keyboard users", async ({ page }) => {
    await page.goto("/");
    await page.keyboard.press("Tab");
    await expect(page.getByRole("link", { name: /skip to content/i })).toBeFocused();
  });

  test("search navigates to results", async ({ page }) => {
    await page.goto("/");
    await page.goto("/search?q=number");
    await expect(page.getByRole("heading", { name: "Search" })).toBeVisible();
  });

  test("404 page works", async ({ page }) => {
    await page.goto("/this-route-does-not-exist");
    await expect(page.getByRole("heading", { name: /Page not found/i })).toBeVisible();
  });
});
