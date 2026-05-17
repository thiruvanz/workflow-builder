package com.workflowbuilder.workflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.CreateEdgeRequest;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.CreateNodeRequest;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.EdgeResponse;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.GraphSummaryResponse;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.NodeResponse;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.UpdateNodeRequest;
import com.workflowbuilder.workflow.domain.WorkflowDefinition;
import com.workflowbuilder.workflow.domain.WorkflowDefinitionRepository;
import com.workflowbuilder.workflow.domain.WorkflowEdge;
import com.workflowbuilder.workflow.domain.WorkflowEdgeRepository;
import com.workflowbuilder.workflow.domain.WorkflowNode;
import com.workflowbuilder.workflow.domain.WorkflowNodeRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WorkflowGraphProcessor {

    private final WorkflowDefinitionRepository workflowRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowEdgeRepository edgeRepository;
    private final ObjectMapper objectMapper;

    public WorkflowGraphProcessor(
            WorkflowDefinitionRepository workflowRepository,
            WorkflowNodeRepository nodeRepository,
            WorkflowEdgeRepository edgeRepository,
            ObjectMapper objectMapper
    ) {
        this.workflowRepository = workflowRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void syncFromDefinitionJson(Long workflowId, String definitionJson) {
        WorkflowDefinition workflow = getWorkflow(workflowId);
        edgeRepository.deleteByWorkflow_Id(workflowId);
        nodeRepository.deleteByWorkflow_Id(workflowId);

        JsonNode root;
        try {
            root = objectMapper.readTree(definitionJson == null || definitionJson.isBlank() ? "{}" : definitionJson);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid workflow definition JSON");
        }

        if (!root.isObject()) {
            return;
        }

        JsonNode nodes = root.path("nodes");
        if (nodes.isArray()) {
            Set<String> usedNodeKeys = new HashSet<>();
            for (JsonNode n : nodes) {
                WorkflowNode entity = new WorkflowNode();
                entity.setWorkflow(workflow);
                String nodeKey = textOrEmpty(n, "id");
                if (nodeKey.isBlank()) {
                    nodeKey = "node-" + UUID.randomUUID();
                }
                while (!usedNodeKeys.add(nodeKey)) {
                    nodeKey = nodeKey + "-" + UUID.randomUUID().toString().substring(0, 8);
                }
                entity.setNodeKey(nodeKey);
                String nodeType = textOrEmpty(n, "type");
                if (nodeType.isBlank()) {
                    nodeType = "default";
                }
                entity.setNodeType(nodeType);
                JsonNode pos = n.path("position");
                entity.setPositionX(pos.path("x").asDouble(0));
                entity.setPositionY(pos.path("y").asDouble(0));
                JsonNode data = n.path("data");
                entity.setDataJson(data.isMissingNode() || data.isNull()
                        ? "{}"
                        : data.toString());
                nodeRepository.save(entity);
            }
        }

        JsonNode edges = root.path("edges");
        if (edges.isArray()) {
            Set<String> usedEdgeKeys = new HashSet<>();
            for (JsonNode e : edges) {
                WorkflowEdge entity = new WorkflowEdge();
                entity.setWorkflow(workflow);
                String edgeKey = textOrEmpty(e, "id");
                if (edgeKey.isBlank()) {
                    edgeKey = "edge-" + UUID.randomUUID();
                }
                while (!usedEdgeKeys.add(edgeKey)) {
                    edgeKey = edgeKey + "-" + UUID.randomUUID().toString().substring(0, 8);
                }
                entity.setEdgeKey(edgeKey);
                entity.setPayloadJson(e.toString());
                edgeRepository.save(entity);
            }
        }
    }

    @Transactional(readOnly = true)
    public GraphSummaryResponse getGraph(Long workflowId) {
        getWorkflow(workflowId);
        try {
            String json = buildDefinitionJson(workflowId);
            return new GraphSummaryResponse(workflowId, objectMapper.readTree(json));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to build graph JSON");
        }
    }

    @Transactional(readOnly = true)
    public List<NodeResponse> listNodes(Long workflowId) {
        getWorkflow(workflowId);
        return nodeRepository.findByWorkflow_IdOrderByNodeKey(workflowId).stream()
                .map(this::toNodeResponse)
                .toList();
    }

    @Transactional
    public NodeResponse createNode(Long workflowId, CreateNodeRequest request) {
        WorkflowDefinition workflow = getWorkflow(workflowId);
        String nodeKey = request.nodeKey();
        if (nodeKey == null || nodeKey.isBlank()) {
            nodeKey = "node-" + UUID.randomUUID();
        }
        if (nodeRepository.existsByWorkflow_IdAndNodeKey(workflowId, nodeKey)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Node key already exists");
        }

        WorkflowNode entity = new WorkflowNode();
        entity.setWorkflow(workflow);
        entity.setNodeKey(nodeKey);
        entity.setNodeType(request.nodeType());
        Map<String, Double> pos = request.position();
        entity.setPositionX(pos.getOrDefault("x", 0.0));
        entity.setPositionY(pos.getOrDefault("y", 0.0));
        entity.setDataJson(request.data().toString());
        nodeRepository.save(entity);

        refreshWorkflowDefinitionJson(workflowId);
        return toNodeResponse(entity);
    }

    @Transactional
    public NodeResponse updateNode(Long workflowId, String nodeKey, UpdateNodeRequest request) {
        WorkflowNode node = findNode(workflowId, nodeKey);
        Map<String, Double> pos = request.position();
        node.setPositionX(pos.getOrDefault("x", node.getPositionX()));
        node.setPositionY(pos.getOrDefault("y", node.getPositionY()));
        node.setDataJson(request.data().toString());
        nodeRepository.save(node);
        refreshWorkflowDefinitionJson(workflowId);
        return toNodeResponse(node);
    }

    @Transactional
    public void deleteNode(Long workflowId, String nodeKey) {
        WorkflowNode node = findNode(workflowId, nodeKey);
        nodeRepository.delete(node);
        refreshWorkflowDefinitionJson(workflowId);
    }

    @Transactional(readOnly = true)
    public List<EdgeResponse> listEdges(Long workflowId) {
        getWorkflow(workflowId);
        return edgeRepository.findByWorkflow_IdOrderByEdgeKey(workflowId).stream()
                .map(this::toEdgeResponse)
                .toList();
    }

    @Transactional
    public EdgeResponse createEdge(Long workflowId, CreateEdgeRequest request) {
        WorkflowDefinition workflow = getWorkflow(workflowId);
        if (!request.payload().isObject()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Edge payload must be a JSON object");
        }
        ObjectNode edgeObj = (ObjectNode) request.payload().deepCopy();
        String edgeKey = request.edgeKey();
        if (edgeKey != null && !edgeKey.isBlank()) {
            edgeObj.put("id", edgeKey);
        } else {
            JsonNode idNode = edgeObj.get("id");
            if (idNode != null && !idNode.isNull() && !idNode.asText().isBlank()) {
                edgeKey = idNode.asText();
            } else {
                edgeKey = "edge-" + UUID.randomUUID();
                edgeObj.put("id", edgeKey);
            }
        }
        if (edgeRepository.existsByWorkflow_IdAndEdgeKey(workflowId, edgeKey)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Edge key already exists");
        }

        WorkflowEdge entity = new WorkflowEdge();
        entity.setWorkflow(workflow);
        entity.setEdgeKey(edgeKey);
        entity.setPayloadJson(edgeObj.toString());
        edgeRepository.save(entity);

        refreshWorkflowDefinitionJson(workflowId);
        return toEdgeResponse(entity);
    }

    @Transactional
    public void deleteEdge(Long workflowId, String edgeKey) {
        WorkflowEdge edge = edgeRepository.findByWorkflow_IdAndEdgeKey(workflowId, edgeKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Edge not found"));
        edgeRepository.delete(edge);
        refreshWorkflowDefinitionJson(workflowId);
    }

    private WorkflowDefinition getWorkflow(Long workflowId) {
        return workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found"));
    }

    private WorkflowNode findNode(Long workflowId, String nodeKey) {
        return nodeRepository.findByWorkflow_IdAndNodeKey(workflowId, nodeKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Node not found"));
    }

    private NodeResponse toNodeResponse(WorkflowNode n) {
        try {
            JsonNode data = objectMapper.readTree(n.getDataJson());
            return new NodeResponse(
                    n.getId(),
                    n.getNodeKey(),
                    n.getNodeType(),
                    n.getPositionX(),
                    n.getPositionY(),
                    data
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid node data JSON");
        }
    }

    private EdgeResponse toEdgeResponse(WorkflowEdge edge) {
        try {
            JsonNode payload = objectMapper.readTree(edge.getPayloadJson());
            return new EdgeResponse(edge.getId(), edge.getEdgeKey(), payload);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid edge payload JSON");
        }
    }

    private void refreshWorkflowDefinitionJson(Long workflowId) {
        WorkflowDefinition workflow = getWorkflow(workflowId);
        try {
            workflow.setDefinitionJson(buildDefinitionJson(workflowId));
            workflow.setUpdatedAt(Instant.now());
            workflowRepository.save(workflow);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to refresh definition JSON");
        }
    }

    public String buildDefinitionJson(Long workflowId) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("version", 1);
        ArrayNode nodesArray = root.putArray("nodes");
        for (WorkflowNode n : nodeRepository.findByWorkflow_IdOrderByNodeKey(workflowId)) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", n.getNodeKey());
            node.put("type", n.getNodeType());
            ObjectNode position = objectMapper.createObjectNode();
            position.put("x", n.getPositionX());
            position.put("y", n.getPositionY());
            node.set("position", position);
            node.set("data", objectMapper.readTree(n.getDataJson()));
            nodesArray.add(node);
        }
        ArrayNode edgesArray = root.putArray("edges");
        for (WorkflowEdge e : edgeRepository.findByWorkflow_IdOrderByEdgeKey(workflowId)) {
            edgesArray.add(objectMapper.readTree(e.getPayloadJson()));
        }
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    private static String textOrEmpty(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isMissingNode() || v.isNull() ? "" : v.asText("");
    }
}
