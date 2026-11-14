package com.ambulance.routing.model;

import java.util.Objects;
import java.util.UUID;

public class Node {

    public enum NodeType {
        REGULAR,
        HOSPITAL,
        ACCIDENT,
        AMBULANCE_DEPOT
    }

    private String id;
    private String name;
    private NodeType nodeType;
    private int x;
    private int y;

    public Node() {
        this.id = UUID.randomUUID().toString();
        this.nodeType = NodeType.REGULAR;
    }

    public Node(String id, String name, NodeType nodeType, int x, int y) {
        this.id = id;
        this.name = name;
        this.nodeType = nodeType;
        this.x = x;
        this.y = y;
    }

    public Node(String name, NodeType nodeType, int x, int y) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.nodeType = nodeType;
        this.x = x;
        this.y = y;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public NodeType getNodeType() { return nodeType; }
    public void setNodeType(NodeType nodeType) { this.nodeType = nodeType; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public double euclideanDistanceTo(Node other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("Node{id='%s', name='%s', type=%s, x=%d, y=%d}", id, name, nodeType, x, y);
    }
}
