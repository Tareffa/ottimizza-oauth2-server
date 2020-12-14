package br.com.ottimizza.application.model.httpsession;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "http_sessions")
public class HttpSessionDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "session_id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "alias")
    private String alias;

    @Column(name = "principal_name")
    private String principal;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

}
