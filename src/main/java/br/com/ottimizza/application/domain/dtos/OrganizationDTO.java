package br.com.ottimizza.application.domain.dtos;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import br.com.ottimizza.application.domain.dtos.criterias.SearchCriteria;
import br.com.ottimizza.application.model.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// @formatter:off
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDTO implements Serializable {

    static final long serialVersionUID = 1L;
    
    @Getter @Setter
    public BigInteger id;

    @Getter @Setter
    public String externalId;

    @Getter @Setter
    public String name;

    @Getter @Setter
    public Integer type;

    @Getter @Setter
    public Boolean active;

    @Getter @Setter
    public String cnpj;

    @Getter @Setter
    public String codigoERP;

    @Getter @Setter
    public String email;

    @Getter @Setter
    public String avatar;

    @Getter @Setter
    public BigInteger organizationId;
    
    @Getter @Setter
    private LocalDateTime createdAt;

    @Getter @Setter
    private LocalDateTime updatedAt;

    @Getter @Setter
    private String updatedBy;

    public Organization toEntity(boolean removeNonDigitsFromCNPJ) {
        return Organization.builder()
            .id(this.getId())
            .externalId(this.getExternalId())
            .name(this.getName())
            .cnpj(
                removeNonDigitsFromCNPJ && this.cnpj != null
                ? this.cnpj.replaceAll("\\D", "")
                : this.cnpj
            )
            .type(this.getType())
            .active(this.getActive())
            .codigoERP(this.getCodigoERP())
            .email(this.getEmail())
            .avatar(this.getAvatar())
            .organization(
                this.organizationId != null
                ? Organization.builder().id(this.organizationId).build()
                : null
            )
            .createdAt(this.getCreatedAt())
            .updatedAt(this.getUpdatedAt())
            .updatedBy(this.getUpdatedBy())
            .build();
    }
    public Organization toEntity() {
        return this.toEntity(true);
    }

    public static OrganizationDTO fromEntity(Organization organization) { // @formatter:off
        return OrganizationDTO.builder()
            .id(organization.getId())
            .externalId(organization.getExternalId())
            .name(organization.getName())
            .cnpj(organization.getCnpj())
            .type(organization.getType())
            .active(organization.getActive())
            .codigoERP(organization.getCodigoERP())
            .email(organization.getEmail())
            .avatar(organization.getAvatar())
            .organizationId(
                organization.getOrganization() != null && organization.getOrganization().getId() != null
                ? organization.getOrganization().getId()
                : null
            )
            .createdAt(organization.getCreatedAt())
            .updatedAt(organization.getUpdatedAt())
            .updatedBy(organization.getUpdatedBy())
            .build();
    }

    public static List<OrganizationDTO> fromEntities(List<Organization> organizations) {
        return organizations.stream().map(organization -> fromEntity(organization)).collect(Collectors.toList());
    }

    public Organization patch(Organization organization) {
        if (this.name != null && !this.name.equals(""))
            organization.setName(this.name);

        if (this.cnpj != null && !this.cnpj.equals(""))
            organization.setCnpj(this.cnpj);

        if (this.codigoERP != null && !this.codigoERP.equals(""))
            organization.setCodigoERP(this.codigoERP);

        if (this.avatar != null) {
            if (this.avatar.trim().equals("")) {
                organization.setAvatar(null);
            } else {
                organization.setAvatar(this.avatar.trim());
            }
        }
        if (this.active != null)
            organization.setActive(this.active);

        if (this.email != null && !this.email.equals(""))
            organization.setEmail(this.email);

        if(this.updatedBy != null && !this.updatedBy.equals(""))
            organization.setUpdatedBy(this.updatedBy);

        return organization;
    }

    public static Pageable getPageRequest(SearchCriteria searchCriteria) {
        return PageRequest.of(searchCriteria.getPageIndex(), searchCriteria.getPageSize(), getSort(searchCriteria));
    }

    public static Sort getSort(SearchCriteria searchCriteria) {
        Sort sort = Sort.unsorted();

        if (searchCriteria.getSort() != null && searchCriteria.getSort().getOrder() != null
                && searchCriteria.getSort().getAttribute() != null) {
            String order = searchCriteria.getSort().getOrder();
            String attribute = searchCriteria.getSort().getAttribute();
            return getSort(attribute, order);
        }

        return sort;
    }

    public static Sort getSort(String attribute, String order) {
        Sort sort = Sort.unsorted();
        sort = Sort.by(attribute);
        if (order.equals("asc")) {
            sort = sort.ascending();
        } else if (order.equals("desc")) {
            sort = sort.descending();
        }
        return sort;
    }

    OrganizationDTO withId(BigInteger id) {
        this.id = id;
        return this;
    }

    OrganizationDTO withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    OrganizationDTO withName(String name) {
        this.name = name;
        return this;
    }

    OrganizationDTO withType(Integer type) {
        this.type = type;
        return this;
    }

    OrganizationDTO withCnpj(String cnpj) {
        this.cnpj = cnpj;
        return this;
    }

    OrganizationDTO withCodigoERP(String codigoERP) {
        this.codigoERP = codigoERP;
        return this;
    }

    OrganizationDTO withEmail(String email) {
        this.email = email;
        return this;
    }

    OrganizationDTO withAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    OrganizationDTO withOrganizationId(BigInteger organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    OrganizationDTO withOrganizationId(Organization organization) {
        if (organization != null && organization.getId() != null) {
            this.organizationId = organization.getId();
        }
        return this;
    }

}
