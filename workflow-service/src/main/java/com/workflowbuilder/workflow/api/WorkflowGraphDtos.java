package com.workflowbuilder.workflow.api;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

public class WorkflowGraphDtos {

    public record CreateNodeRequest(
            @Size(max = 256) String nodeKey,
            @NotBlank @Size(max = 64) String nodeType,
            @NotNull Map<String, Double> position,
            @NotNull JsonNode data
    ) {
    }

    public record UpdateNodeRequest(
            @NotNull Map<String, Double> position,
            @NotNull JsonNode data
    ) {
    }

    public record NodeResponse(
            Long id,
            String nodeKey,
            String nodeType,
            double positionX,
            double positionY,
            JsonNode data
    ) {
    }

    /**
     * Full edge document as produced by the canvas (id, source, target, sourceHandle, targetHandle, animated, …).
     * If {@code edgeKey} is blank, {@code payload.id} is used when present; otherwise an id is generated.
     */
    public record CreateEdgeRequest(
            @Size(max = 256) String edgeKey,
            @NotNull JsonNode payload
    ) {
    }

    public record EdgeResponse(Long id, String edgeKey, JsonNode payload) {
    }

    public record GraphSummaryResponse(
            Long workflowId,
            JsonNode definitionJson
    ) {
    }
}
