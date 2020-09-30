package br.com.ottimizza.application.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import br.com.ottimizza.application.domain.OrganizationTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "organizations") // @formatter:off
public class Organization implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Getter @Setter
    @Column(name = "id", nullable = false)
    @SequenceGenerator(name = "organizations_sequence", sequenceName = "organizations_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organizations_sequence")
    private BigInteger id;

    @Getter @Setter
    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Getter @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Getter @Setter
    @Column(name = "type", nullable = true)
    private Integer type = OrganizationTypes.CLIENT.getValue();

    @Getter @Setter
    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean active;

    @Getter @Setter
    @Column(name = "cnpj", nullable = false)
    private String cnpj;

    @Getter @Setter
    @Column(name = "codigo_erp", nullable = true)
    private String codigoERP;

    @Getter @Setter
    @Column(name = "email", nullable = true)
    private String email;

    @Getter @Setter
    @Column(name = "avatar")
    private String avatar;

    @Getter @Setter
    @ManyToOne
    @JoinColumn(name = "fk_organizations_id")
    private Organization organization;
    
    @Getter @Setter
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Getter @Setter
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Getter @Setter
    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    public void prePersist() {
        if (this.active == null) {
            this.active = true;
        }
        this.setCreatedAt(LocalDateTime.now());
        this.setExternalId(UUID.randomUUID().toString());
        this.setCnpj(cnpj.replaceAll("\\D", ""));
    }

    @PreUpdate
    public void preUpdate() {
        if (this.active == null) {
            this.active = true;
        }
        this.setUpdatedAt(LocalDateTime.now());
    }

    public static class Type {
        public static final Integer ACCOUNTING = 1;
        public static final Integer CLIENT = 2;
    }


    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;
        Organization organization = (Organization) o;
        // field comparison
        return organization.getId().equals(id);
    }

}
