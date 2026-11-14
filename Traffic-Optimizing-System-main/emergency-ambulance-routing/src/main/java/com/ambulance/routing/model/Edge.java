package com.ambulance.routing.model;

import java.util.Objects;
import java.util.UUID;

public class Edge {

    public enum TrafficLevel {
        NORMAL(1.0),
        MODERATE(1.5),
        HEAVY(2.5),
        BLOCKED(Double.POSITIVE_INFINITY);

        private final double multiplier;

        TrafficLevel(double multiplier) {
            this.multiplier = multiplier;
        }

        public double getMultiplier() { return multiplier; }
    }

    private String id;
    private String sourceId;
    private String targetId;
    private double distance;
    private TrafficLevel traffic;

    public Edge() {
        this.id = UUID.randomUUID().toString();
        this.traffic = TrafficLevel.NORMAL;
    }

    public Edge(String id, String sourceId, String targetId, double distance, TrafficLevel traffic) {
        this.id = id;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.distance = distance;
        this.traffic = traffic;
    }

    public Edge(String sourceId, String targetId, double distance, TrafficLevel traffic) {
        this.id = UUID.randomUUID().toString();
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.distance = distance;
        this.traffic = traffic;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public TrafficLevel getTraffic() { return traffic; }
    public void setTraffic(TrafficLevel traffic) { this.traffic = traffic; }

    public double getEffectiveDistance() {
        if (traffic == TrafficLevel.BLOCKED) return Double.POSITIVE_INFINITY;
        return distance * traffic.getMultiplier();
    }

    public boolean isBlocked() {
        return traffic == TrafficLevel.BLOCKED;
    }

    public static class WeightedNeighbor {
        private final String nodeId;
        private final double weight;

        public WeightedNeighbor(String nodeId, double weight) {
            this.nodeId = nodeId;
            this.weight = weight;
        }

        public String getNodeId() { return nodeId; }
        public double getWeight() { return weight; }

        @Override
        public String toString() {
            return String.format("Neighbor{node='%s', weight=%.2f}", nodeId, weight);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge edge = (Edge) o;
        return Objects.equals(id, edge.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("Edge{id='%s', source='%s', target='%s', dist=%.1f, traffic=%s}",
                id, sourceId, targetId, distance, traffic);
    }
}
