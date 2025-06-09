package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role handleCreateRole(Role role) {
        return this.roleRepository.save(role);
    }

    public Role handleUpdateRole(Role role) {
        Role currentRole = this.roleRepository.findById(role.getRoleId()).orElse(null);
        if(currentRole == null) {
            throw new RuntimeException("Role not found");
        }
        currentRole.setRoleName(role.getRoleName());
        return this.roleRepository.save(currentRole);
    }

    public List<Role> fetchAllRoles() {
        return this.roleRepository.findAll();
    }

    public Role findRoleById(long roleId) {
        return this.roleRepository.findById(roleId).orElse(null);
    }

    public Role findRoleByName(String name) {
        return this.roleRepository.findByRoleName(name).orElse(null);
    }
}
