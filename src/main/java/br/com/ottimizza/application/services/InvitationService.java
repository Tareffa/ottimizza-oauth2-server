package br.com.ottimizza.application.services;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Resource;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

import br.com.ottimizza.application.domain.exceptions.OrganizationNotFoundException;
import br.com.ottimizza.application.domain.exceptions.users.UserNotFoundException;
import br.com.ottimizza.application.domain.responses.GenericResponse;
import br.com.ottimizza.application.repositories.clients.OAuthClientRepository;
import br.com.ottimizza.application.repositories.organizations.OrganizationRepository;
/* ********************************** **
 * Entities
 * ********************************** */
import br.com.ottimizza.application.model.OAuthClientAdditionalInformation;
import br.com.ottimizza.application.model.OAuthClientDetails;
import br.com.ottimizza.application.model.Organization;
import br.com.ottimizza.application.model.user.User;
import br.com.ottimizza.application.model.user_organization.UserOrganizationInvite;

/* ********************************** **
 * Repositories
 * ********************************** */
import br.com.ottimizza.application.repositories.users.UsersRepository;
import br.com.ottimizza.application.repositories.UserOrganizationInviteRepository;

@Service // @formatter:off
public class InvitationService {

    @Inject
    UsersRepository userRepository;

    @Inject
    OrganizationRepository organizationRepository;

    @Inject
    UserOrganizationInviteRepository userOrganizationInviteRepository;
    
    @Inject
    MailServices mailServices;

    public UserOrganizationInvite invite(UserOrganizationInvite inviteDetails, Principal principal) 
            throws UserNotFoundException, OrganizationNotFoundException, Exception {
        User authenticated =  userRepository.findByUsername(principal.getName())
                                            .orElseThrow(() -> new UserNotFoundException(""));

        if (inviteDetails.getEmail() == null || inviteDetails.getEmail().equals("")) {
            throw new IllegalArgumentException("Informe o e-mail para enviar o convite!");
        }

        if (authenticated.getType().equals(User.Type.ACCOUNTANT)) {
            inviteDetails.setToken(UUID.randomUUID().toString());
            
            if (inviteDetails.getOrganization() == null) {
                inviteDetails.setType(User.Type.ACCOUNTANT);
                inviteDetails.setOrganization(authenticated.getOrganization());
            } else if (inviteDetails.getOrganization().getId() != null) {
                Organization organization = organizationRepository.fetchById(inviteDetails.getOrganization().getId());
                if (organization == null) {
                    throw new IllegalArgumentException("Não foi encontrada nenhuma empresa com o ID informado!");
                }
                inviteDetails.setType(organization.getType());
                inviteDetails.setOrganization(organization);
            } else if (inviteDetails.getOrganization().getCnpj() != null && !inviteDetails.getOrganization().getCnpj().equals("")) {
                Organization organization = organizationRepository.fetchByCnpj(inviteDetails.getOrganization().getCnpj());
                if (organization == null) {
                    throw new IllegalArgumentException("Não foi encontrada nenhuma empresa com o CNPJ informado!");
                }
                inviteDetails.setType(organization.getType());
                inviteDetails.setOrganization(organization);
            }
            List<UserOrganizationInvite> invites = userOrganizationInviteRepository.findByEmailAndOrganizationId(
                inviteDetails.getEmail(), authenticated.getOrganization().getId()
            );

            if (invites.size() == 0) {
                inviteDetails = userOrganizationInviteRepository.save(inviteDetails);
            } else {
                inviteDetails = invites.get(0);
            }

            this.sendInvitation(inviteDetails, authenticated);
        }
        return inviteDetails;
    }
    
    public Page<UserOrganizationInvite> fetchInvitedUsers(String email, Integer pageIndex, Integer pageSize, Principal principal)
            throws Exception {
        User authenticated = userRepository.findByUsername(principal.getName())
                                            .orElseThrow(() -> new UserNotFoundException(""));
        return userOrganizationInviteRepository.fetchInvitedUsersByAccountingId(
            email, authenticated.getOrganization().getId(), PageRequest.of(pageIndex, pageSize));
    }

    @Async
    private void sendInvitation(UserOrganizationInvite invite, User authorizedUser) {
        String accountingName = authorizedUser.getOrganization().getName();
        String subject = MessageFormat.format("Conta {0}.", accountingName);
        String template = mailServices.inviteCustomerTemplate(authorizedUser, invite.getToken());
        MailServices.Builder messageBuilder = new MailServices.Builder()
                .withName(accountingName)
                .withTo(invite.getEmail())
                .withCc(authorizedUser.getOrganization().getEmail())
                .withSubject(subject).withHtml(template);
        mailServices.send(messageBuilder); 
    }

}
