import { describe, expect, it } from "vitest";
import {
  validateDelayNode,
  validateEmailNode,
  validateHttpNode,
  validateMutationWorkflowId,
  validateNodeFields,
  validateTopologyForPublish,
  validateTriggerId,
  validateWorkflowName,
} from "./validation";

describe("validateWorkflowName", () => {
  it("rejects empty", () => {
    expect(validateWorkflowName("")).toBeTruthy();
    expect(validateWorkflowName("   ")).toBeTruthy();
  });
  it("accepts trimmed name", () => {
    expect(validateWorkflowName("orders-pipeline")).toBeNull();
  });
});

describe("validateHttpNode", () => {
  it("requires https url", () => {
    expect(validateHttpNode({ method: "GET", url: "" })).toBeTruthy();
    expect(validateHttpNode({ method: "GET", url: "https://a.com/x" })).toBeNull();
  });
  it("rejects invalid url", () => {
    expect(validateHttpNode({ url: "not a url" })).toBeTruthy();
  });
});

describe("validateDelayNode", () => {
  it("accepts positive ms", () => {
    expect(validateDelayNode({ delayMs: 500 })).toBeNull();
  });
  it("rejects negative", () => {
    expect(validateDelayNode({ delayMs: -1 })).toBeTruthy();
  });
  it("allows empty optional delay", () => {
    expect(validateDelayNode({ delayMs: "" })).toBeNull();
  });
});

describe("validateEmailNode", () => {
  it("requires valid email", () => {
    expect(validateEmailNode({ to: "a@b.co" })).toBeNull();
    expect(validateEmailNode({ to: "bad" })).toBeTruthy();
  });
});

describe("validateTriggerId", () => {
  it("requires positive integer string", () => {
    expect(validateTriggerId("")).toBeTruthy();
    expect(validateTriggerId("0")).toBeTruthy();
    expect(validateTriggerId("12")).toBeNull();
  });
});

describe("validateMutationWorkflowId", () => {
  it("requires id from either field", () => {
    expect(validateMutationWorkflowId("", "")).toBeTruthy();
    expect(validateMutationWorkflowId("5", "")).toBeNull();
    expect(validateMutationWorkflowId("", "7")).toBeNull();
  });
});

describe("validateNodeFields", () => {
  it("returns first node error", () => {
    const nodes = [
      { id: "start", type: "start", data: {} },
      { id: "http-1", type: "http", data: { method: "GET", url: "bad" } },
    ];
    expect(validateNodeFields(nodes)).toContain("HTTP node http-1");
  });
});

describe("validateTopologyForPublish", () => {
  const start = { id: "start", type: "start", data: {} };
  const http = { id: "h1", type: "http", data: {} };

  it("requires edge from start when steps exist", () => {
    expect(validateTopologyForPublish([start, http], [])).toBeTruthy();
  });
  it("passes when start connects to step", () => {
    expect(
      validateTopologyForPublish([start, http], [{ id: "e1", source: "start", target: "h1" }])
    ).toBeNull();
  });
  it("passes with only start", () => {
    expect(validateTopologyForPublish([start], [])).toBeNull();
  });
});
