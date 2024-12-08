package com.example.unsspring.service;

import com.example.unsspring.model.Role;
import com.example.unsspring.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    
    @Autowired
    private RoleRepository roleRepository;
    
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    public Role createRole(Role role) {
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new RuntimeException("Role already exists");
        }
        return roleRepository.save(role);
    }
    
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Role not found"));
    }
    
    public void deleteRole(Long id) {
        Role role = getRoleById(id);
        
        // Prevent deletion of ADMIN role
        if ("ADMIN".equals(role.getName())) {
            throw new RuntimeException("Cannot delete ADMIN role");
        }
        
        roleRepository.deleteById(id);
    }
} 