package br.com.ottimizza.application.domain.dtos;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import br.com.ottimizza.application.model.Organization;
import br.com.ottimizza.application.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// @formatter:off
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable {

    static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private BigInteger id;

    @Getter
    @Setter
    private String username;

    @Getter @Setter
    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;

    @Getter @Setter
    private String email;

    @Getter
    @Setter
    private String firstName;

    @Getter
    @Setter
    private String lastName;

    @Getter
    @Setter
    private Integer type;

    @Getter
    @Setter
    private String avatar;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigInteger organizationId;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private OrganizationDTO organization;

    public User toEntity() {
        User user = new User();

        Organization accounting = new Organization();
        accounting.setId(this.organizationId);

        user.setId(this.id);
        user.setUsername(this.username);
        user.setPassword(this.password);
        user.setEmail(this.email);
        user.setType(this.type);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        
        user.setAvatar(this.avatar);
        user.setOrganization(accounting);

        return user;
    }

    public static UserDTO fromEntity(User user) {
        // @formatter:off
        UserDTO dto = new UserDTO()
            .withId(user.getId())
            .withUsername(user.getUsername())
            .withFirstName(user.getFirstName())
            .withLastName(user.getLastName())
            .withEmail(user.getEmail())
            .withType(user.getType())
            .withAvatar(user.getAvatar())
            .withOrganizationId(user.getOrganization());
        // @formatter:on
        return dto;
    }

    public static UserDTO fromEntityWithOrganization(User user) {
        // @formatter:off
        UserDTO dto = new UserDTO()
            .withId(user.getId())
            .withUsername(user.getUsername())
            .withFirstName(user.getFirstName())
            .withLastName(user.getLastName())
            .withEmail(user.getEmail())
            .withType(user.getType())
            .withAvatar(user.getAvatar())
            .withOrganization(OrganizationDTO.fromEntity(user.getOrganization()));
        // @formatter:on
        return dto;
    }

    public static List<UserDTO> fromEntities(List<User> users) {
        return users.stream().map(user -> fromEntity(user)).collect(Collectors.toList());
    }

    public User patch(User user) {
        if (this.username != null && !this.username.equals(""))
            user.setUsername(this.username);

        if (this.email != null && !this.email.equals(""))
            user.setEmail(this.email);

        if (this.firstName != null && !this.firstName.equals(""))
            user.setFirstName(this.firstName);

        if (this.lastName != null && !this.lastName.equals(""))
            user.setLastName(this.lastName);

        return user;
    }

    UserDTO withId(BigInteger id) {
        this.id = id;
        return this;
    }

    UserDTO withUsername(String username) {
        this.username = username;
        return this;
    }

    UserDTO withEmail(String email) {
        this.email = email;
        return this;
    }

    UserDTO withType(Integer type) {
        this.type = type;
        return this;
    }

    UserDTO withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    UserDTO withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    UserDTO withAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    UserDTO withOrganization(OrganizationDTO organization) {
        this.organization = organization;
        return this;
    }

    UserDTO withOrganizationId(BigInteger organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    UserDTO withOrganizationId(Organization organization) {
        if (organization != null && organization.getId() != null) {
            this.organizationId = organization.getId();
        }
        return this;
    }

}
