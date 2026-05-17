/**
 * Fetch helper for /ui-api with readable errors (Spring JSON or plain text).
 */
export async function jsonFetch(url, options = {}) {
  const { headers: optHeaders, ...rest } = options;
  const response = await fetch(url, {
    ...rest,
    headers: { "Content-Type": "application/json", ...optHeaders },
  });

  const text = await response.text();

  if (!response.ok) {
    let message = text?.trim() || `HTTP ${response.status}`;
    if (text && text.trim().startsWith("{")) {
      try {
        const j = JSON.parse(text);
        message =
          j.message ||
          j.error ||
          j.title ||
          (Array.isArray(j.errors) ? j.errors.map((e) => e.defaultMessage || e).join("; ") : null) ||
          message;
      } catch {
        /* use raw */
      }
    }
    if (response.status === 409) {
      message = message.includes("Conflict") ? message : `${message} — try a different name or use Update.`;
    }
    if (response.status === 401 || response.status === 403) {
      message = "Not authorized. If you use the API gateway, obtain a JWT for /api/** or call frontend-service directly.";
    }
    throw new Error(message);
  }

  if (!text || !text.trim()) {
    return null;
  }
  try {
    return JSON.parse(text);
  } catch {
    throw new Error("Invalid JSON from server");
  }
}
