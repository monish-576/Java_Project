package com.ambulance.service;

import com.ambulance.dto.RoadNetworkRequest;
import com.ambulance.dto.RoadNetworkResponse;
import com.ambulance.model.RoadNetwork;
import com.ambulance.repository.RoadNetworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoadNetworkService {

    private final RoadNetworkRepository repository;

    public RoadNetworkService(RoadNetworkRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RoadNetworkResponse create(RoadNetworkRequest request) {
        RoadNetwork road = new RoadNetwork();
        road.setSourceNode(request.getSourceNode());
        road.setTargetNode(request.getTargetNode());
        road.setDistanceKm(request.getDistanceKm());
        road.setRoadName(request.getRoadName());
        road.setRoadType(request.getRoadType() != null ? request.getRoadType() : "local");
        road.setSpeedLimit(request.getSpeedLimit() != null ? request.getSpeedLimit() : 40);
        road.setOneWay(request.isOneWay());
        return RoadNetworkResponse.fromEntity(repository.save(road));
    }

    public List<RoadNetworkResponse> findAll() {
        return repository.findAll().stream()
                .map(RoadNetworkResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public RoadNetworkResponse findById(String id) {
        return repository.findById(id)
                .map(RoadNetworkResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("Road not found: " + id));
    }

    @Transactional
    public RoadNetworkResponse update(String id, RoadNetworkRequest request) {
        RoadNetwork existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Road not found: " + id));
        existing.setSourceNode(request.getSourceNode());
        existing.setTargetNode(request.getTargetNode());
        existing.setDistanceKm(request.getDistanceKm());
        existing.setRoadName(request.getRoadName());
        existing.setRoadType(request.getRoadType() != null ? request.getRoadType() : "local");
        existing.setSpeedLimit(request.getSpeedLimit() != null ? request.getSpeedLimit() : 40);
        existing.setOneWay(request.isOneWay());
        repository.update(existing);
        return RoadNetworkResponse.fromEntity(
                repository.findById(id).orElse(existing));
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Road not found: " + id);
        }
        repository.deleteById(id);
    }
}
