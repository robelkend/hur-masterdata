package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.TypeSanctionCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeSanctionDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeSanctionUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeSanction;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeSanctionRepository;
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

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TypeSanctionServiceTest {

    @Mock
    private TypeSanctionRepository repository;

    @InjectMocks
    private TypeSanctionService service;

    private TypeSanction testEntity;
    private TypeSanctionCreateDTO createDTO;
    private TypeSanctionUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        testEntity = new TypeSanction();
        testEntity.setId(1L);
        testEntity.setCodeSanction("TEST001");
        testEntity.setDescription("Test Description");
        testEntity.setGravite(TypeSanction.Gravite.GRAVE);
        testEntity.setCategorie(TypeSanction.Categorie.SANCTION);
        testEntity.setCreatedBy("testuser");
        testEntity.setCreatedOn(OffsetDateTime.now());
        testEntity.setRowscn(1);

        createDTO = new TypeSanctionCreateDTO();
        createDTO.setCodeSanction("TEST001");
        createDTO.setDescription("Test Description");
        createDTO.setGravite(TypeSanction.Gravite.GRAVE);
        createDTO.setCategorie(TypeSanction.Categorie.SANCTION);

        updateDTO = new TypeSanctionUpdateDTO();
        updateDTO.setRowscn(1);
        updateDTO.setDescription("Updated Description");
        updateDTO.setGravite(TypeSanction.Gravite.MOYEN);
        updateDTO.setCategorie(TypeSanction.Categorie.BLAME);
    }

    @Test
    void testFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TypeSanction> page = new PageImpl<>(Arrays.asList(testEntity), pageable, 1);
        
        when(repository.findAll(pageable)).thenReturn(page);

        Page<TypeSanctionDTO> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(repository).findAll(pageable);
    }

    @Test
    void testFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(testEntity));

        TypeSanctionDTO result = service.findById(1L);

        assertNotNull(result);
        assertEquals("TEST001", result.getCodeSanction());
        verify(repository).findById(1L);
    }

    @Test
    void testFindByIdNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.findById(999L));
    }

    @Test
    void testCreate() {
        when(repository.existsByCodeSanction("TEST001")).thenReturn(false);
        when(repository.save(any(TypeSanction.class))).thenReturn(testEntity);

        TypeSanctionDTO result = service.create(createDTO, "testuser");

        assertNotNull(result);
        assertEquals("TEST001", result.getCodeSanction());
        verify(repository).existsByCodeSanction("TEST001");
        verify(repository).save(any(TypeSanction.class));
    }

    @Test
    void testCreateDuplicateCode() {
        when(repository.existsByCodeSanction("TEST001")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.create(createDTO, "testuser"));
        verify(repository, never()).save(any(TypeSanction.class));
    }

    @Test
    void testUpdate() {
        when(repository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(repository.save(any(TypeSanction.class))).thenReturn(testEntity);

        TypeSanctionDTO result = service.update(1L, updateDTO, "testuser");

        assertNotNull(result);
        verify(repository).findById(1L);
        verify(repository).save(any(TypeSanction.class));
    }

    @Test
    void testUpdateConcurrencyConflict() {
        testEntity.setRowscn(2); // Different rowscn
        when(repository.findById(1L)).thenReturn(Optional.of(testEntity));

        assertThrows(RuntimeException.class, () -> service.update(1L, updateDTO, "testuser"));
        verify(repository, never()).save(any(TypeSanction.class));
    }

    @Test
    void testDelete() {
        when(repository.findById(1L)).thenReturn(Optional.of(testEntity));
        doNothing().when(repository).delete(testEntity);

        service.delete(1L, 1);

        verify(repository).findById(1L);
        verify(repository).delete(testEntity);
    }

    @Test
    void testDeleteConcurrencyConflict() {
        testEntity.setRowscn(2); // Different rowscn
        when(repository.findById(1L)).thenReturn(Optional.of(testEntity));

        assertThrows(RuntimeException.class, () -> service.delete(1L, 1));
        verify(repository, never()).delete(any(TypeSanction.class));
    }
}
