package com.ambulance.service;

import com.ambulance.dto.AccidentLocationRequest;
import com.ambulance.dto.AccidentLocationResponse;
import com.ambulance.model.AccidentLocation;
import com.ambulance.repository.AccidentLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccidentLocationService {

    private final AccidentLocationRepository repository;

    public AccidentLocationService(AccidentLocationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AccidentLocationResponse create(AccidentLocationRequest request) {
        AccidentLocation loc = new AccidentLocation();
        loc.setNodeId(request.getNodeId());
        loc.setLatitude(request.getLatitude());
        loc.setLongitude(request.getLongitude());
        loc.setSeverity(request.getSeverity() != null ? request.getSeverity() : "MODERATE");
        loc.setDescription(request.getDescription());
        loc.setActive(true);
        return AccidentLocationResponse.fromEntity(repository.save(loc));
    }

    public List<AccidentLocationResponse> findAll() {
        return repository.findAll().stream()
                .map(AccidentLocationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AccidentLocationResponse> findActive() {
        return repository.findActive().stream()
                .map(AccidentLocationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public AccidentLocationResponse findById(String id) {
        return repository.findById(id)
                .map(AccidentLocationResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("Accident location not found: " + id));
    }

    @Transactional
    public AccidentLocationResponse update(String id, AccidentLocationRequest request) {
        AccidentLocation existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Accident location not found: " + id));
        existing.setNodeId(request.getNodeId());
        existing.setLatitude(request.getLatitude());
        existing.setLongitude(request.getLongitude());
        existing.setSeverity(request.getSeverity() != null ? request.getSeverity() : "MODERATE");
        existing.setDescription(request.getDescription());
        repository.update(existing);
        return AccidentLocationResponse.fromEntity(
                repository.findById(id).orElse(existing));
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Accident location not found: " + id);
        }
        repository.deleteById(id);
    }
}
