package com.example.appointmentservice.service.impl;

import com.example.appointmentservice.client.PatientClient;
import com.example.appointmentservice.entity.Appointment;
import com.example.appointmentservice.repository.AppointmentRepository;
import com.example.appointmentservice.service.AppointmentService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientClient patientClient;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, PatientClient patientClient) {
        this.appointmentRepository = appointmentRepository;
        this.patientClient = patientClient;
    }

    @Override
    public Appointment createAppointment(Appointment appointment) {
        try {
            validatePatient(appointment.getPatientId()).join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        }
        return appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
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
