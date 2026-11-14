package com.ambulance.controller;

import com.ambulance.dto.AccidentLocationRequest;
import com.ambulance.dto.AccidentLocationResponse;
import com.ambulance.service.AccidentLocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accidents")
public class AccidentLocationController {

    private final AccidentLocationService service;

    public AccidentLocationController(AccidentLocationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AccidentLocationResponse> create(@Valid @RequestBody AccidentLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<AccidentLocationResponse>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<AccidentLocationResponse>> getActive() {
        return ResponseEntity.ok(service.findActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccidentLocationResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccidentLocationResponse> update(@PathVariable String id,
                                                            @Valid @RequestBody AccidentLocationRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
