package com.example.medicalrecordservice.service.impl;

import com.example.medicalrecordservice.client.PatientClient;
import com.example.medicalrecordservice.entity.MedicalRecord;
import com.example.medicalrecordservice.repository.MedicalRecordRepository;
import com.example.medicalrecordservice.service.MedicalRecordService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientClient patientClient;

    public MedicalRecordServiceImpl(MedicalRecordRepository medicalRecordRepository, PatientClient patientClient) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientClient = patientClient;
    }

    @Override
    public MedicalRecord createRecord(MedicalRecord record) {
        try {
            validatePatient(record.getPatientId()).join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        }
        return medicalRecordRepository.save(record);
    }

    @Override
    public List<MedicalRecord> getRecordsByPatient(Long patientId) {
        return medicalRecordRepository.findByPatientId(patientId);
    }

    @CircuitBreaker(name = "patientService", fallbackMethod = "patientServiceFallback")
    @Retry(name = "patientService")
    @TimeLimiter(name = "patientService")
    public CompletableFuture<Void> validatePatient(Long patientId) {
        return CompletableFuture.runAsync(() -> patientClient.getPatientById(patientId));
    }

    public CompletableFuture<Void> patientServiceFallback(Long patientId, Throwable throwable) {
        return CompletableFuture.failedFuture(new IllegalStateException("Patient service unavailable, please try later"));
    }
}
