package com.workflowbuilder.execution.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Orders workflow canvas nodes from Start following edges (BFS). Used by Temporal workflow code.
 */
public final class WorkflowGraphPlanner {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_STEPS = 256;

    private WorkflowGraphPlanner() {}

    public record ExecutableStep(String nodeId, String kind, JsonNode data) {}

    public static List<ExecutableStep> plan(String definitionJson) {
        if (definitionJson == null || definitionJson.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = MAPPER.readTree(definitionJson);
            if (!root.isObject()) {
                return List.of();
            }
            JsonNode nodesNode = root.get("nodes");
            JsonNode edgesNode = root.get("edges");
            if (nodesNode == null || !nodesNode.isArray()) {
                return List.of();
            }
            Map<String, JsonNode> byId = new HashMap<>();
            for (JsonNode n : nodesNode) {
                String id = text(n, "id");
                if (!id.isEmpty()) {
                    byId.put(id, n);
                }
            }
            List<List<String>> adj = new ArrayList<>();
            Map<String, Integer> index = new HashMap<>();
            for (String id : byId.keySet()) {
                index.put(id, adj.size());
                adj.add(new ArrayList<>());
            }
            if (edgesNode != null && edgesNode.isArray()) {
                for (JsonNode e : edgesNode) {
                    String src = text(e, "source");
                    String tgt = text(e, "target");
                    if (index.containsKey(src) && index.containsKey(tgt)) {
                        adj.get(index.get(src)).add(tgt);
                    }
                }
            }
            String startId = findStartId(byId);
            if (startId == null) {
                return List.of();
            }
            List<ExecutableStep> out = new ArrayList<>();
            Queue<String> q = new ArrayDeque<>();
            Set<String> seen = new HashSet<>();
            q.add(startId);
            seen.add(startId);
            int guard = 0;
            while (!q.isEmpty() && guard++ < MAX_STEPS) {
                String id = q.poll();
                JsonNode node = byId.get(id);
                if (node == null) {
                    continue;
                }
                String kind = text(node, "type");
                if (kind.isEmpty()) {
                    kind = "default";
                }
                if (!"start".equals(kind)) {
                    out.add(new ExecutableStep(id, kind, node.path("data")));
                }
                for (String next : adj.get(index.get(id))) {
                    if (seen.add(next)) {
                        q.add(next);
                    }
                }
            }
            return List.copyOf(out);
        } catch (Exception e) {
            return List.of();
        }
    }

    private static String findStartId(Map<String, JsonNode> byId) {
        for (Map.Entry<String, JsonNode> e : byId.entrySet()) {
            if ("start".equals(text(e.getValue(), "type")) || "start".equals(e.getKey())) {
                return e.getKey();
            }
        }
        return byId.containsKey("start") ? "start" : null;
    }

    private static String text(JsonNode n, String field) {
        if (n == null || !n.has(field) || n.get(field).isNull()) {
            return "";
        }
        return n.get(field).asText("").trim();
    }
}
