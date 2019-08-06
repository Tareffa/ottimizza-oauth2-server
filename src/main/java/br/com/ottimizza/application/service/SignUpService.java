package br.com.ottimizza.application.service;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.ottimizza.application.domain.exceptions.OrganizationAlreadyRegisteredException;
import br.com.ottimizza.application.domain.exceptions.UserAlreadyRegisteredException;
import br.com.ottimizza.application.model.Authority;
import br.com.ottimizza.application.model.Organization;
import br.com.ottimizza.application.model.user.User;
import br.com.ottimizza.application.repositories.AuthorityRepository;
import br.com.ottimizza.application.repositories.PasswordRecoveryRepository;
import br.com.ottimizza.application.repositories.organizations.OrganizationRepository;
import br.com.ottimizza.application.repositories.users.UsersRepository;

@Service
public class SignUpService {

    @Inject
    UsersRepository userRepository;

    @Inject
    OrganizationRepository organizationRepository;

    @Inject
    AuthorityRepository authorityRepository;

    @Inject
    PasswordRecoveryRepository passwordRecoveryRepository;

    public User register(User user, Organization organization)
            throws OrganizationAlreadyRegisteredException, UserAlreadyRegisteredException, Exception {
        user.setUsername(user.getEmail());
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        organization.setExternalId(organization.getCnpj());

        // Checking if organization wont cause an loop
        if (organization.getOrganization() != null) {
            if (organization.getId().compareTo(organization.getOrganization().getId()) == 0) {
                System.out.println("A organization cannot be a parent of itself.");
                throw new Exception("A organization cannot be a parent of itself.");
            }
        }

        // Checking if email is already registered.
        if (userRepository.emailIsAlreadyRegistered(user.getEmail())) {
            System.out.println("A user with that email address is already registered.");
            throw new UserAlreadyRegisteredException("A user with that email address is already registered.");
        }

        // Checking if organization is already registered.
        BigInteger organizationId = organization.getOrganization() == null ? null
                : organization.getOrganization().getId();
        if (organizationRepository.cnpjIsAlreadyRegistered(organization.getCnpj(), null, organizationId)) {
            System.out.println("A organization with that cnpj is already registered.");
            throw new OrganizationAlreadyRegisteredException("A organization with that cnpj is already registered.");
        }

        // creates the organization.
        organization = organizationRepository.save(organization);

        // creates the user.
        user = userRepository.save(user);

        userRepository.addOrganization(user.getUsername(), organization.getId());
        userRepository.addAuthority(user.getUsername(), "ACCOUNTANT_ADMIN");

        return user;
    }

}
