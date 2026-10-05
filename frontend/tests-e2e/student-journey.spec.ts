import { test, expect } from "@playwright/test";

/**
 * Full student journey (spec section 39). Requires the backend running with seed data
 * (docker compose up). If the Class 9 subjects are not present (backend down / empty), the test
 * is skipped rather than failing - it is an integration check, not a shell check.
 *
 * Journey: Home -> Class IX -> subject -> book -> chapter -> read/summary -> questions -> search.
 */
test("discover and read a chapter end to end", async ({ page }) => {
  await page.goto("/class/9");
  await expect(page.getByRole("heading", { name: /Class IX|Class 9/i })).toBeVisible();

  const subjectLinks = page.locator('a[href^="/class/9/"]');
  const count = await subjectLinks.count();
  test.skip(count === 0, "No seeded subjects; start the backend with seed data to run this journey.");

  // Open Mathematics if present, else the first subject.
  const maths = page.locator('a[href="/class/9/mathematics"]').first();
  if (await maths.count()) {
    await maths.click();
  } else {
    await subjectLinks.first().click();
  }

  // Open the first book if one exists.
  const bookLink = page.locator('a[href^="/book/"]').first();
  if (await bookLink.count()) {
    await bookLink.click();
    await expect(page.getByRole("heading", { level: 1 })).toBeVisible();

    // Open the first chapter if one exists.
    const chapterLink = page.locator('a[href*="/chapter/"]').first();
    if (await chapterLink.count()) {
      await chapterLink.click();
      // Chapter tabs should be present.
      await expect(page.getByRole("tab", { name: "Summary" })).toBeVisible();
      await page.getByRole("tab", { name: "Questions" }).click();
      // Either questions render or an honest empty state shows - both are acceptable.
      await expect(page.getByRole("tabpanel")).toBeVisible();
    }
  }

  // Search still works.
  await page.goto("/search?q=number");
  await expect(page.getByRole("heading", { name: "Search" })).toBeVisible();
});
