package com.ambulance.service;

import com.ambulance.dto.AmbulanceRequest;
import com.ambulance.dto.AmbulanceResponse;
import com.ambulance.model.Ambulance;
import com.ambulance.repository.AmbulanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmbulanceService {

    private final AmbulanceRepository repository;

    public AmbulanceService(AmbulanceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AmbulanceResponse create(AmbulanceRequest request) {
        Ambulance ambulance = new Ambulance();
        ambulance.setHospitalId(request.getHospitalId());
        ambulance.setVehicleNumber(request.getVehicleNumber());
        ambulance.setDriverName(request.getDriverName());
        ambulance.setDriverPhone(request.getDriverPhone());
        ambulance.setStatus(request.getStatus() != null ? request.getStatus() : "AVAILABLE");
        ambulance.setCurrentLat(request.getCurrentLat());
        ambulance.setCurrentLng(request.getCurrentLng());
        return AmbulanceResponse.fromEntity(repository.save(ambulance));
    }

    public List<AmbulanceResponse> findAll() {
        return repository.findAll().stream()
                .map(AmbulanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public AmbulanceResponse findById(String id) {
        return repository.findById(id)
                .map(AmbulanceResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("Ambulance not found: " + id));
    }

    public List<AmbulanceResponse> findByHospitalId(String hospitalId) {
        return repository.findByHospitalId(hospitalId).stream()
                .map(AmbulanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AmbulanceResponse> findByStatus(String status) {
        return repository.findByStatus(status).stream()
                .map(AmbulanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AmbulanceResponse update(String id, AmbulanceRequest request) {
        Ambulance existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ambulance not found: " + id));
        existing.setHospitalId(request.getHospitalId());
        existing.setVehicleNumber(request.getVehicleNumber());
        existing.setDriverName(request.getDriverName());
        existing.setDriverPhone(request.getDriverPhone());
        existing.setStatus(request.getStatus() != null ? request.getStatus() : "AVAILABLE");
        existing.setCurrentLat(request.getCurrentLat());
        existing.setCurrentLng(request.getCurrentLng());
        repository.update(existing);
        return AmbulanceResponse.fromEntity(
                repository.findById(id).orElse(existing));
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Ambulance not found: " + id);
        }
        repository.deleteById(id);
    }
}
