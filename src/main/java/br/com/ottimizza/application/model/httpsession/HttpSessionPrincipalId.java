package br.com.ottimizza.application.model.httpsession;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class HttpSessionPrincipalId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "session_id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "principal_name")
    private String principal;

}