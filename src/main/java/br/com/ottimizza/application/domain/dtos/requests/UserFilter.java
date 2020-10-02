package br.com.ottimizza.application.domain.dtos.requests;

import java.io.Serializable;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// @formatter:off
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFilter implements Serializable {

    static final long serialVersionUID = 1L;

    private BigInteger id;
    
    private String username;

    private String email;

    private String phone;

    private String firstName;

    private String lastName;

    private Integer type;

    private Boolean active;

    private Boolean activated;

    private BigInteger organizationId;

}
