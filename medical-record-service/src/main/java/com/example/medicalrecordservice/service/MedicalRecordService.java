package com.example.medicalrecordservice.service;

import com.example.medicalrecordservice.entity.MedicalRecord;

import java.util.List;

public interface MedicalRecordService {

    MedicalRecord createRecord(MedicalRecord record);

    List<MedicalRecord> getRecordsByPatient(Long patientId);
}
