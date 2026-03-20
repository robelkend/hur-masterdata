package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.BoniGenerationRequestDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.BoniGenerationEmployeRequestDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.BoniDeleteRequestDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.BoniValidationRequestDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollBoniDeductionDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollEmployeBoniDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollPeriodeBoni;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollPeriodeBoniRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RubriquePaieRepository;
import com.rsoft.hurmanagement.hurmasterdata.service.BoniGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/bonis")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class BoniController {

    private final BoniGenerationService boniGenerationService;
    private final PayrollPeriodeBoniRepository payrollPeriodeBoniRepository;
    private final RubriquePaieRepository rubriquePaieRepository;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(
            @Valid @RequestBody BoniGenerationRequestDTO request,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        PayrollPeriodeBoni periodeBoni = payrollPeriodeBoniRepository.findById(request.getPeriodeBoniId())
                .orElseThrow(() -> new RuntimeException("PayrollPeriodeBoni not found with id: " + request.getPeriodeBoniId()));
        rubriquePaieRepository.findById(request.getRubriquePaieId())
                .orElseThrow(() -> new RuntimeException("RubriquePaie not found with id: " + request.getRubriquePaieId()));
        Map<String, Object> result = boniGenerationService.generate(
                periodeBoni.getId(),
                request.getRubriquePaieId(),
                request.getEntrepriseId(),
                request.getRegimePaieId(),
                username
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate-for-employe")
    public ResponseEntity<Map<String, Object>> generateForEmploye(
            @Valid @RequestBody BoniGenerationEmployeRequestDTO request,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        PayrollPeriodeBoni periodeBoni = payrollPeriodeBoniRepository.findById(request.getPeriodeBoniId())
                .orElseThrow(() -> new RuntimeException("PayrollPeriodeBoni not found with id: " + request.getPeriodeBoniId()));
        rubriquePaieRepository.findById(request.getRubriquePaieId())
                .orElseThrow(() -> new RuntimeException("RubriquePaie not found with id: " + request.getRubriquePaieId()));
        Map<String, Object> result = boniGenerationService.generate(
                periodeBoni.getId(),
                request.getRubriquePaieId(),
                request.getEntrepriseId(),
                request.getRegimePaieId(),
                request.getEmployeId(),
                username
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(
            @Valid @RequestBody BoniValidationRequestDTO request,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        payrollPeriodeBoniRepository.findById(request.getPeriodeBoniId())
                .orElseThrow(() -> new RuntimeException("PayrollPeriodeBoni not found with id: " + request.getPeriodeBoniId()));
        rubriquePaieRepository.findById(request.getRubriquePaieId())
                .orElseThrow(() -> new RuntimeException("RubriquePaie not found with id: " + request.getRubriquePaieId()));
        Map<String, Object> result = boniGenerationService.validateCalculated(
                request.getPeriodeBoniId(),
                request.getRubriquePaieId(),
                request.getEntrepriseId(),
                request.getRegimePaieId(),
                username
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<PayrollEmployeBoniDTO>> findBonis(
            @RequestParam Long periodeBoniId,
            @RequestParam Long rubriquePaieId,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) Long regimePaieId,
            @RequestParam(required = false) Long employeId) {
        return ResponseEntity.ok(
                boniGenerationService.findBonis(periodeBoniId, rubriquePaieId, entrepriseId, regimePaieId, employeId)
        );
    }

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteCalculated(
            @Valid @RequestBody BoniDeleteRequestDTO request) {
        payrollPeriodeBoniRepository.findById(request.getPeriodeBoniId())
                .orElseThrow(() -> new RuntimeException("PayrollPeriodeBoni not found with id: " + request.getPeriodeBoniId()));
        rubriquePaieRepository.findById(request.getRubriquePaieId())
                .orElseThrow(() -> new RuntimeException("RubriquePaie not found with id: " + request.getRubriquePaieId()));
        return ResponseEntity.ok(
                boniGenerationService.deleteCalculated(
                        request.getPeriodeBoniId(),
                        request.getRubriquePaieId(),
                        request.getEntrepriseId(),
                        request.getRegimePaieId(),
                        request.getEmployeId()
                )
        );
    }

    @GetMapping("/{payrollBoniId}/deductions")
    public ResponseEntity<List<PayrollBoniDeductionDTO>> findBoniDeductions(@PathVariable Long payrollBoniId) {
        return ResponseEntity.ok(boniGenerationService.findBoniDeductions(payrollBoniId));
    }
}
