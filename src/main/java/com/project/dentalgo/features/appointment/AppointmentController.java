package com.project.dentalgo.features.appointment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:3000")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    // Book an appointment
    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment) {
        // Clinic-wide check: no two patients can book the same date + time slot
        boolean conflict = appointmentRepository.findAll()
                .stream()
                .filter(a -> !a.isDeleted())
                .filter(a -> !"CANCELLED".equals(a.getStatus()))
                .anyMatch(a ->
                        appointment.getAppointmentDate().equals(a.getAppointmentDate()) &&
                        appointment.getStartTime().equals(a.getStartTime())
                );

        if (conflict) {
            return ResponseEntity.status(409)
                    .body("The time slot " + appointment.getStartTime() +
                          " on " + appointment.getAppointmentDate() +
                          " is already taken. Please choose a different date or time.");
        }

        appointment.setStatus("PENDING");
        appointment.setCreatedAt(LocalDateTime.now().toString());
        return ResponseEntity.status(201).body(appointmentRepository.save(appointment));
    }

    // Get all appointments for a patient
    @GetMapping
    public ResponseEntity<List<Appointment>> getAppointments(@RequestParam String email) {
        return ResponseEntity.ok(appointmentRepository.findByPatientEmail(email));
    }

    // Get a single appointment
    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable String id) {
        Optional<Appointment> apt = appointmentRepository.findById(id);
        return apt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(null));
    }

    // Update appointment status (Admin)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        Optional<Appointment> aptOpt = appointmentRepository.findById(id);
        if (aptOpt.isPresent()) {
            Appointment apt = aptOpt.get();
            apt.setStatus(body.get("status"));
            return ResponseEntity.ok(appointmentRepository.save(apt));
        }
        return ResponseEntity.status(404).body("Appointment not found");
    }

    // Cancel an appointment
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable String id) {
        if (appointmentRepository.existsById(id)) {
            appointmentRepository.deleteById(id);
            return ResponseEntity.ok("Appointment cancelled");
        }
        return ResponseEntity.status(404).body("Appointment not found");
    }
}
