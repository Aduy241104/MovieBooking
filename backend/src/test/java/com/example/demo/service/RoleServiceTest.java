package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleService
 * Testing role management operations, CRUD functionality and data retrieval
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role testRole;
    private List<Role> testRoleList;

    @BeforeEach
    void setUp() {
        // Setup test role
        testRole = new Role();
        testRole.setRoleId(1L);
        testRole.setRoleName("ADMIN");

        // Setup test role list
        testRoleList = new ArrayList<>();
        testRoleList.add(testRole);
        testRoleList.add(new Role(2L, "EMPLOYEE"));
        testRoleList.add(new Role(3L, "CUSTOMER"));
    }

    // ========== HANDLE CREATE ROLE TESTS ==========

    @Test
    void testHandleCreateRole_whenValidRole_shouldCreateSuccessfully() {
        // Arrange
        Role newRole = new Role();
        newRole.setRoleName("MANAGER");

        Role savedRole = new Role(4L, "MANAGER");
        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        // Act
        Role result = roleService.handleCreateRole(newRole);

        // Assert
        assertNotNull(result);
        assertEquals(4L, result.getRoleId());
        assertEquals("MANAGER", result.getRoleName());
        verify(roleRepository).save(newRole);
    }

    @Test
    void testHandleCreateRole_whenRoleIsNull_shouldHandleGracefully() {
        // Arrange
        when(roleRepository.save(null)).thenReturn(null);

        // Act
        Role result = roleService.handleCreateRole(null);

        // Assert
        assertNull(result);
        verify(roleRepository).save(null);
    }

    @Test
    void testHandleCreateRole_whenRoleWithNullName_shouldCreateSuccessfully() {
        // Arrange
        Role newRole = new Role();
        newRole.setRoleName(null);

        Role savedRole = new Role(5L, null);
        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        // Act
        Role result = roleService.handleCreateRole(newRole);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getRoleId());
        assertNull(result.getRoleName());
        verify(roleRepository).save(newRole);
    }

    // ========== HANDLE UPDATE ROLE TESTS ==========

    @Test
    void testHandleUpdateRole_whenValidRole_shouldUpdateSuccessfully() {
        // Arrange
        Role updateRole = new Role();
        updateRole.setRoleId(1L);
        updateRole.setRoleName("SUPER_ADMIN");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // Act
        Role result = roleService.handleUpdateRole(updateRole);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getRoleId());
        assertEquals("SUPER_ADMIN", result.getRoleName());
        verify(roleRepository).findById(1L);
        verify(roleRepository).save(testRole);
    }

    @Test
    void testHandleUpdateRole_whenRoleNotFound_shouldThrowRuntimeException() {
        // Arrange
        Role updateRole = new Role();
        updateRole.setRoleId(999L);
        updateRole.setRoleName("NONEXISTENT");

        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> roleService.handleUpdateRole(updateRole));
        assertEquals("Role not found", exception.getMessage());
        verify(roleRepository).findById(999L);
        verify(roleRepository, never()).save(any());
    }

    @Test
    void testHandleUpdateRole_whenUpdatingToNullName_shouldUpdateSuccessfully() {
        // Arrange
        Role updateRole = new Role();
        updateRole.setRoleId(1L);
        updateRole.setRoleName(null);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // Act
        Role result = roleService.handleUpdateRole(updateRole);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getRoleId());
        assertNull(result.getRoleName());
        verify(roleRepository).findById(1L);
        verify(roleRepository).save(testRole);
    }

    @Test
    void testHandleUpdateRole_whenUpdatingToEmptyName_shouldUpdateSuccessfully() {
        // Arrange
        Role updateRole = new Role();
        updateRole.setRoleId(1L);
        updateRole.setRoleName("");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // Act
        Role result = roleService.handleUpdateRole(updateRole);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getRoleId());
        assertEquals("", result.getRoleName());
        verify(roleRepository).findById(1L);
        verify(roleRepository).save(testRole);
    }

    // ========== FETCH ALL ROLES TESTS ==========

    @Test
    void testFetchAllRoles_whenRolesExist_shouldReturnAllRoles() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(testRoleList);

        // Act
        List<Role> result = roleService.fetchAllRoles();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("ADMIN", result.get(0).getRoleName());
        assertEquals("EMPLOYEE", result.get(1).getRoleName());
        assertEquals("CUSTOMER", result.get(2).getRoleName());
        verify(roleRepository).findAll();
    }

    @Test
    void testFetchAllRoles_whenNoRolesExist_shouldReturnEmptyList() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<Role> result = roleService.fetchAllRoles();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(roleRepository).findAll();
    }

    @Test
    void testFetchAllRoles_whenRepositoryReturnsNull_shouldReturnNull() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(null);

        // Act
        List<Role> result = roleService.fetchAllRoles();

        // Assert
        assertNull(result);
        verify(roleRepository).findAll();
    }

    // ========== FIND ROLE BY ID TESTS ==========

    @Test
    void testFindRoleById_whenRoleExists_shouldReturnRole() {
        // Arrange
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));

        // Act
        Role result = roleService.findRoleById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getRoleId());
        assertEquals("ADMIN", result.getRoleName());
        verify(roleRepository).findById(1L);
    }

    @Test
    void testFindRoleById_whenRoleNotExists_shouldReturnNull() {
        // Arrange
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Role result = roleService.findRoleById(999L);

        // Assert
        assertNull(result);
        verify(roleRepository).findById(999L);
    }

    @Test
    void testFindRoleById_whenIdIsZero_shouldReturnNull() {
        // Arrange
        when(roleRepository.findById(0L)).thenReturn(Optional.empty());

        // Act
        Role result = roleService.findRoleById(0L);

        // Assert
        assertNull(result);
        verify(roleRepository).findById(0L);
    }

    @Test
    void testFindRoleById_whenIdIsNegative_shouldReturnNull() {
        // Arrange
        when(roleRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act
        Role result = roleService.findRoleById(-1L);

        // Assert
        assertNull(result);
        verify(roleRepository).findById(-1L);
    }

    // ========== FIND ROLE BY NAME TESTS ==========

    @Test
    void testFindRoleByName_whenRoleExists_shouldReturnRole() {
        // Arrange
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(testRole));

        // Act
        Role result = roleService.findRoleByName("ADMIN");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getRoleId());
        assertEquals("ADMIN", result.getRoleName());
        verify(roleRepository).findByRoleName("ADMIN");
    }

    @Test
    void testFindRoleByName_whenRoleNotExists_shouldReturnNull() {
        // Arrange
        when(roleRepository.findByRoleName("NONEXISTENT")).thenReturn(Optional.empty());

        // Act
        Role result = roleService.findRoleByName("NONEXISTENT");

        // Assert
        assertNull(result);
        verify(roleRepository).findByRoleName("NONEXISTENT");
    }

    @Test
    void testFindRoleByName_whenNameIsNull_shouldReturnNull() {
        // Arrange
        when(roleRepository.findByRoleName(null)).thenReturn(Optional.empty());

        // Act
        Role result = roleService.findRoleByName(null);

        // Assert
        assertNull(result);
        verify(roleRepository).findByRoleName(null);
    }

    @Test
    void testFindRoleByName_whenNameIsEmpty_shouldReturnNull() {
        // Arrange
        when(roleRepository.findByRoleName("")).thenReturn(Optional.empty());

        // Act
        Role result = roleService.findRoleByName("");

        // Assert
        assertNull(result);
        verify(roleRepository).findByRoleName("");
    }

    @Test
    void testFindRoleByName_whenNameHasWhitespace_shouldReturnRole() {
        // Arrange
        Role roleWithSpaces = new Role(4L, " ADMIN ");
        when(roleRepository.findByRoleName(" ADMIN ")).thenReturn(Optional.of(roleWithSpaces));

        // Act
        Role result = roleService.findRoleByName(" ADMIN ");

        // Assert
        assertNotNull(result);
        assertEquals(4L, result.getRoleId());
        assertEquals(" ADMIN ", result.getRoleName());
        verify(roleRepository).findByRoleName(" ADMIN ");
    }

    @Test
    void testFindRoleByName_caseSensitivity_shouldReturnNull() {
        // Arrange
        when(roleRepository.findByRoleName("admin")).thenReturn(Optional.empty());

        // Act
        Role result = roleService.findRoleByName("admin");

        // Assert
        assertNull(result);
        verify(roleRepository).findByRoleName("admin");
    }

    // ========== EDGE CASES AND ERROR HANDLING TESTS ==========

    @Test
    void testHandleCreateRole_whenRepositoryThrowsException_shouldPropagateException() {
        // Arrange
        Role newRole = new Role();
        newRole.setRoleName("TEST");

        when(roleRepository.save(any(Role.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> roleService.handleCreateRole(newRole));
        assertEquals("Database error", exception.getMessage());
        verify(roleRepository).save(newRole);
    }

    @Test
    void testHandleUpdateRole_whenRepositoryThrowsExceptionOnFind_shouldPropagateException() {
        // Arrange
        Role updateRole = new Role();
        updateRole.setRoleId(1L);

        when(roleRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> roleService.handleUpdateRole(updateRole));
        assertEquals("Database error", exception.getMessage());
        verify(roleRepository).findById(1L);
        verify(roleRepository, never()).save(any());
    }

    @Test
    void testFetchAllRoles_whenRepositoryThrowsException_shouldPropagateException() {
        // Arrange
        when(roleRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> roleService.fetchAllRoles());
        assertEquals("Database error", exception.getMessage());
        verify(roleRepository).findAll();
    }

    @Test
    void testFindRoleById_whenRepositoryThrowsException_shouldPropagateException() {
        // Arrange
        when(roleRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> roleService.findRoleById(1L));
        assertEquals("Database error", exception.getMessage());
        verify(roleRepository).findById(1L);
    }

    @Test
    void testFindRoleByName_whenRepositoryThrowsException_shouldPropagateException() {
        // Arrange
        when(roleRepository.findByRoleName("ADMIN")).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> roleService.findRoleByName("ADMIN"));
        assertEquals("Database error", exception.getMessage());
        verify(roleRepository).findByRoleName("ADMIN");
    }
}
