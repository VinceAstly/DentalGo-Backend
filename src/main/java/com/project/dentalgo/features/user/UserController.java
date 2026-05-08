package com.project.dentalgo.features.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User savedUser = userService.registerUser(user);
            savedUser.setPassword(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginData) {
        try {
            User user = userService.login(loginData.getEmail(), loginData.getPassword());
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // GET PROFILE by query param (e.g. ?email=...)
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestParam String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) { user.get().setPassword(null); }
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(null));
    }

    // GET PROFILE by path variable (used by Dashboard & Profile pages)
    @GetMapping("/profile/{email}")
    public ResponseEntity<?> getUserProfileByPath(@PathVariable String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) { user.get().setPassword(null); }
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(null));
    }

    // EDIT PROFILE
    @PutMapping("/profile/edit/{email}")
    public ResponseEntity<?> updateUserProfile(@PathVariable String email, @RequestBody User updatedData) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User existingUser = userOpt.get();
            existingUser.setFullName(updatedData.getFullName());
            existingUser.setPhone(updatedData.getPhone());
            existingUser.setBio(updatedData.getBio());
            userRepository.save(existingUser);
            return ResponseEntity.ok("Profile updated successfully!");
        }
        return ResponseEntity.status(404).body("User not found");
    }

    // CHANGE PASSWORD
    @PutMapping("/change-password/{email}")
    public ResponseEntity<?> changePassword(@PathVariable String email, @RequestBody Map<String, String> request) {
        String newPassword = request.get("password");
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return ResponseEntity.ok("Password changed successfully!");
        }
        return ResponseEntity.status(404).body("User not found");
    }

    // UPLOAD PHOTO
    @PostMapping("/upload-photo/{email}")
    public ResponseEntity<?> uploadPhoto(@PathVariable String email, @RequestParam("file") MultipartFile file) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setProfileImage(file.getBytes());
                userRepository.save(user);
                return ResponseEntity.ok("Photo uploaded successfully! Size: " + file.getSize());
            }
            return ResponseEntity.status(404).body("User not found");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error uploading file");
        }
    }
}
