package com.ambulance.service;

import com.ambulance.dto.TrafficConditionRequest;
import com.ambulance.dto.TrafficConditionResponse;
import com.ambulance.model.TrafficCondition;
import com.ambulance.repository.TrafficConditionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrafficConditionService {

    private final TrafficConditionRepository repository;

    public TrafficConditionService(TrafficConditionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TrafficConditionResponse create(TrafficConditionRequest request) {
        TrafficCondition tc = new TrafficCondition();
        tc.setRoadId(request.getRoadId());
        tc.setCongestionLevel(request.getCongestionLevel() != null ? request.getCongestionLevel() : "NORMAL");
        tc.setAvgSpeedKmh(request.getAvgSpeedKmh());
        tc.setSource(request.getSource() != null ? request.getSource() : "system");
        return TrafficConditionResponse.fromEntity(repository.save(tc));
    }

    public List<TrafficConditionResponse> findAll() {
        return repository.findAll().stream()
                .map(TrafficConditionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public TrafficConditionResponse findById(String id) {
        return repository.findById(id)
                .map(TrafficConditionResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("Traffic condition not found: " + id));
    }

    public List<TrafficConditionResponse> findRecentByRoadId(String roadId) {
        return repository.findRecentByRoadId(roadId).stream()
                .map(TrafficConditionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TrafficConditionResponse> findActiveCongestion() {
        return repository.findActiveCongestion().stream()
                .map(TrafficConditionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrafficConditionResponse update(String id, TrafficConditionRequest request) {
        TrafficCondition existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Traffic condition not found: " + id));
        existing.setRoadId(request.getRoadId());
        existing.setCongestionLevel(request.getCongestionLevel() != null ? request.getCongestionLevel() : "NORMAL");
        existing.setAvgSpeedKmh(request.getAvgSpeedKmh());
        existing.setSource(request.getSource() != null ? request.getSource() : "system");
        repository.update(existing);
        return TrafficConditionResponse.fromEntity(
                repository.findById(id).orElse(existing));
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Traffic condition not found: " + id);
        }
        repository.deleteById(id);
    }
}
