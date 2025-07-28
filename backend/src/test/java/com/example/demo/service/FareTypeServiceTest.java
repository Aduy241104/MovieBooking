package com.example.demo.service;

import com.example.demo.DTO.request.FareTypeRequest;
import com.example.demo.DTO.response.FareTypeResponse;
import com.example.demo.DTO.response.ResPagination;
import com.example.demo.model.FareType;
import com.example.demo.repository.FareTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FareTypeServiceTest {

    @Mock
    private FareTypeRepository fareTypeRepository;

    @InjectMocks
    private FareTypeService fareTypeService;

    private FareType fareType;
    private FareTypeRequest fareTypeRequest;
    private Pageable pageable;
    private Specification<FareType> spec;

    @BeforeEach
    void setUp() {
        fareType = new FareType();
        fareType.setId(1L);
        fareType.setName("Adult");
        fareType.setBasePrice(new BigDecimal("100000"));
        fareType.setDayPrice(new BigDecimal("120000"));
        fareType.setTimeSlotType("Evening");
        fareType.setMovieFormat("2D");
        fareType.setIsDeleted(false);

        fareTypeRequest = new FareTypeRequest();
        fareTypeRequest.setName("Adult");
        fareTypeRequest.setBasePrice(new BigDecimal("100000"));
        fareTypeRequest.setDayPrice(new BigDecimal("120000"));
        fareTypeRequest.setTimeSlotType("Evening");
        fareTypeRequest.setMovieFormat("2D");

        pageable = PageRequest.of(0, 10);
        spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), "Adult");
    }

    @Test
    void handleCreateFareType_success() {
        // Arrange
        when(fareTypeRepository.save(any(FareType.class))).thenReturn(fareType);

        // Act
        FareType result = fareTypeService.handleCreateFareType(fareTypeRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Adult", result.getName());
        assertEquals(new BigDecimal("100000"), result.getBasePrice());
        assertFalse(result.getIsDeleted());
        verify(fareTypeRepository, times(1)).save(any(FareType.class));
    }

    @Test
    void handleUpdateFareType_success() {
        // Arrange
        when(fareTypeRepository.findById(1L)).thenReturn(Optional.of(fareType));
        when(fareTypeRepository.save(any(FareType.class))).thenReturn(fareType);

        // Act
        FareType result = fareTypeService.handleUpdateFareType(fareTypeRequest, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Adult", result.getName());
        assertEquals(new BigDecimal("100000"), result.getBasePrice());
        verify(fareTypeRepository, times(1)).findById(1L);
        verify(fareTypeRepository, times(1)).save(any(FareType.class));
    }

    @Test
    void handleUpdateFareType_notFound() {
        // Arrange
        when(fareTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fareTypeService.handleUpdateFareType(fareTypeRequest, 1L);
        });
        assertEquals("Không tìm thấy loại giá", exception.getMessage());
        verify(fareTypeRepository, times(1)).findById(1L);
        verify(fareTypeRepository, never()).save(any(FareType.class));
    }

    @Test
    void handleDeleteFareType_success() {
        // Arrange
        when(fareTypeRepository.findById(1L)).thenReturn(Optional.of(fareType));
        when(fareTypeRepository.save(any(FareType.class))).thenReturn(fareType);

        // Act
        FareType result = fareTypeService.handleDeleteFareType(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsDeleted());
        verify(fareTypeRepository, times(1)).findById(1L);
        verify(fareTypeRepository, times(1)).save(any(FareType.class));
    }

    @Test
    void handleDeleteFareType_notFound() {
        // Arrange
        when(fareTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fareTypeService.handleDeleteFareType(1L);
        });
        assertEquals("Không tìm thấy loại giá với ID: 1", exception.getMessage());
        verify(fareTypeRepository, times(1)).findById(1L);
        verify(fareTypeRepository, never()).save(any(FareType.class));
    }

    @Test
    void handleDeleteFareType_alreadyDeleted() {
        // Arrange
        fareType.setIsDeleted(true);
        when(fareTypeRepository.findById(1L)).thenReturn(Optional.of(fareType));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fareTypeService.handleDeleteFareType(1L);
        });
        assertEquals("Loại giá này đã bị xóa", exception.getMessage());
        verify(fareTypeRepository, times(1)).findById(1L);
        verify(fareTypeRepository, never()).save(any(FareType.class));
    }

    @Test
    void fetchAllFareTypes_success() {
        // Arrange
        List<FareType> fareTypes = Arrays.asList(fareType);
        Page<FareType> page = new PageImpl<>(fareTypes, pageable, fareTypes.size());
        when(fareTypeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        ResPagination result = fareTypeService.fetchAllFareTypes(spec, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getMeta().getPage());
        assertEquals(10, result.getMeta().getPageSize());
        assertEquals(1, result.getMeta().getPages());
        assertEquals(1, result.getMeta().getTotal());
        assertEquals(1, ((List<?>) result.getData()).size()); // Ép kiểu thành List
        assertEquals("Adult", ((FareTypeResponse) ((List<?>) result.getData()).get(0)).getName());
        verify(fareTypeRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }  

    @Test
    void fetchFareTypeById_success() {
        // Arrange
        when(fareTypeRepository.findById(1L)).thenReturn(Optional.of(fareType));

        // Act
        FareType result = fareTypeService.fetchFareTypeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Adult", result.getName());
        verify(fareTypeRepository, times(1)).findById(1L);
    }

    @Test
    void fetchFareTypeById_notFound() {
        // Arrange
        when(fareTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        FareType result = fareTypeService.fetchFareTypeById(1L);

        // Assert
        assertNull(result);
        verify(fareTypeRepository, times(1)).findById(1L);
    }

    @Test
    void fetchFareTypeByName_success() {
        // Arrange
        when(fareTypeRepository.findByNameAndIsDeletedFalse("Adult")).thenReturn(fareType);

        // Act
        FareType result = fareTypeService.fetchFareTypeByName("Adult");

        // Assert
        assertNotNull(result);
        assertEquals("Adult", result.getName());
        verify(fareTypeRepository, times(1)).findByNameAndIsDeletedFalse("Adult");
    }

    @Test
    void existsByName_success() {
        // Arrange
        when(fareTypeRepository.existsByName("Adult")).thenReturn(true);

        // Act
        boolean result = fareTypeService.existsByName("Adult");

        // Assert
        assertTrue(result);
        verify(fareTypeRepository, times(1)).existsByName("Adult");
    }

    @Test
    void fetchFareTypeByIsDeletedFalse_success() {
        // Arrange
        List<FareType> fareTypes = Arrays.asList(fareType);
        when(fareTypeRepository.findByIsDeletedFalse()).thenReturn(fareTypes);

        // Act
        List<FareType> result = fareTypeService.fetchFareTypeByIsDeletedFalse();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Adult", result.get(0).getName());
        verify(fareTypeRepository, times(1)).findByIsDeletedFalse();
    }
}