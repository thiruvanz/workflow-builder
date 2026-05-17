import { test, expect } from "@playwright/test";

const BFF = process.env.BFF_URL || "http://127.0.0.1:8088";

test.describe("Workflow Builder E2E (browser)", () => {
  test.describe.configure({ mode: "serial" });

  test.beforeAll(async ({ request }) => {
    const wf = await request.get(`${BFF}/ui-api/workflows`);
    const dash = await request.get(`${BFF}/ui-api/monitoring/dashboard`);
    if (!wf.ok() || !dash.ok()) {
      test.skip(
        true,
        `BFF not ready at ${BFF} (workflows=${wf.status()}, dashboard=${dash.status()}). ` +
          `Start: docker compose up (frontend-service 8088:8088) + deps, or gradlew :frontend-service:bootRun with workflow/monitoring/execution on expected ports.`
      );
    }
  });

  test("load, save, publish, execute", async ({ page }) => {
    await page.goto("/");
    await expect(page.locator(".brand")).toContainText("Workflow Builder");
    await expect(page.locator(".error-banner")).toBeHidden({ timeout: 30_000 });

    const name = `ui-e2e-${Date.now()}`;
    await page.getByLabel("Workflow name").fill(name);
    await page.getByRole("button", { name: "Save", exact: true }).click();
    await expect(page.locator(".success-banner")).toContainText("Save complete", { timeout: 30_000 });
    await expect(page.locator(".error-banner")).toBeHidden();

    await page.getByRole("button", { name: "Publish", exact: true }).click();
    await expect(page.locator(".success-banner")).toContainText("PUBLISHED", { timeout: 30_000 });
    await expect(page.locator(".error-banner")).toBeHidden();

    const runInput = page.getByLabel("Workflow ID to execute");
    await expect(runInput).not.toHaveValue("", { timeout: 15_000 });

    await page.getByRole("button", { name: "Execute Workflow" }).click();
    await expect(page.locator(".success-banner")).toContainText("Execution finished", { timeout: 45_000 });
  });
});
