package br.com.ottimizza.application.repositories.httpsession;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.ottimizza.application.model.httpsession.HttpSessionDetails;

@Repository
public interface HttpSessionRepository extends JpaRepository<HttpSessionDetails, UUID> {

}
