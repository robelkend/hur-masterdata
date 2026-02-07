package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.TypeSanctionDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeSanction;
import com.rsoft.hurmanagement.hurmasterdata.service.TypeSanctionService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TypeSanctionController.
 * Note: Full integration tests with MockMvc would require spring-boot-starter-test dependency.
 */
@ExtendWith(MockitoExtension.class)
class TypeSanctionControllerTest {

    @Mock
    private TypeSanctionService service;

    @InjectMocks
    private TypeSanctionController controller;

    private TypeSanctionDTO testDTO;

    @BeforeEach
    void setUp() {
        testDTO = new TypeSanctionDTO();
        testDTO.setId(1L);
        testDTO.setCodeSanction("TEST001");
        testDTO.setDescription("Test Description");
        testDTO.setGravite(TypeSanction.Gravite.GRAVE);
        testDTO.setCategorie(TypeSanction.Categorie.SANCTION);
        testDTO.setCreatedBy("testuser");
        testDTO.setCreatedOn(OffsetDateTime.now());
        testDTO.setRowscn(1);
    }

    @Test
    void testFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TypeSanctionDTO> page = new PageImpl<>(Arrays.asList(testDTO), pageable, 1);
        
        when(service.findAll(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<TypeSanctionDTO>> response = controller.findAll(0, 10, "id", "ASC");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("TEST001", response.getBody().getContent().get(0).getCodeSanction());
        verify(service).findAll(any(Pageable.class));
    }

    @Test
    void testFindById() {
        when(service.findById(1L)).thenReturn(testDTO);

        ResponseEntity<TypeSanctionDTO> response = controller.findById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TEST001", response.getBody().getCodeSanction());
        verify(service).findById(1L);
    }
}
