package com.ambulance.controller;

import com.ambulance.dto.AmbulanceRequest;
import com.ambulance.dto.AmbulanceResponse;
import com.ambulance.service.AmbulanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ambulances")
public class AmbulanceController {

    private final AmbulanceService service;

    public AmbulanceController(AmbulanceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AmbulanceResponse> create(@Valid @RequestBody AmbulanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<AmbulanceResponse>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AmbulanceResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/by-hospital/{hospitalId}")
    public ResponseEntity<List<AmbulanceResponse>> getByHospitalId(@PathVariable String hospitalId) {
        return ResponseEntity.ok(service.findByHospitalId(hospitalId));
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<AmbulanceResponse>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(service.findByStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AmbulanceResponse> update(@PathVariable String id,
                                                     @Valid @RequestBody AmbulanceRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
