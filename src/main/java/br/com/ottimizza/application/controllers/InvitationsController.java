package br.com.ottimizza.application.controllers;

import java.security.Principal;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.ottimizza.application.domain.dtos.OrganizationDTO;
import br.com.ottimizza.application.domain.dtos.UserDTO;
import br.com.ottimizza.application.domain.dtos.models.invitation.InvitationDTO;
import br.com.ottimizza.application.domain.dtos.responses.GenericResponse;
import br.com.ottimizza.application.domain.responses.GenericPageableResponse;
import br.com.ottimizza.application.model.Organization;
import br.com.ottimizza.application.model.user.User;
import br.com.ottimizza.application.model.user_organization.UserOrganizationInvite;
import br.com.ottimizza.application.services.InvitationService;
import br.com.ottimizza.application.services.MailServices;
import br.com.ottimizza.application.services.SignUpService;

@RestController // @formatter:off
@RequestMapping("/api/v2/invitations")
public class InvitationsController {

    @Inject
    InvitationService invitationService;

    @Inject
    SignUpService signUpService;

    @Inject
	MailServices mailServices;

    @GetMapping
    public ResponseEntity<?> fetch(@RequestParam(name = "page_index", required = false, defaultValue = "0") int pageIndex,
                                   @RequestParam(name = "page_size", required = false, defaultValue = "10") int pageSize, 
                                   @RequestParam(name = "token", required = false, defaultValue = "") String token, 
                                   Principal principal) throws Exception {
        // validar se principal null > obrigar token arg
        if (principal == null) {
            if (!token.equals("")) {
                return ResponseEntity.ok(new GenericResponse<InvitationDTO>(
                    this.invitationService.fetchInvitationByToken(token))
                );
            }
        }

        return ResponseEntity.ok(new GenericPageableResponse<UserOrganizationInvite>(
                invitationService.fetchInvitedUsers("", pageIndex, pageSize, principal)));
    }

    @PostMapping
    public ResponseEntity<?> sendInvitation(@RequestBody InvitationDTO invitation, Principal principal)
            throws Exception {
        if (principal == null) {
            return ResponseEntity.ok(new GenericResponse<InvitationDTO>(
                this.invitationService.inviteAccountant(invitation))
            );
        } else {
            return ResponseEntity.ok("{}");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> signup(@RequestParam(name = "token", required = false, defaultValue = "") String token,
                                    @RequestBody Map<String, Object> args) throws Exception {
        if (args.get("user") == null) {
            throw new IllegalArgumentException("Informe os detalhes do usuário!");
        }         
        if (args.get("organization") == null) {
            throw new IllegalArgumentException("Informe os detalhes da organização!");
        } 

        ObjectMapper mapper = new ObjectMapper();

        UserDTO userDTO                 = mapper.convertValue(args.get("user"), UserDTO.class);
        OrganizationDTO organizationDTO = mapper.convertValue(args.get("organization"), OrganizationDTO.class);

        User user = userDTO.toEntity();
        Organization organization = organizationDTO.toEntity();

        return ResponseEntity.ok(new GenericResponse<>(
            signUpService.register(user, organization, token)
        ));
    }
	
//	@GetMapping("mailTo")
//    public String enviaEmailTeste() {
//    	MailDTO mail = new MailDTO();
//    	mail.setTo("dev.diogo@gmail.com");
//    	mail.setSubject("this is a subject");
//    	mail.setBody("nobody is a body.");
//    	
//    	try {
//    		mailServices.send(mail.getTo(), mail.getSubject(), mail.getBody());
//    		return "Success";
//    	}catch (Exception e) { }
//    	return "error";
//    }
}