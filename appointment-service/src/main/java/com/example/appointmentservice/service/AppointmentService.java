package com.example.appointmentservice.service;

import com.example.appointmentservice.entity.Appointment;

import java.util.List;

public interface AppointmentService {

    Appointment createAppointment(Appointment appointment);

    List<Appointment> getAppointmentsByPatient(Long patientId);
}
