package br.com.ottimizza.application.repositories.users;

import br.com.ottimizza.application.domain.dtos.UserDTO;
import br.com.ottimizza.application.model.user.User;
import br.com.ottimizza.application.model.user.QUser;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.jpa.impl.JPAQuery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class UsersRepositoryImpl implements UsersRepositoryCustom {

    @PersistenceContext
    EntityManager em;

    private QUser user = QUser.user;

    @Override
    public Page<User> fetchAll(UserDTO filter, Pageable pageable) {

        Integer pageIndex = pageable.getPageNumber();
        Integer pageSize = pageable.getPageSize();

        long totalElements = 0;

        JPAQuery<User> query = new JPAQuery<User>(em).from(user);

        // Email
        if (filter.getEmail() != null && !filter.getEmail().isEmpty()) {
            query.where(user.email.like("%" + filter.getEmail() + "%"));
        }

        // Type
        if (filter.getType() != null && filter.getType() > 0) {
            query.where(user.type.eq(filter.getType()));
        }

        // totalPages = (totalElements + pageSize - 1) / pageSize;

        totalElements = query.fetchCount();

        // hasNext = (pageIndex + 1) * pageSize < totalElements;

        if (pageSize != null && pageSize > 0) {
            query.limit(pageSize);
            if (pageIndex != null && pageIndex > 0) {
                query.offset(pageSize * pageIndex);
            }
        }

        return new PageImpl<User>(query.fetch(), pageable, totalElements);
    }

    @Override
    public Page<User> fetchAll(UserDTO filter, Pageable pageable, User authorizedUser) {
        long totalElements = 0;
        JPAQuery<User> query = new JPAQuery<User>(em).from(user);
        // Email
        if (filter.getEmail() != null && !filter.getEmail().isEmpty()) {
            query.where(user.email.like("%" + filter.getEmail() + "%"));
        }
        // Type
        if (filter.getType() != null && filter.getType() > 0) {
            query.where(user.type.eq(filter.getType()));
        }
        totalElements = query.fetchCount();
        query.limit(pageable.getPageSize());
        query.offset(pageable.getPageSize() * pageable.getPageNumber());
        return new PageImpl<User>(query.fetch(), pageable, totalElements);
    }

}