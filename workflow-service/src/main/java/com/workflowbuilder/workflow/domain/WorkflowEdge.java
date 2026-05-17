package com.workflowbuilder.workflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "workflow_edges",
        uniqueConstraints = @UniqueConstraint(columnNames = {"workflow_id", "edge_key"})
)
public class WorkflowEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowDefinition workflow;

    @Column(name = "edge_key", nullable = false, length = 256)
    private String edgeKey;

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    public Long getId() {
        return id;
    }

    public WorkflowDefinition getWorkflow() {
        return workflow;
    }

    public void setWorkflow(WorkflowDefinition workflow) {
        this.workflow = workflow;
    }

    public String getEdgeKey() {
        return edgeKey;
    }

    public void setEdgeKey(String edgeKey) {
        this.edgeKey = edgeKey;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }
}
