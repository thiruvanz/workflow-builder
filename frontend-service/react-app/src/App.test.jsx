import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import App from "./App";

describe("App", () => {
  it("renders workflow builder shell", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn((url) =>
        Promise.resolve({
          ok: true,
          text: () =>
            Promise.resolve(String(url).includes("dashboard") ? "{}" : "[]"),
        })
      )
    );

    render(<App />);

    expect(screen.getByText("Workflow Builder")).toBeTruthy();
    expect(screen.getByRole("button", { name: /save/i })).toBeTruthy();
    expect(screen.getByLabelText(/workflow name/i)).toBeTruthy();
  });
});
