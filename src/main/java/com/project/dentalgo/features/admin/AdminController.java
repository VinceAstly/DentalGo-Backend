package com.project.dentalgo.features.admin;

import com.project.dentalgo.features.appointment.Appointment;
import com.project.dentalgo.features.appointment.AppointmentRepository;
import com.project.dentalgo.features.user.User;
import com.project.dentalgo.features.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    // GET all appointments (admin only) — excludes soft-deleted
    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        List<Appointment> all = appointmentRepository.findAll();
        List<Appointment> active = all.stream()
                .filter(a -> !a.isDeleted())
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(active);
    }

    // GET all users (admin only)
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Strip passwords before returning
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    // GET dashboard stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<Appointment> allApts = appointmentRepository.findAll().stream()
                .filter(a -> !a.isDeleted())
                .collect(java.util.stream.Collectors.toList());
        long pending   = allApts.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        long confirmed = allApts.stream().filter(a -> "CONFIRMED".equals(a.getStatus())).count();
        long cancelled = allApts.stream().filter(a -> "CANCELLED".equals(a.getStatus())).count();
        long completed = allApts.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers",        userRepository.count());
        stats.put("totalAppointments", allApts.size());
        stats.put("pendingCount",      pending);
        stats.put("confirmedCount",    confirmed);
        stats.put("cancelledCount",    cancelled);
        stats.put("completedCount",    completed);
        return ResponseEntity.ok(stats);
    }

    // UPDATE appointment status
    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable String id,
                                                     @RequestBody Map<String, String> body) {
        Optional<Appointment> aptOpt = appointmentRepository.findById(id);
        if (aptOpt.isPresent()) {
            Appointment apt = aptOpt.get();
            apt.setStatus(body.get("status"));
            return ResponseEntity.ok(appointmentRepository.save(apt));
        }
        return ResponseEntity.status(404).body("Appointment not found");
    }

    // SOFT-DELETE appointment (keeps record in DB for patient history)
    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable String id) {
        Optional<Appointment> aptOpt = appointmentRepository.findById(id);
        if (aptOpt.isPresent()) {
            Appointment apt = aptOpt.get();
            apt.setDeleted(true);
            appointmentRepository.save(apt);
            return ResponseEntity.ok("Appointment removed");
        }
        return ResponseEntity.status(404).body("Appointment not found");
    }

    // UPDATE user role
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable String id,
                                            @RequestBody Map<String, String> body) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(body.get("role"));
            userRepository.save(user);
            return ResponseEntity.ok("Role updated");
        }
        return ResponseEntity.status(404).body("User not found");
    }
}
