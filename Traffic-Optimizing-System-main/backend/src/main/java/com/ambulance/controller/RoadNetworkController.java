package com.ambulance.controller;

import com.ambulance.dto.RoadNetworkRequest;
import com.ambulance.dto.RoadNetworkResponse;
import com.ambulance.service.RoadNetworkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roads")
public class RoadNetworkController {

    private final RoadNetworkService service;

    public RoadNetworkController(RoadNetworkService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RoadNetworkResponse> create(@Valid @RequestBody RoadNetworkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<RoadNetworkResponse>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoadNetworkResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoadNetworkResponse> update(@PathVariable String id,
                                                       @Valid @RequestBody RoadNetworkRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
