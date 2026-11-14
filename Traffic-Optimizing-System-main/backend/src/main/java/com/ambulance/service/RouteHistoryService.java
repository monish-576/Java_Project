package com.ambulance.service;

import com.ambulance.dto.RouteHistoryResponse;
import com.ambulance.model.RouteHistory;
import com.ambulance.repository.RouteHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteHistoryService {

    private static final Logger log = LoggerFactory.getLogger(RouteHistoryService.class);
    private final RouteHistoryRepository repository;

    public RouteHistoryService(RouteHistoryRepository repository) {
        this.repository = repository;
    }

    public List<RouteHistoryResponse> findAll() {
        return repository.findAll().stream()
                .map(RouteHistoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public RouteHistoryResponse findById(String id) {
        return repository.findById(id)
                .map(RouteHistoryResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("Route history not found: " + id));
    }

    @Transactional
    public RouteHistory save(RouteHistory history) {
        RouteHistory saved = repository.save(history);
        log.info("Route history saved: id={}, algo={}, source={}, dest={}, dist={}",
                saved.getId(), saved.getAlgorithm(), saved.getSourceNode(),
                saved.getDestinationNode(), saved.getDistanceKm());
        return saved;
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Route history not found: " + id);
        }
        repository.deleteById(id);
        log.info("Route history deleted: {}", id);
    }

    @Transactional
    public void deleteAll() {
        List<RouteHistory> all = repository.findAll();
        for (RouteHistory h : all) {
            repository.deleteById(h.getId());
        }
        log.info("All {} route history entries deleted", all.size());
    }
}
