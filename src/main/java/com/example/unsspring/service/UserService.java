package com.example.unsspring.service;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.unsspring.model.Role;
import com.example.unsspring.model.User;
import com.example.unsspring.repository.RoleRepository;
import com.example.unsspring.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Create
    public User registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    // Read
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Update
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);

        // Update username if provided and different
        if (userDetails.getUsername() != null && !userDetails.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(userDetails.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists");
            }
            user.setUsername(userDetails.getUsername());
        }

        // Update email if provided and different
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(userDetails.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(userDetails.getEmail());
        }

        // Update password if provided
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        // Update roles if provided
        if (userDetails.getRoles() != null && !userDetails.getRoles().isEmpty()) {
            Set<Role> newRoles = new HashSet<>();
            for (Role role : userDetails.getRoles()) {
                Role existingRole = roleRepository.findByName(role.getName())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + role.getName()));
                newRoles.add(existingRole);
            }
            user.setRoles(newRoles);
        }

        return userRepository.save(user);
    }

    public User updateUserRoles(Long userId, Set<String> roleNames) {
        User user = getUserById(userId);
        
        // Security validation: Don't allow removing the last ADMIN
        boolean isRemovingLastAdmin = user.getRoles().stream()
            .anyMatch(r -> r.getName().equals("ADMIN")) &&
            !roleNames.contains("ADMIN");
        
        if (isRemovingLastAdmin && roleRepository.countByName("ADMIN") <= 1) {
            throw new RuntimeException("Cannot remove the last admin user");
        }
        
        // Convert role names to Role entities
        Set<Role> newRoles = roleNames.stream()
            .map(name -> roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Role not found: " + name)))
            .collect(Collectors.toSet());
        
        user.setRoles(newRoles);
        return userRepository.save(user);
    }

    // Delete
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
} 