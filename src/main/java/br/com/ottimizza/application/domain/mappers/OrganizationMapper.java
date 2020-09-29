package br.com.ottimizza.application.domain.mappers;

import br.com.ottimizza.application.domain.dtos.OrganizationDTO;
import br.com.ottimizza.application.model.Organization;

/**
 * @formatter:off
 * 
 * Classe responsável pelo mapeamento de Entity para DTO e vice-versa. 
 * Normalmente utilizada em Services para converter um DTO recebido pelo 
 * Controller em uma Entity, e para converter uma Entity em DTO novamente 
 * após todas as regras de negócio. Desta forma o cliente nunca tem acesso 
 * direto a camada de banco de dados.
 */
public class OrganizationMapper { // @formatter:off

    /**
     * Método responsável pela conversão de uma Entity para DTO.
     * 
     * @param Organization Entity que será convertida.
     * @return DTO (Data Transfer Object). 
     */
    public static OrganizationDTO fromEntity(Organization organization) { 
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
            .createdAt(organization.getCreatedAt())
            .updatedAt(organization.getUpdatedAt())
            .updatedBy(organization.getUpdatedBy())
            .organizationId(
                organization.getOrganization() != null && organization.getOrganization().getId() != null
                ? organization.getOrganization().getId()
                : null
            )
            .build();
    }

    /**
     * Método responsável pela conversão de um DTO para Entity.
     * 
     * @param OrganizationDTO Classe DTO que será convertida.
     * @param removeNonDigitsFromCNPJ booleano para remover formatação de CPF/CNPJ.
     * @return Entity (Classe que reflete a camada de negócio - banco de dados). 
     */
    public static Organization fromDTO(OrganizationDTO dto, boolean removeNonDigitsFromCNPJ) {
        return Organization.builder()
            .id(dto.getId())
            .externalId(dto.getExternalId())
            .name(dto.getName())
            .cnpj(
                removeNonDigitsFromCNPJ && dto.getCnpj() != null
                ? dto.getCnpj().replaceAll("\\D", "")
                : dto.getCnpj()
            )
            .type(dto.getType())
            .active(dto.getActive())
            .codigoERP(dto.getCodigoERP())
            .email(dto.getEmail())
            .avatar(dto.getAvatar())
            .createdAt(dto.getCreatedAt())
            .updatedAt(dto.getUpdatedAt())
            .updatedBy(dto.getUpdatedBy())
            .organization(
                dto.getOrganizationId() != null
                ? Organization.builder().id(dto.getOrganizationId()).build()
                : null
            )
            .build();
    }

    public static Organization fromDTO(OrganizationDTO dto) {
        return fromDTO(dto, true);
    }

}
