package br.com.ottimizza.application.repositories;


import java.math.BigInteger;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.ottimizza.application.domain.dtos.ProductShortDTO;
import br.com.ottimizza.application.model.Authority;
import br.com.ottimizza.application.model.user.UserProducts;
import br.com.ottimizza.application.model.user.UserProductsId;

@Repository
public interface UserProductsRepository extends JpaRepository<UserProducts, UserProductsId>{

	@Query(value = "SELECT a.name FROM users_authorities ua INNER JOIN authorities a ON (a.name = ua.fk_authorities_id) WHERE ua.fk_users_id = :id", nativeQuery = true)
    List<Authority> fetchAuthoritiesByUserId(@Param("id") BigInteger id);
    
    @Query(value = "SELECT p.id  FROM products p INNER JOIN users_products up ON (up.fk_products_id = p.id) WHERE up.fk_users_id = :id", nativeQuery = true)
    List<BigInteger> fetchProductsByUserId(@Param("id") BigInteger id);
    
    @Query(value = "SELECT new br.com.ottimizza.application.domain.dtos.ProductShortDTO(p.id , p.name) FROM Product p WHERE p.group = :group")
    List<ProductShortDTO> fetchAllProducts(@Param("group")String group);
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO users_products (fk_users_id , fk_products_id) values (:userId , :productId)", nativeQuery = true)
    void saveUserProducts(@Param("userId") BigInteger userId, @Param("productId") BigInteger productId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users_products ua WHERE ua.fk_users_id = :userId AND ua.fk_products_id = :productId", nativeQuery = true)
    void deleteUserProducts(@Param("userId") BigInteger userId, @Param("productId") BigInteger productId);
	
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO users_authorities (fk_users_id , fk_authorities_id) values (:userId , :authorityId)", nativeQuery = true)
    void saveUserAuhtorities(@Param("userId") BigInteger userId, @Param("authorityId") String authorityId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users_authorities ua WHERE ua.fk_users_id = :userId AND ua.fk_authorities_id = :authorityId", nativeQuery = true)
    void deleteUserAuhtorities(@Param("userId") BigInteger userId, @Param("authorityId") String authorityId);
    

    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO users_products (fk_users_id , fk_products_id) SELECT :userId, id FROM products WHERE id IN (5,8,10)", nativeQuery = true)
    void saveUserProductsTareffa(@Param("userId") BigInteger userId);
    
}
