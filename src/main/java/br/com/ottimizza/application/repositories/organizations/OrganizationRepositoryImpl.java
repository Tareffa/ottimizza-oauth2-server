package br.com.ottimizza.application.repositories.organizations;

import br.com.ottimizza.application.domain.dtos.OrganizationDTO;
import br.com.ottimizza.application.model.Organization;
import br.com.ottimizza.application.model.QOrganization;
import br.com.ottimizza.application.model.user.User;
import br.com.ottimizza.application.model.user_organization.QUserOrganization;
import br.com.ottimizza.application.utils.QueryDSLUtils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.jpa.impl.JPAQuery;
// Sort
import com.querydsl.core.types.Order;
import org.springframework.data.domain.Sort;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;

@Repository // @formatter:off
public class OrganizationRepositoryImpl implements OrganizationRepositoryCustom {

    private final String QORGANIZATION_NAME = "organization1";

    private long totalElements;

    private QOrganization organization = QOrganization.organization1;

    private QUserOrganization userOrganization = QUserOrganization.userOrganization;

    @PersistenceContext
    EntityManager em;

    @Override
    public Page<Organization> fetchAllByAccountantId(OrganizationDTO filter, Pageable pageable, User authorizedUser) {
        JPAQuery<Organization> query = new JPAQuery<Organization>(em).from(organization);
        if(filter.getType() == 2) {
            filter.setOrganizationId(authorizedUser.getOrganization().getId());
        }
        totalElements = filter(query, filter);
        sort(query, pageable, Organization.class, QORGANIZATION_NAME);
        paginate(query, pageable);
        return new PageImpl<Organization>(query.fetch(), pageable, totalElements);
    }

    @Override 
    public Page<Organization> fetchAllByCustomerId(BigInteger id, OrganizationDTO filter, Pageable pageable) {
        JPAQuery<Organization> query = new JPAQuery<Organization>(em).from(organization)
            .innerJoin(userOrganization)
                .on(userOrganization.organization.id.eq(organization.id)
                .and(userOrganization.user.id.eq(id)));
        totalElements = filter(query, filter);  
        sort(query, pageable, Organization.class, QORGANIZATION_NAME);
        paginate(query, pageable);
        return new PageImpl<Organization>(query.fetch(), pageable, totalElements);
    } 

    @Override 
    public Page<Organization> fetchAllByCustomerId(OrganizationDTO filter, Pageable pageable, User authorizedUser) {
        JPAQuery<Organization> query = new JPAQuery<Organization>(em).from(organization)
            .innerJoin(userOrganization)
                .on(userOrganization.organization.id.eq(organization.id)
                .and(userOrganization.user.id.eq(authorizedUser.getId())));
        totalElements = filter(query, filter);  
        sort(query, pageable, Organization.class, QORGANIZATION_NAME);
        paginate(query, pageable);
        return new PageImpl<Organization>(query.fetch(), pageable, totalElements);
    } 

    @Override
    public Page<Organization> fetchAll(OrganizationDTO filter, Pageable pageable, User authenticated) {
        JPAQuery<Organization> query = new JPAQuery<Organization>(em).from(organization)
            .leftJoin(organization.organization)
                .on(organization.organization.id.eq(organization.id));
        totalElements = filter(query, filter);  
        sort(query, pageable, Organization.class, QORGANIZATION_NAME);
        paginate(query, pageable);
        return new PageImpl<Organization>(query.fetch(), pageable, totalElements);
    }

    private <T> long filter(JPAQuery<T> query, OrganizationDTO filter) {
        if (filter != null){
            if (filter.getId() != null) {
                query.where(organization.id.eq(filter.getId()));
            }
            if (filter.getExternalId() != null && !filter.getExternalId().isEmpty()) {
                query.where(organization.externalId.like(filter.getExternalId()));
            }
            if (filter.getName() != null && !filter.getName().isEmpty()) {
                query.where(QueryDSLUtils.unnacent(organization.name, "%" + filter.getName() + "%"));
            }
            if (filter.getCnpj() != null && !filter.getCnpj().isEmpty()) {
                query.where(organization.cnpj.like(filter.getCnpj().replaceAll("\\D", "")));
            }
            if (filter.getActive() != null) {
                query.where(organization.active.eq(filter.getActive()));
            }
            if (filter.getCodigoERP() != null && !filter.getCodigoERP().isEmpty()) {
                query.where(organization.codigoERP.like(filter.getCodigoERP()));
            }
            if (filter.getType() != null) {
                query.where(organization.type.eq(filter.getType()));
            }
            if (filter.getTypes() != null && filter.getTypes().size() > 0) {
                query.where(organization.type.in(filter.getTypes()));
            }
            if (filter.getOrganizationId() != null) {
                query.where(organization.organization.id.eq(filter.getOrganizationId()));
            }
        }
        return query.fetchCount();
    }

    private <T> JPAQuery<T> paginate(JPAQuery<T> query, Pageable pageable) {
        query.limit(pageable.getPageSize());
        query.offset(pageable.getPageSize() * pageable.getPageNumber());
        return query;
    }

    private <T> JPAQuery<T> sort(JPAQuery<T> query, Pageable pageable, Class<T> clazz, String value) {
        PathBuilder<T> entityPath = new PathBuilder<T>(clazz, value);
        for (Sort.Order order : pageable.getSort()) {
            PathBuilder<Object> propertyPath = entityPath.get(order.getProperty());
            query.orderBy(new OrderSpecifier(Order.valueOf(order.getDirection().name()), propertyPath));
        }
        return query;
    }

}
