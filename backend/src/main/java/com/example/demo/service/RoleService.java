package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    /**
     * Handle the creation of a new role.
     *
     * @param role The role to be created.
     * @return The created Role object.
     */
    public Role handleCreateRole(Role role) {
        return this.roleRepository.save(role);
    }

    /**
     * Handle the update of an existing role.
     *
     * @param role The role to be updated.
     * @return The updated Role object.
     */
    public Role handleUpdateRole(Role role) {
        Role currentRole = this.roleRepository.findById(role.getRoleId()).orElse(null);
        if (currentRole == null) {
            throw new RuntimeException("Role not found");
        }
        currentRole.setRoleName(role.getRoleName());
        return this.roleRepository.save(currentRole);
    }

    public List<Role> fetchAllRoles() {
        return this.roleRepository.findAll();
    }

    /**
     * Find a role by its ID.
     *
     * @param roleId The ID of the role.
     * @return The Role object if found, otherwise null.
     */
    public Role findRoleById(long roleId) {
        return this.roleRepository.findById(roleId).orElse(null);
    }

    /**
     * Find a role by its name.
     *
     * @param name The name of the role.
     * @return The Role object if found, otherwise null.
     */
    public Role findRoleByName(String name) {
        return this.roleRepository.findByRoleName(name).orElse(null);
    }
}
