package br.com.ottimizza.application.repositories.users;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;

import br.com.ottimizza.application.domain.dtos.UserDTO;
import br.com.ottimizza.application.domain.dtos.UserShortDTO;
import br.com.ottimizza.application.model.Authority;
import br.com.ottimizza.application.model.user.QUser;
import br.com.ottimizza.application.model.user.QUserAuthorities;
import br.com.ottimizza.application.model.user.User;
import br.com.ottimizza.application.model.user_organization.QUserOrganization;
import br.com.ottimizza.application.repositories.UserProductsRepository;
import br.com.ottimizza.application.utils.QueryDSLUtils;

@Repository
public class UsersRepositoryImpl implements UsersRepositoryCustom {

    @PersistenceContext
    EntityManager em;
    
    @Inject
    private UserProductsRepository usersRepository;

    private static final String QUSER_NAME = "user";

    private QUser user = QUser.user;

    private QUserOrganization userOrganization = QUserOrganization.userOrganization;
    
    private QUserAuthorities userAuthorities = QUserAuthorities.userAuthorities;

    @Override
    public Page<User> fetchAll(UserDTO filter, Pageable pageable) {
        long totalElements = 0;
        JPAQuery<User> query = new JPAQuery<User>(em).from(user);

        totalElements = filter(query, filter);  
        sort(query, pageable, User.class, QUSER_NAME);
        paginate(query, pageable);
        
        return new PageImpl<User>(query.fetch(), pageable, totalElements);
    }

    @Override
    public Page<User> fetchAll(UserDTO filter, Pageable pageable, User authorizedUser) {
        long totalElements = 0;
        JPAQuery<User> query = new JPAQuery<User>(em).from(user);
        
        totalElements = filter(query, filter);  
        sort(query, pageable, User.class, QUSER_NAME);
        paginate(query, pageable);
        
        return new PageImpl<User>(query.fetch(), pageable, totalElements);
    }

    @Override
    public Page<User> fetchAllCustomers(UserDTO filter, Pageable pageable, User authorizedUser) {
        long totalElements = 0;
        JPAQuery<User> query = new JPAQuery<User>(em).from(user);

        // query.where(user.organization.eq(authorizedUser.getOrganization()));
        query.where(user.id.in(JPAExpressions.select(userOrganization.user.id)
                                  .from(userOrganization)
                                  .where(userOrganization.organization.id.in(
                                      JPAExpressions.select(userOrganization.organization.id)
                                                    .from(userOrganization)
                                                    .where(userOrganization.user.id.eq(authorizedUser.getId()))
                                  ))));

        totalElements = filter(query, filter);  
        sort(query, pageable, User.class, QUSER_NAME);
        paginate(query, pageable);
        
        return new PageImpl<User>(query.fetch(), pageable, totalElements);
    }
    
    @Override
	public Page<UserShortDTO> fetchUserShort(UserDTO filter, Pageable pageable, BigInteger organizationId) {
		long totalElements = 0;
        JPAQuery<UserShortDTO> query = new JPAQuery<UserShortDTO>(em).from(user);
        List<UserShortDTO> list = new ArrayList<UserShortDTO>();
        if(filter.getAuthority() == null || filter.getAuthority().equalsIgnoreCase("NENHUM")) {
        	if (filter.getActive() != null)    
        		query.where(user.active.eq(filter.getActive()));
        	query.where(user.organization.id.eq(organizationId));
        	totalElements = filter(query, filter);
        	sort(query, pageable, UserShortDTO.class, QUSER_NAME);
        	paginate(query, pageable);
        	
        	query.select(Projections.constructor(UserShortDTO.class, user.id, user.firstName, user.lastName, user.email, user.avatar));
        	list = query.fetch();
        	List<UserShortDTO> listNoAuthority = new ArrayList<UserShortDTO>();
        	for(UserShortDTO user : list) {
        		try {
    				List<Authority>     authorities = usersRepository.fetchAuthoritiesByUserId(user.getId());
    				List<BigInteger>    products    = usersRepository.fetchProductsByUserId(user.getId());
    				user.setAuthorities(authorities);
    				user.setProducts(products);
    				if(authorities.isEmpty() && filter.getAuthority().equalsIgnoreCase("NENHUM")) {
    					listNoAuthority.add(user);
    				}
    			}
    			catch(Exception ex) {
    				ex.getMessage();
    			}
    		}
        	if(filter.getAuthority() == null)
        		return new PageImpl<UserShortDTO>(list, pageable, totalElements);
        		
        	else
        		return new PageImpl<UserShortDTO>(listNoAuthority, pageable, totalElements);
        }
        else {
        	query.innerJoin(userAuthorities).on(userAuthorities.id.authoritiesId.like(filter.getAuthority())
        			.and(userAuthorities.usersId.id.eq(user.id)));
        	if (filter.getActive() != null)    
        		query.where(user.active.eq(filter.getActive()));
        	query.where(user.organization.id.eq(organizationId));
        	totalElements = filter(query, filter);  
        	sort(query, pageable, UserShortDTO.class, QUSER_NAME);
        	paginate(query, pageable);
        	
        	query.select(Projections.constructor(UserShortDTO.class, user.id, user.firstName, user.lastName, user.email, user.avatar));
        	list = query.fetch();
        	for(UserShortDTO user : list) {
        		try {
    				List<Authority>     authorities = usersRepository.fetchAuthoritiesByUserId(user.getId());
    				List<BigInteger>    products    = usersRepository.fetchProductsByUserId(user.getId());
    				user.setAuthorities(authorities);
    				user.setProducts(products);
    			}
    			catch(Exception ex) {
    				ex.getMessage();
    			}
    		}
        	return new PageImpl<UserShortDTO>(list, pageable, totalElements);
        }
	}
    
