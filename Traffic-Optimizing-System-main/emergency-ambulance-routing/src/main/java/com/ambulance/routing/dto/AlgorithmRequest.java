package com.ambulance.routing.dto;

import jakarta.validation.constraints.NotBlank;

public class AlgorithmRequest {

    @NotBlank(message = "Source node ID is required")
    private String sourceId;

    @NotBlank(message = "Destination node ID is required")
    private String destinationId;

    public AlgorithmRequest() {}

    public AlgorithmRequest(String sourceId, String destinationId) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
    }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getDestinationId() { return destinationId; }
    public void setDestinationId(String destinationId) { this.destinationId = destinationId; }
}
