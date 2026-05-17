import { useCallback, useEffect, useMemo, useState } from "react";
import ReactFlow, {
  Background,
  ConnectionMode,
  Controls,
  Handle,
  MiniMap,
  Position,
  addEdge,
  useEdgesState,
  useNodesState,
} from "reactflow";
import "reactflow/dist/style.css";
import { jsonFetch } from "./api";
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

function normalizeWorkflows(payload) {
  return Array.isArray(payload) ? payload : [];
}

function normalizeDashboard(payload) {
  if (payload != null && typeof payload === "object" && !Array.isArray(payload)) {
    return payload;
  }
  return {};
}

function StartNode({ data, isConnectable }) {
  return (
    <div className="custom-node start-node">
      <div className="node-badge">Entry</div>
      <div className="node-title">Start</div>
      <div className="node-line">{data?.hint || "Drag from the dot below to your first step"}</div>
      <Handle type="source" position={Position.Bottom} id="out" isConnectable={isConnectable} />
    </div>
  );
}

function HttpNode({ data, isConnectable }) {
  return (
    <div className="custom-node http">
      <Handle type="target" position={Position.Top} isConnectable={isConnectable} />
      <div className="node-title">HTTP Request</div>
      <div className="node-line">
        {data?.method || "GET"} {data?.url || "https://example.com/api"}
      </div>
      <Handle type="source" position={Position.Bottom} isConnectable={isConnectable} />
    </div>
  );
}

function DelayNode({ data, isConnectable }) {
  return (
    <div className="custom-node delay">
      <Handle type="target" position={Position.Top} isConnectable={isConnectable} />
      <div className="node-title">Delay</div>
      <div className="node-line">Wait {data?.delayMs ?? 1000} ms</div>
      <Handle type="source" position={Position.Bottom} isConnectable={isConnectable} />
    </div>
  );
}

function EmailNode({ data, isConnectable }) {
  return (
    <div className="custom-node email">
      <Handle type="target" position={Position.Top} isConnectable={isConnectable} />
      <div className="node-title">Send Email</div>
      <div className="node-line">{data?.to || "user@example.com"}</div>
      <Handle type="source" position={Position.Bottom} isConnectable={isConnectable} />
    </div>
  );
}

function BranchNode({ data, isConnectable }) {
  return (
    <div className="custom-node branch-node">
      <Handle type="target" position={Position.Top} isConnectable={isConnectable} />
      <div className="node-title">If / Branch</div>
      <div className="node-line">{data?.condition || "status == 'ok'"}</div>
      <Handle
        id="true"
        type="source"
        position={Position.Bottom}
        isConnectable={isConnectable}
        style={{ left: "30%", background: "#16a34a" }}
      />
      <Handle
        id="false"
        type="source"
        position={Position.Bottom}
        isConnectable={isConnectable}
        style={{ left: "70%", background: "#dc2626" }}
      />
      <div className="branch-labels" aria-hidden="true">
        <span>True</span>
        <span>False</span>
      </div>
    </div>
  );
}

function mapNodeTypeForExport(n) {
  if (n.type === "start") return "start";
  if (n.type) return n.type;
  if (n.id.startsWith("http-")) return "http";
  if (n.id.startsWith("delay-")) return "delay";
  if (n.id.startsWith("email-")) return "email";
  if (n.id.startsWith("branch-")) return "branch";
  return "start";
}

function extractCreatedId(created) {
  if (created == null || typeof created !== "object") return null;
  const id = created.id ?? created.Id ?? created.workflowId;
  if (id == null) return null;
  return id;
}

