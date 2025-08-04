package com.example.demo.controller;

import com.example.demo.DTO.response.ApiResponse;

import com.example.demo.model.Role;
import com.example.demo.path.RolePath;
import com.example.demo.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/public")
public class RoleController {
    private final RoleService roleService;
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping(RolePath.CREATE_ROLE)
    public ApiResponse<Role> createRole(@RequestBody Role role) {
        if(this.roleService.findRoleByName(role.getRoleName()) != null) {
            throw new RuntimeException("Role already exists");
        }
        return ApiResponse.<Role>builder()
                .status(HttpStatus.CREATED.value())
                .message("Create a role")
                .result(this.roleService.handleCreateRole(role))
                .build();
    }

    @PutMapping(RolePath.UPDATE_ROLE)
    public ApiResponse<Role> updateRole(@RequestBody Role role) {
        if(this.roleService.findRoleById(role.getRoleId()) == null) {
            throw new RuntimeException("Role does not exist");
        }
        return ApiResponse.<Role>builder()
                .status(HttpStatus.OK.value())
                .message("Update a role")
                .result(this.roleService.handleUpdateRole(role))
                .build();
    }

    @GetMapping(RolePath.GET_ALL_ROLES)
    public ApiResponse<List<Role>> getAllRoles() {
        return ApiResponse.<List<Role>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetch all role")
                .result(this.roleService.fetchAllRoles())
                .build();
    }
}