    @Override
    public List<BigInteger> fetchIds(UserDTO filter, BigInteger organizationId) {
    	JPAQuery<BigInteger> query = new JPAQuery<BigInteger>(em).from(user);
    	List<BigInteger> result = new ArrayList<BigInteger>();
    	List<BigInteger> listNoAuthority = new ArrayList<BigInteger>();
    	
    	if(filter.getAuthority() == null || filter.getAuthority().equalsIgnoreCase("NENHUM")) {
    		if (filter.getUsername() != null)  query.where(QueryDSLUtils.unnacent(user.username, "%" + filter.getUsername() + "%"));
    		if (filter.getFirstName() != null) query.where(QueryDSLUtils.unnacent(user.firstName, "%" + filter.getFirstName() + "%"));
    		if (filter.getLastName() != null)  query.where(QueryDSLUtils.unnacent(user.lastName, "%" + filter.getLastName() + "%"));
    		if (filter.getEmail() != null)     query.where(QueryDSLUtils.unnacent(user.email, "%" + filter.getEmail() + "%"));
    		if (filter.getType() != null)	   query.where(user.type.eq(filter.getType()));
    		if (filter.getActive() != null)    query.where(user.active.eq(filter.getActive()));
    		query.where(user.organization.id.eq(organizationId));
    		query.select(user.id);
    		result = query.fetch();
    		for(BigInteger userId : result) {
    			try {
    				List<Authority> authorities = usersRepository.fetchAuthoritiesByUserId(userId);
    				if(authorities.isEmpty() && filter.getAuthority().equalsIgnoreCase("NENHUM")) {
    					listNoAuthority.add(userId);
    				}
    			}
    			catch(Exception ex) {
    				ex.getMessage();
    			}
    		}
    		if(filter.getAuthority() == null)
        		return result;
        		
        	else
        		return listNoAuthority;
    	}
    	else {
    		query.innerJoin(userAuthorities).on(userAuthorities.id.authoritiesId.like(filter.getAuthority())
        			.and(userAuthorities.usersId.id.eq(user.id)));
    		if (filter.getUsername() != null)  query.where(QueryDSLUtils.unnacent(user.username, "%" + filter.getUsername() + "%"));
    		if (filter.getFirstName() != null) query.where(QueryDSLUtils.unnacent(user.firstName, "%" + filter.getFirstName() + "%"));
    		if (filter.getLastName() != null)  query.where(QueryDSLUtils.unnacent(user.lastName, "%" + filter.getLastName() + "%"));
    		if (filter.getEmail() != null)     query.where(QueryDSLUtils.unnacent(user.email, "%" + filter.getEmail() + "%"));
    		if (filter.getType() != null)	   query.where(user.type.eq(filter.getType()));
    		if (filter.getActive() != null)    query.where(user.active.eq(filter.getActive()));
    		query.where(user.organization.id.eq(organizationId));
    		query.select(user.id);
    		result = query.fetch();
    		
    		return result;
    	}
    }
    
    private <T> long filter(JPAQuery<T> query, UserDTO filter) {
        if (filter != null){
            if (filter.getId() != null) {
                query.where(user.id.eq(filter.getId()));
            }
            if (filter.getFirstName() != null && !filter.getFirstName().isEmpty()) {
                query.where(QueryDSLUtils.unnacent(user.firstName, "%" + filter.getFirstName() + "%"));
            }
            if (filter.getLastName() != null && !filter.getLastName().isEmpty()) {
                query.where(QueryDSLUtils.unnacent(user.lastName, "%" + filter.getLastName() + "%"));
            }
            if (filter.getEmail() != null && !filter.getEmail().isEmpty()) {
                query.where(QueryDSLUtils.unnacent(user.email, "%" + filter.getEmail() + "%"));
            }
            if (filter.getUsername() != null && !filter.getUsername().isEmpty()) {
                query.where(QueryDSLUtils.unnacent(user.username, "%" + filter.getUsername() + "%"));
            }
            if (filter.getActive() != null) {
                query.where(user.active.eq(filter.getActive()));
            }
            if (filter.getType() != null) {
                query.where(user.type.eq(filter.getType()));
            }
            if (filter.getTypes() != null && filter.getTypes().size() > 0) {
                query.where(user.type.in(filter.getTypes()));
            }
            if (filter.getOrganizationId() != null) {
                query.where(user.organization.id.eq(filter.getOrganizationId()));
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
