package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.RessourceUiDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.RessourceUiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ressources-ui")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class RessourceUiController {
    private final RessourceUiService service;

    @GetMapping("/all-for-dropdown")
    public ResponseEntity<List<RessourceUiDTO>> findAllForDropdown() {
        return ResponseEntity.ok(service.findAllForDropdown());
    }
}