export default function App() {
  const [workflowName, setWorkflowName] = useState("my-workflow");
  const [selectedWorkflowId, setSelectedWorkflowId] = useState("");
  const [selectedNodeId, setSelectedNodeId] = useState("");
  const [triggerId, setTriggerId] = useState("");
  const [workflows, setWorkflows] = useState([]);
  const [dashboard, setDashboard] = useState({});
  const [error, setError] = useState("");
  const [softWarning, setSoftWarning] = useState("");
  const [success, setSuccess] = useState("");
  /** Kept after the top banner clears so users still see confirmation (newest first). */
  const [successLog, setSuccessLog] = useState([]);
  const [savedDefinition, setSavedDefinition] = useState("");
  const [fieldHint, setFieldHint] = useState("");
  const [pending, setPending] = useState(null);

  const initialNodes = useMemo(
    () => [
      {
        id: "start",
        type: "start",
        position: { x: 250, y: 40 },
        data: { label: "Start", hint: "Connect to your first node" },
      },
    ],
    []
  );
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const nodeTypes = useMemo(
    () => ({
      start: StartNode,
      http: HttpNode,
      delay: DelayNode,
      email: EmailNode,
      branch: BranchNode,
    }),
    []
  );

  useEffect(() => {
    if (!success) return undefined;
    const t = setTimeout(() => setSuccess(""), 10000);
    return () => clearTimeout(t);
  }, [success]);

  const pushSuccessEntry = (text) => {
    setSoftWarning("");
    setSuccess(text);
    setSuccessLog((prev) =>
      [{ id: `${Date.now()}-${Math.random()}`, text, time: new Date() }, ...prev].slice(0, 10)
    );
  };

  const onConnect = useCallback(
    (params) =>
      setEdges((current) =>
        addEdge(
          {
            ...params,
            animated: true,
            label:
              params.sourceHandle === "true"
                ? "true"
                : params.sourceHandle === "false"
                  ? "false"
                  : "",
          },
          current
        )
      ),
    [setEdges]
  );

  const addNode = (type) => {
    const suffix =
      typeof crypto !== "undefined" && typeof crypto.randomUUID === "function"
        ? crypto.randomUUID()
        : `${Math.random().toString(36).slice(2, 11)}`;
    const id = `${type}-${Date.now()}-${suffix}`;
    const defaults = {
      http: { label: "HTTP Node", method: "GET", url: "https://example.com/api" },
      delay: { label: "Delay Node", delayMs: 1000 },
      email: {
        label: "Email Node",
        to: "user@example.com",
        subject: "Workflow update",
        body: "Hello from workflow",
      },
      branch: { label: "Branch Node", condition: "response.status === 200" },
    };
    setNodes((current) => [
      ...current,
      {
        id,
        type,
        position: { x: 120 + current.length * 30, y: 120 + current.length * 40 },
        data: defaults[type],
      },
    ]);
    setSelectedNodeId(id);
    setFieldHint("");
    setSuccess("");
  };

  const selectedNode = nodes.find((n) => n.id === selectedNodeId);

  const updateSelectedNodeData = (key, value) => {
    setNodes((current) =>
      current.map((node) =>
        node.id === selectedNodeId ? { ...node, data: { ...node.data, [key]: value } } : node
      )
    );
  };

  const toDefinitionJson = () =>
    JSON.stringify(
      {
        version: 1,
        nodes: nodes.map((n) => ({
          id: n.id,
          type: mapNodeTypeForExport(n),
          data: n.data,
          position: n.position,
        })),
        edges,
      },
      null,
      2
    );

  const refreshWorkflows = async () => {
    const data = await jsonFetch("/ui-api/workflows");
    setWorkflows(normalizeWorkflows(data));
  };

  const refreshDashboard = async () => {
    const data = await jsonFetch("/ui-api/monitoring/dashboard");
    setDashboard(normalizeDashboard(data));
  };

  const validateWorkflowNameOrSetError = () => {
    const nameErr = validateWorkflowName(workflowName);
    if (nameErr) {
      setError(nameErr);
      setSuccess("");
      return false;
    }
    return true;
  };

  /** Save / Update: name only so drafts can persist with incomplete node fields. */
  const validateBeforeSave = () => validateWorkflowNameOrSetError();

  const validateBeforePublish = () => {
    if (!validateWorkflowNameOrSetError()) return false;
    const fieldsErr = validateNodeFields(nodes);
    if (fieldsErr) {
      setError(fieldsErr);
      setSuccess("");
      return false;
    }
    const topoErr = validateTopologyForPublish(nodes, edges);
    if (topoErr) {
      setError(topoErr);
      setSuccess("");
      return false;
    }
    return true;
  };

  const runAsync = async (actionKey, fn) => {
    try {
      setError("");
      setSoftWarning("");
      setSuccess("");
      setPending(actionKey);
      await fn();
    } catch (e) {
      setSuccess("");
      setSoftWarning("");
      setError(e?.message || "Unexpected error");
    } finally {
      setPending(null);
    }
  };

  useEffect(() => {
    const load = async () => {
      const backendHint =
        "With `npm run dev`, start the Java `frontend-service` first (port 8088 by default) so Vite can proxy /ui-api. From repo root: gradlew :frontend-service:bootRun — or set VITE_UI_API_PROXY in react-app/.env";
      try {
        const results = await Promise.allSettled([refreshWorkflows(), refreshDashboard()]);
        const wfResult = results[0];
        const dashResult = results[1];
        if (wfResult.status === "rejected" && dashResult.status === "rejected") {
          const messages = [wfResult.reason, dashResult.reason]
            .map((r) => r?.message || String(r))
            .join(" · ");
          setError(`${messages}. ${backendHint}`);
          setSoftWarning("");
        } else if (wfResult.status === "rejected") {
          setSoftWarning("");
          setError(`${wfResult.reason?.message || String(wfResult.reason)}. ${backendHint}`);
        } else if (dashResult.status === "rejected") {
          setError("");
          setSoftWarning(
            `Monitoring dashboard unavailable (${dashResult.reason?.message || String(dashResult.reason)}). Workflows still load — start Mongo + monitoring-service, or use “Refresh Monitoring” later.`
          );
        } else {
          setError("");
          setSoftWarning("");
        }
      } catch (e) {
        setError(`${e?.message || "Failed to load data"}. ${backendHint}`);
      }
    };
    load();
  }, []);

  const handleSave = () => {
    if (!validateBeforeSave()) return;
    runAsync("save", async () => {
      const definitionJson = toDefinitionJson();
      setSavedDefinition(definitionJson);
      const created = await jsonFetch("/ui-api/workflows", {
        method: "POST",
        body: JSON.stringify({ name: workflowName.trim(), definitionJson }),
      });
      const newId = extractCreatedId(created);
      if (newId == null) {
        throw new Error("Create workflow returned no id. Is workflow-service running?");
      }
      const idStr = String(newId);
      setSelectedWorkflowId(idStr);
      setTriggerId(idStr);
      pushSuccessEntry(
        `Save complete — workflow "${workflowName.trim()}" stored as #${idStr}. Workflow ID is filled in below; use Update to edit this graph, Publish when ready, then Execute to run.`
      );
      await refreshWorkflows();
    });
  };

  const handleUpdate = () => {
    const idErr = validateMutationWorkflowId(selectedWorkflowId, triggerId);
    if (idErr) {
      setError(idErr);
      setSuccess("");
      return;
    }
    if (!validateBeforeSave()) return;
    runAsync("update", async () => {
      const id = String(selectedWorkflowId || triggerId).trim();
      const definitionJson = toDefinitionJson();
      setSavedDefinition(definitionJson);
      await jsonFetch(`/ui-api/workflows/${id}`, {
        method: "PUT",
        body: JSON.stringify({ definitionJson }),
      });
      pushSuccessEntry(`Update complete — workflow #${id} saved with your latest canvas changes.`);
      await refreshWorkflows();
    });
  };

  const handlePublish = () => {
    const idErr = validateMutationWorkflowId(selectedWorkflowId, triggerId);
    if (idErr) {
      setError(idErr);
      setSuccess("");
      return;
    }
    if (!validateBeforePublish()) return;
    runAsync("publish", async () => {
      const id = String(selectedWorkflowId || triggerId).trim();
      await jsonFetch(`/ui-api/workflows/${id}/publish`, { method: "POST" });
      pushSuccessEntry(
        `Publish complete — workflow #${id} is now PUBLISHED. You can Execute it (execution-service must reach workflow-service).`
      );
      await refreshWorkflows();
    });
  };

  const handleExecute = () => {
    const runErr = validateTriggerId(triggerId);
    if (runErr) {
      setError(runErr);
      setSuccess("");
      return;
    }
    runAsync("execute", async () => {
      const workflowId = Number(triggerId);
      const result = await jsonFetch("/ui-api/executions/trigger", {
        method: "POST",
        body: JSON.stringify({
          workflowId,
          tenantId: "default-tenant",
          idempotencyKey: `manual-${workflowId}-${Date.now()}`,
        }),
      });
      const runId = result?.id ?? result?.Id;
      const status = result?.status ?? result?.Status;
      pushSuccessEntry(
        runId != null
          ? `Execution finished — workflow #${workflowId}, run #${runId}, status ${status ?? "unknown"}. Use “Refresh Monitoring” to update the dashboard.`
          : `Trigger accepted for workflow #${workflowId}. Use “Refresh Monitoring” to update the dashboard.`
      );
    });
  };

  const handleRefreshWorkflows = () => {
    runAsync("wf", async () => {
      await refreshWorkflows();
      pushSuccessEntry("Workflow list refreshed.");
    });
  };

  const handleRefreshDashboard = () => {
    runAsync("dash", async () => {
      await refreshDashboard();
      pushSuccessEntry("Monitoring dashboard refreshed.");
    });
  };

  const runSelectedNodeValidation = useCallback(() => {
    if (!selectedNode) {
      setFieldHint("");
      return;
    }
    let err = null;
    if (selectedNode.type === "http") err = validateHttpNode(selectedNode.data);
    else if (selectedNode.type === "delay") err = validateDelayNode(selectedNode.data);
    else if (selectedNode.type === "email") err = validateEmailNode(selectedNode.data);
    setFieldHint(err || "");
  }, [selectedNode]);

  useEffect(() => {
    runSelectedNodeValidation();
  }, [runSelectedNodeValidation, nodes, selectedNodeId]);

  const busy = pending != null;

  return (
    <div className="n8n-app">
      <header className="topbar">
        <div className="brand">Workflow Builder</div>
        <input
          className="wf-name"
          value={workflowName}
          onChange={(e) => setWorkflowName(e.target.value)}
          placeholder="Workflow name"
          aria-label="Workflow name"
          disabled={busy}
        />
        <button type="button" disabled={busy} onClick={handleSave}>
          {pending === "save" ? "Saving…" : "Save"}
        </button>
        <button type="button" disabled={busy} onClick={handleUpdate}>
          {pending === "update" ? "Updating…" : "Update"}
        </button>
        <button type="button" disabled={busy} onClick={handlePublish}>
          {pending === "publish" ? "Publishing…" : "Publish"}
        </button>
        <input
          className="wf-id"
          value={selectedWorkflowId}
          onChange={(e) => setSelectedWorkflowId(e.target.value)}
          placeholder="Workflow ID (set after Save)"
          aria-label="Workflow ID"
          disabled={busy}
        />
      </header>

      <div className="status-strip" role="status">
        {busy ? <div className="pending-banner">Working: {pending}…</div> : null}
        {success ? (
          <div className="success-banner" role="status" aria-live="polite">
            <div className="success-banner-title">Success</div>
            <div className="success-banner-body">{success}</div>
          </div>
        ) : null}
        {error ? <div className="error-banner">{error}</div> : null}
        {softWarning ? <div className="soft-warning-banner">{softWarning}</div> : null}
      </div>

      <div className="workspace">
        <aside className="left-panel">
          <div className="backend-card">
            <h4 className="backend-card-title">API / Save / lists</h4>
            <p>
              Vite sends <code>/ui-api</code> to <strong>frontend-service</strong> (default{" "}
              <code>127.0.0.1:8088</code>). If you see proxy errors, start it before using Save:
            </p>
            <pre className="cmd-line">
              gradlew :frontend-service:bootRun
            </pre>
            <p className="backend-card-note">
              Optional: copy <code>react-app/.env.example</code> to <code>.env.local</code> and set{" "}
              <code>VITE_UI_API_PROXY</code> if your port differs.
            </p>
          </div>
          <h3>Workflow start (on canvas)</h3>
          <p className="panel-hint">
            There is <strong>no &quot;Start&quot; toolbar button</strong>. The green <strong>Start</strong> card on the
            canvas is the workflow entry — drag from its <b>bottom</b> handle to your first step. Use the
            controls on the canvas (bottom-right) to zoom or fit view if you don&apos;t see it.
          </p>
          <h3>Add nodes</h3>
          <p className="panel-hint small">
            Click a node to edit on the right. Connect: bottom handle → next node&apos;s top handle.
          </p>
          <button type="button" disabled={busy} onClick={() => addNode("http")}>
            HTTP Request
          </button>
          <button type="button" disabled={busy} onClick={() => addNode("delay")}>
            Delay
          </button>
          <button type="button" disabled={busy} onClick={() => addNode("email")}>
            Send Email
          </button>
          <button type="button" disabled={busy} onClick={() => addNode("branch")}>
            If / Branch
          </button>
          <h3>Run</h3>
          <input
            value={triggerId}
            onChange={(e) => setTriggerId(e.target.value)}
            placeholder="Workflow ID to run"
            aria-label="Workflow ID to execute"
            disabled={busy}
          />
          <button type="button" disabled={busy} onClick={handleExecute}>
            {pending === "execute" ? "Executing…" : "Execute Workflow"}
          </button>
          <button type="button" disabled={busy} onClick={handleRefreshWorkflows}>
            {pending === "wf" ? "Loading…" : "Refresh Workflows"}
          </button>
          <button type="button" disabled={busy} onClick={handleRefreshDashboard}>
            {pending === "dash" ? "Loading…" : "Refresh Monitoring"}
          </button>
          <h3>Recent successes</h3>
          <p className="panel-hint small">Confirmation messages stay here after the green banner fades (newest first).</p>
          {successLog.length === 0 ? (
            <p className="muted">No successful saves or actions yet.</p>
          ) : (
            <ul className="success-log" aria-label="Recent success messages">
              {successLog.map((entry) => (
                <li key={entry.id}>
                  <span className="success-log-time">{entry.time.toLocaleTimeString()}</span>
                  <span className="success-log-text">{entry.text}</span>
                </li>
              ))}
            </ul>
          )}
        </aside>

        <main className="canvas-panel">
          <ReactFlow
            className="flow-canvas"
            style={{ width: "100%", height: "100%" }}
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
            onNodeClick={(_, node) => {
              setSelectedNodeId(node.id);
              setError("");
            }}
            onPaneClick={() => {
              setSelectedNodeId("");
              setFieldHint("");
            }}
            nodeTypes={nodeTypes}
            connectionMode={ConnectionMode.Strict}
            nodesConnectable
            nodesDraggable
            elementsSelectable
            connectionRadius={40}
            isValidConnection={() => true}
            connectOnClick={false}
            connectionLineStyle={{ stroke: "#94a3b8", strokeWidth: 2 }}
            fitView
            fitViewOptions={{ padding: 0.2 }}
            defaultEdgeOptions={{ animated: true, style: { strokeWidth: 2 } }}
          >
            <MiniMap pannable zoomable />
            <Controls />
            <Background />
          </ReactFlow>
        </main>

        <aside className="right-panel">
          <h3>Node Properties</h3>
          {selectedNode ? (
            <div className="props-form">
              <p>
                <b>Type:</b> {selectedNode.type}
              </p>
              <p>
                <b>ID:</b> {selectedNode.id}
              </p>
              {selectedNode.type === "start" ? (
                <p className="field-hint ok">
                  Workflow entry point. Drag from the green dot under Start to your first step.
                </p>
              ) : null}
              {selectedNode.type === "http" ? (
                <>
                  <label className="field-label" htmlFor="http-method">
                    Method
                  </label>
                  <input
                    id="http-method"
                    value={selectedNode.data?.method || ""}
                    onChange={(e) => updateSelectedNodeData("method", e.target.value)}
                    placeholder="GET"
                    autoComplete="off"
                    disabled={busy}
                  />
                  <label className="field-label" htmlFor="http-url">
                    URL
                  </label>
                  <input
                    id="http-url"
                    value={selectedNode.data?.url || ""}
                    onChange={(e) => updateSelectedNodeData("url", e.target.value)}
                    placeholder="https://api.example.com/..."
                    autoComplete="off"
                    disabled={busy}
                  />
                </>
              ) : null}
              {selectedNode.type === "delay" ? (
                <>
                  <label className="field-label" htmlFor="delay-ms">
                    Delay (ms)
                  </label>
                  <input
                    id="delay-ms"
                    value={selectedNode.data?.delayMs ?? ""}
                    onChange={(e) => {
                      const v = e.target.value;
                      if (v === "") {
                        updateSelectedNodeData("delayMs", 1000);
                        return;
                      }
                      const n = Number(v);
                      updateSelectedNodeData("delayMs", Number.isFinite(n) ? n : 1000);
                    }}
                    placeholder="1000"
                    type="number"
                    min={0}
                    disabled={busy}
                  />
                </>
              ) : null}
              {selectedNode.type === "email" ? (
                <>
                  <label className="field-label" htmlFor="email-to">
                    To
                  </label>
                  <input
                    id="email-to"
                    value={selectedNode.data?.to || ""}
                    onChange={(e) => updateSelectedNodeData("to", e.target.value)}
                    placeholder="user@example.com"
                    disabled={busy}
                  />
                  <label className="field-label" htmlFor="email-subject">
                    Subject
                  </label>
                  <input
                    id="email-subject"
                    value={selectedNode.data?.subject || ""}
                    onChange={(e) => updateSelectedNodeData("subject", e.target.value)}
                    placeholder="Subject"
                    disabled={busy}
                  />
                  <label className="field-label" htmlFor="email-body">
                    Body
                  </label>
                  <textarea
                    id="email-body"
                    rows={4}
                    value={selectedNode.data?.body || ""}
                    onChange={(e) => updateSelectedNodeData("body", e.target.value)}
                    placeholder="Body"
                    disabled={busy}
                  />
                </>
              ) : null}
              {selectedNode.type === "branch" ? (
                <>
                  <label className="field-label" htmlFor="branch-condition">
                    Condition
                  </label>
                  <input
                    id="branch-condition"
                    value={selectedNode.data?.condition || ""}
                    onChange={(e) => updateSelectedNodeData("condition", e.target.value)}
                    placeholder="response.status === 200"
                    disabled={busy}
                  />
                </>
              ) : null}
              {fieldHint ? <div className="field-hint warn">{fieldHint}</div> : null}
            </div>
          ) : (
            <p className="muted">Click a node on the canvas to edit its settings.</p>
          )}

          <h3>Backend checks</h3>
          <ul className="checklist">
            <li>
              <code>frontend-service</code> on :8088 (or Vite proxy). Workflow / execution / monitoring URLs must be reachable from it.
            </li>
            <li>
              <b>Save</b> → POST workflow. <b>Update</b> → PUT (needs id). <b>Publish</b> → publish + graph rules. <b>Execute</b> → run published workflow.
            </li>
          </ul>

          <h3>Dashboard</h3>
          <div className="kpi">
            <span className="chip">Workflows: {dashboard?.totalWorkflows ?? 0}</span>
            <span className="chip">Published: {dashboard?.publishedWorkflows ?? 0}</span>
            <span className="chip">Runs: {dashboard?.totalRuns ?? 0}</span>
          </div>

          <h3>Workflows</h3>
          <pre>
            {JSON.stringify(
              (Array.isArray(workflows) ? workflows : []).map((w) => ({
                id: w.id,
                name: w.name,
                status: w.status,
              })),
              null,
              2
            )}
          </pre>

          <h3>Definition JSON</h3>
          <pre>{savedDefinition || toDefinitionJson()}</pre>
        </aside>
      </div>
    </div>
  );
}
