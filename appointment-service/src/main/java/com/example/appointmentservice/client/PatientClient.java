package com.example.appointmentservice.client;

import com.example.appointmentservice.dto.PatientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "api-gateway")
public interface PatientClient {

    @GetMapping("/patients/{id}")
    PatientDto getPatientById(@PathVariable("id") Long id);
}
