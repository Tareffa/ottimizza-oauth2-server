package br.com.ottimizza.application.model.httpsession;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "http_session_principals")
public class HttpSessionPrincipal implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private HttpSessionPrincipalId id;

    @Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "session_id", columnDefinition = "uuid", insertable = false, updatable = false)
    private UUID sessionId;

    @Column(name = "alias")
    private String alias;

    @Column(name = "principal_name", insertable = false, updatable = false)
    private String principal;

}
