/**
 * Client-side validation for workflow builder actions.
 */

export function validateWorkflowName(name) {
  const t = name == null ? "" : String(name).trim();
  if (!t) return "Workflow name is required.";
  if (t.length > 128) return "Workflow name must be at most 128 characters.";
  return null;
}

export function validateHttpNode(data) {
  if (!data) return null;
  const url = (data.url || "").trim();
  if (!url) return "HTTP URL is required.";
  try {
    const u = new URL(url);
    if (u.protocol !== "http:" && u.protocol !== "https:") {
      return "URL must use http or https.";
    }
  } catch {
    return "Enter a valid URL (e.g. https://api.example.com/path).";
  }
  const method = (data.method || "GET").trim().toUpperCase();
  const allowed = ["GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"];
  if (!allowed.includes(method)) return `Unsupported HTTP method: ${method}.`;
  return null;
}

export function validateDelayNode(data) {
  if (!data) return null;
  const raw = data.delayMs;
  if (raw === undefined || raw === null || raw === "") return null;
  const n = Number(raw);
  if (!Number.isFinite(n) || n < 0) return "Delay must be zero or a positive number (ms).";
  if (n > 86_400_000) return "Delay cannot exceed 24 hours (86400000 ms).";
  return null;
}

export function validateEmailNode(data) {
  if (!data) return null;
  const to = (data.to || "").trim();
  if (!to) return "Email recipient is required.";
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(to)) return "Enter a valid email address.";
  return null;
}

export function validateTriggerId(triggerId) {
  const raw = triggerId == null ? "" : String(triggerId).trim();
  if (!raw) return "Enter a workflow ID to run.";
  const n = Number(raw);
  if (!Number.isInteger(n) || n < 1) return "Workflow ID must be a positive integer.";
  return null;
}

/** Update / Publish need an existing workflow id (from Save or manual entry). */
export function validateMutationWorkflowId(selectedWorkflowId, triggerId) {
  const a = selectedWorkflowId == null ? "" : String(selectedWorkflowId).trim();
  const b = triggerId == null ? "" : String(triggerId).trim();
  const raw = a || b;
  if (!raw) {
    return "Save the workflow first (to create an id) or type the Workflow ID in the header.";
  }
  const n = Number(raw);
  if (!Number.isInteger(n) || n < 1) {
    return "Workflow ID in the header must be a positive integer.";
  }
  return null;
}

/** Validates per-node fields (URL, email, delay) for any persisted definition. */
export function validateNodeFields(nodes) {
  for (const n of nodes) {
    if (n.type === "http") {
      const err = validateHttpNode(n.data);
      if (err) return `HTTP node ${n.id}: ${err}`;
    }
    if (n.type === "delay") {
      const err = validateDelayNode(n.data);
      if (err) return `Delay node ${n.id}: ${err}`;
    }
    if (n.type === "email") {
      const err = validateEmailNode(n.data);
      if (err) return `Email node ${n.id}: ${err}`;
    }
  }
  return null;
}

/**
 * For publish: require Start and at least one edge from Start when multiple steps exist.
 */
export function validateTopologyForPublish(nodes, edges) {
  const hasStart = nodes.some((n) => n.id === "start" || n.type === "start");
  if (!hasStart) return "Graph must include a Start node.";

  const downstreamOfStart = new Set();
  for (const e of edges) {
    if (e.source === "start") downstreamOfStart.add(e.target);
  }
  const stepNodes = nodes.filter((n) => n.type && n.type !== "start");
  if (stepNodes.length > 0 && downstreamOfStart.size === 0) {
    return "Connect Start to your first step: drag from the dot under Start to another node.";
  }
  return null;
}
