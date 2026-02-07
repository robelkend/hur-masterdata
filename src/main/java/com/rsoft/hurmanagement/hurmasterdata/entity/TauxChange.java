package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "taux_change", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_taux_change_devise_date_institution",
        columnNames = {"code_devise", "date_taux", "code_institution"}
    )
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TauxChange {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "date_taux", nullable = false)
    private LocalDate dateTaux;
    
    @Column(name = "taux", nullable = false, precision = 18, scale = 6)
    private BigDecimal taux;
    
    @Column(name = "taux_payroll", nullable = false, precision = 18, scale = 6, columnDefinition = "NUMERIC(18,6) DEFAULT 0")
    private BigDecimal tauxPayroll;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "code_devise", referencedColumnName = "code_devise", nullable = false)
    private Devise devise;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "code_institution", referencedColumnName = "code_institution")
    private InstitutionTierse institution;
    
    // Audit fields
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    @Column(name = "created_on", nullable = false)
    private OffsetDateTime createdOn;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Column(name = "updated_on")
    private OffsetDateTime updatedOn;
    
    // Optimistic concurrency control
    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;
}
