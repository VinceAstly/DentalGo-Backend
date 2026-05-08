package com.project.dentalgo.features.appointment;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "appointments")
public class Appointment {
    @Id
    private String id;
    private String patientEmail;
    private String serviceId;
    private String serviceName;
    private String appointmentDate;
    private String startTime;
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED
    private String notes;
    private String createdAt;
    private boolean deleted = false; // soft-delete flag
}
