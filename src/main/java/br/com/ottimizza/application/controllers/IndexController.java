package br.com.ottimizza.application.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import br.com.ottimizza.application.domain.projections.AuthenticatedAccountDetails;
import br.com.ottimizza.application.model.Organization;
import br.com.ottimizza.application.repositories.httpsession.HttpSessionPrincipalsRepository;
import br.com.ottimizza.application.repositories.users.UsersRepository;
import br.com.ottimizza.application.services.security.ContextSecurityService;

@Controller
public class IndexController {

    @Value("${backgroundColor}")
    private String BACKGROUND_COLOR;

    @Inject
    UsersRepository userRepository;

    @Autowired
    private ContextSecurityService contextSecurityService;

    @Autowired
    private HttpSessionPrincipalsRepository httpSessionPrincipalsRepository;

    @GetMapping("/")
    public String indexPage(Model model, Principal principal, HttpSession session, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String ssid = contextSecurityService.getSSIDFromRequest(request);

        if (ssid != null) {
            List<AuthenticatedAccountDetails> authenticatedAccounts = httpSessionPrincipalsRepository
                    .findAuthenticatedAccountDetailsBySessionId(UUID.fromString(ssid));
            model.addAttribute("authenticatedAccounts", authenticatedAccounts);
        }
        model.addAttribute("backgroundColor", BACKGROUND_COLOR);

        return "index.html";
    }

    /**
     * ******************************************************************************************
     * ** EMPRESAS
     * *******************************************************************************************
     */
    @GetMapping(value = { "/empresas", "/organizations" })
    public String organizations(Principal principal, Model model) {
        model.addAttribute("authorizedUser", userRepository.findByEmail(principal.getName()));
        return "organizations/organizations.html";
    }

    @GetMapping("/organizations/{externalId}")
    public String organization(@PathVariable("externalId") String externalId, Principal principal, Model model) {
        model.addAttribute("authorizedUser", userRepository.findByEmail(principal.getName()));
        return "organizations/organization.html";
    }

    @GetMapping(value = { "/accounting/{externalId}", "/contabilidade/{externalId}" })
    public String accounting(@PathVariable("externalId") String externalId, Principal principal, Model model) {
        model.addAttribute("authorizedUser", userRepository.findByEmail(principal.getName()));
        return "accounting/accounting.html";
    }

    /**
     * ********************************************************************************
     * ** USUARIOS
     * *********************************************************************************
     */
    @GetMapping(value = { "/usuarios", "/users" })
    public String users(Principal principal, Model model) {
        model.addAttribute("authorizedUser", userRepository.findByEmail(principal.getName()));
        return "users/users.html";
    }

    @GetMapping(value = { "/usuarios/{id}", "/users/{id}" })
    public String user(@PathVariable("id") String externalId, Principal principal, Model model) {
        model.addAttribute("authorizedUser", userRepository.findByEmail(principal.getName()));
        return "users/user.html";
    }

    /**
     * ********************************************************************************
     * ** USUARIOS
     * *********************************************************************************
     */
    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        // find user by username.
        model.addAttribute("authorizedUser", userRepository.findByEmail(principal.getName()));

        return "profile/profile.html";
    }

    @GetMapping("/profile/security")
    public String profileSecurity(Principal principal, Model model) {
        // find user by username.
        model.addAttribute("authorizedUser", userRepository.findByEmail(principal.getName()));

        return "profile/security.html";
    }

    /**
     * ********************************************************************************
     * ** PASSWORD RESET
     * *********************************************************************************
     */
    @GetMapping(value = "/password_recovery")
    public String passwordRecoveryPage(Locale locale, Model model) {
        return "password_recovery.html";
    }

}