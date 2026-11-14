package com.ambulance.service;

import com.ambulance.dto.HospitalRequest;
import com.ambulance.dto.HospitalResponse;
import com.ambulance.model.Hospital;
import com.ambulance.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalService {

    private final HospitalRepository repository;

    public HospitalService(HospitalRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public HospitalResponse create(HospitalRequest request) {
        Hospital hospital = new Hospital();
        hospital.setName(request.getName());
        hospital.setAddress(request.getAddress());
        hospital.setLatitude(request.getLatitude());
        hospital.setLongitude(request.getLongitude());
        hospital.setPhone(request.getPhone());
        hospital.setEmergencyLevel(request.getEmergencyLevel() != null ? request.getEmergencyLevel() : "general");
        return HospitalResponse.fromEntity(repository.save(hospital));
    }

    public List<HospitalResponse> findAll() {
        return repository.findAll().stream()
                .map(HospitalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public HospitalResponse findById(String id) {
        return repository.findById(id)
                .map(HospitalResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("Hospital not found: " + id));
    }

    @Transactional
    public HospitalResponse update(String id, HospitalRequest request) {
        Hospital existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital not found: " + id));
        existing.setName(request.getName());
        existing.setAddress(request.getAddress());
        existing.setLatitude(request.getLatitude());
        existing.setLongitude(request.getLongitude());
        existing.setPhone(request.getPhone());
        existing.setEmergencyLevel(request.getEmergencyLevel() != null ? request.getEmergencyLevel() : "general");
        repository.update(existing);
        return HospitalResponse.fromEntity(
                repository.findById(id).orElse(existing));
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Hospital not found: " + id);
        }
        repository.deleteById(id);
    }
}
