package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prestation_depart")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestationDepart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regime_paie_id", nullable = false)
    private RegimePaie regimePaie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mutation_employe_id", nullable = false)
    private MutationEmploye mutationEmploye;

    @Column(name = "type_depart", nullable = false, length = 50)
    private String typeDepart;

    @Column(name = "date_depart", nullable = false)
    private LocalDate dateDepart;

    @Column(name = "date_calcul", nullable = false)
    private LocalDateTime dateCalcul;

    @Column(name = "total_gains", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalGains = BigDecimal.ZERO;

    @Column(name = "total_deductions", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "montant_net", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantNet = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    private StatutPrestationDepart statut = StatutPrestationDepart.CALCULE;

    @OneToMany(mappedBy = "prestationDepart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrestationDepartDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "prestationDepart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrestationDepartDeduction> deductions = new ArrayList<>();

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_on", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdOn;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_on", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedOn;

    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;
}
