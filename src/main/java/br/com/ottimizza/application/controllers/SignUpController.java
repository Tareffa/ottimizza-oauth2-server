package br.com.ottimizza.application.controllers;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.ottimizza.application.domain.exceptions.OrganizationAlreadyRegisteredException;
import br.com.ottimizza.application.domain.exceptions.UserAlreadyRegisteredException;
import br.com.ottimizza.application.model.Organization;
import br.com.ottimizza.application.model.user.User;
import br.com.ottimizza.application.model.user_organization.UserOrganizationInvite;
import br.com.ottimizza.application.services.SignUpService;

@Controller
public class SignUpController {

    @Inject
    SignUpService signUpService;

    @GetMapping("/register")
    public String signupPage(@RequestParam(name = "token", defaultValue = "", required = false) String token,
            Model model) {

        User user = new User();
        Organization organization = new Organization();

        if (!token.equals("")) {
            System.out.println("User has token  " + token);

            UserOrganizationInvite inviteTokenDetails = signUpService.getInviteTokenDetails(token);

            //
            user.setEmail(inviteTokenDetails.getEmail());
            organization = inviteTokenDetails.getOrganization();

            System.out.println(organization.getId());
        }

        model.addAttribute("user", user);
        model.addAttribute("organization", organization);

        return "signup.html";
    }

    @PostMapping("/register")
    public String signup(@RequestParam(name = "token", defaultValue = "") String token,  User user,
            Organization organization, Model model) {
        try {
            System.out.println("Registering new user...");
            user = signUpService.register(user, organization, token);
            System.out.println("User is registered...");

            // if no exceptions was thrown adds a success attribute.
            model.addAttribute("success", "true");
            
            
        } catch (UserAlreadyRegisteredException ex) {
            model.addAttribute("error_message", "Um usuário com este endereço de email já está cadastrado!");
        } catch (OrganizationAlreadyRegisteredException ex) {
            model.addAttribute("error_message", "Uma empresa com este CPF/CNPJ já está cadastrada!");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return "signup.html";
    }

}
