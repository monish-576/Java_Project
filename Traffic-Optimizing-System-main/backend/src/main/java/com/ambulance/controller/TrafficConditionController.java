package com.ambulance.controller;

import com.ambulance.dto.TrafficConditionRequest;
import com.ambulance.dto.TrafficConditionResponse;
import com.ambulance.service.TrafficConditionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traffic")
public class TrafficConditionController {

    private final TrafficConditionService service;

    public TrafficConditionController(TrafficConditionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TrafficConditionResponse> create(@Valid @RequestBody TrafficConditionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<TrafficConditionResponse>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrafficConditionResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/by-road/{roadId}")
    public ResponseEntity<List<TrafficConditionResponse>> getByRoadId(@PathVariable String roadId) {
        return ResponseEntity.ok(service.findRecentByRoadId(roadId));
    }

    @GetMapping("/congestion")
    public ResponseEntity<List<TrafficConditionResponse>> getActiveCongestion() {
        return ResponseEntity.ok(service.findActiveCongestion());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrafficConditionResponse> update(@PathVariable String id,
                                                            @Valid @RequestBody TrafficConditionRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
