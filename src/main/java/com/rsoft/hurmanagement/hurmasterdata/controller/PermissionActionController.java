package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.PermissionActionDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.PermissionActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permission-actions")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PermissionActionController {
    private final PermissionActionService service;

    @GetMapping("/all-for-dropdown")
    public ResponseEntity<List<PermissionActionDTO>> findAllForDropdown() {
        return ResponseEntity.ok(service.findAllForDropdown());
    }
}
