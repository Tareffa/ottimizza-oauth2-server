package br.com.ottimizza.application.services;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import br.com.ottimizza.application.SpringbootOauth2ServerApplication;
import br.com.ottimizza.application.domain.dtos.OrganizationDTO;
import br.com.ottimizza.application.domain.dtos.models.invitation.InvitationDTO;
import br.com.ottimizza.application.domain.exceptions.OrganizationAlreadyRegisteredException;
import br.com.ottimizza.application.model.Organization;
import br.com.ottimizza.application.model.user.User;

// @formatter:off
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringbootOauth2ServerApplication.class)
class InvitationServiceTest {
	static final String EMAIL = "lucas@ottimizza.com.br";

    @Inject
    InvitationService invitationService;

    @Test
    public void givenInvitationDTO_whenSaveAccountantInvitation_thenOK() throws Exception { 
		OrganizationDTO organizationDTO = OrganizationDTO.builder()
            .name("Accounting Firm Co")
            .cnpj("10000000000101")
            .type(Organization.Type.ACCOUNTING).build();

        InvitationDTO invitationDTO = InvitationDTO.builder()
				.email(EMAIL)
				.type(User.Type.ACCOUNTANT)
				.userDetails(null)
				.organization(organizationDTO)
				.build();

        InvitationDTO created = invitationService.inviteAccountant(invitationDTO);
        
		Assertions.assertNotNull(created);
        Assertions.assertNotNull(created.getId());
	}

	@Test
    public void givenInvitationDTO_whenSaveAccountantInvitation_thenOrganizationAlreadyRegisteredException() throws Exception { 
		OrganizationDTO organizationDTO = OrganizationDTO.builder()
            .name("Accounting Firm Co")
            .cnpj("10000000000101")
            .type(Organization.Type.ACCOUNTING).build();

        InvitationDTO invitationDTO = InvitationDTO.builder()
				.email(EMAIL)
				.type(User.Type.ACCOUNTANT)
				.userDetails(null)
				.organization(organizationDTO)
				.build();

        Assertions.assertThrows(OrganizationAlreadyRegisteredException.class, () -> {
        	invitationService.inviteAccountant(invitationDTO);
        });
	}

	@Test
    public void givenInvitationDTO_whenNoEmail_thenIllegalArgumentsException() throws Exception { 
		OrganizationDTO organizationDTO = OrganizationDTO.builder()
            .name("Accounting Firm Co")
            .cnpj("10000000000101")
            .type(Organization.Type.ACCOUNTING).build();
        InvitationDTO invitationDTO = InvitationDTO.builder()
				.email(null)
				.type(User.Type.ACCOUNTANT)
				.userDetails(null)
				.organization(organizationDTO)
				.build();

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
        	invitationService.inviteAccountant(invitationDTO);
        });
	}

	@Test
    public void givenInvitationDTO_whenNoType_thenIllegalArgumentsException() throws Exception { 
		OrganizationDTO organizationDTO = OrganizationDTO.builder()
            .name("Accounting Firm Co")
            .cnpj("10000000000101")
            .type(Organization.Type.ACCOUNTING).build();
        InvitationDTO invitationDTO = InvitationDTO.builder()
				.email(EMAIL)
				.type(null)
				.userDetails(null)
				.organization(organizationDTO)
				.build();

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
        	invitationService.inviteAccountant(invitationDTO);
        });
	}

	@Test
    public void givenInvitationDTO_whenNoAccountingInformation_thenIllegalArgumentsException() throws Exception {
        InvitationDTO invitationDTO = InvitationDTO.builder()
				.email(EMAIL)
				.type(User.Type.ACCOUNTANT)
				.userDetails(null)
				.organization(null)
				.build();

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
        	invitationService.inviteAccountant(invitationDTO);
        });
	}

	// @Test
	// void inviteAccountant() {

	// 	String email = "user01@invitation.com.br";
        
    //     this.user = new User();
	// 	this.user.setUsername("test@ottimizza.com.br");
	// 	PasswordResetToken passwordResetToken = new PasswordResetToken(null, "token", user, new Date(2019, 2, 20));
	// 	try {
	// 		Assertions.assertTrue(
	// 				securityService.validatePasswordResetTokenForUser("test@ottimizza.com.br", passwordResetToken));
	// 	} catch (PasswordResetTokenInvalidException exception) {
	// 		fail("PasswordResetTokenInvalidException was thrown, but no exception should be expected.");
	// 	}
	// }

	// @Test
	// void whenTokenInvalidForUser() {
	// 	this.securityService = new SecurityService();

	// 	this.user = new User();
	// 	this.user.setUsername("test@ottimizza.com.br");
	// 	PasswordResetToken passwordResetToken = new PasswordResetToken(null, "token", user, new Date(2019, 2, 20));

	// 	Assertions.assertThrows(PasswordResetTokenInvalidException.class, () -> {
	// 		securityService.validatePasswordResetTokenForUser("test2@ottimizza.com.br", passwordResetToken);
	// 	});
	// }
}
