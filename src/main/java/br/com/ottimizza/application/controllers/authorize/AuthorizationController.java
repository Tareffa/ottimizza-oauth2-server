package br.com.ottimizza.application.controllers.authorize;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.ottimizza.application.domain.projections.AuthenticatedAccountDetails;
import br.com.ottimizza.application.repositories.httpsession.HttpSessionPrincipalsRepository;
import br.com.ottimizza.application.services.security.ContextSecurityService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/oauth")
public class AuthorizationController {

	@Value("${oauth2-config.client-id}")
    private String OAUTH2_CLIENT_ID;

    @Value("${backgroundColor}")
    private String BACKGROUND_COLOR;
	
	@Value("${oauth2-config.default-success-redirect}")
    private String DEFAULT_SUCCESS_REDIRECT;
	
    @Autowired
    private ContextSecurityService contextSecurityService;

    @Autowired
    private HttpSessionPrincipalsRepository httpSessionPrincipalsRepository;

    @PostMapping("/{oauthuser}/logout")
    public String oauthUserLogout(@PathVariable("oauthuser") String oauthUser, HttpServletRequest request)
            throws Exception {
        if (contextSecurityService.logoutOAuthUser(oauthUser, request)) {
            return "redirect:/logout";
        }
        return "redirect:/";
    }

    @GetMapping("/authorize/oauthchooseaccount")
    public String oauthChooseAccount(Model model, HttpServletRequest request, HttpServletResponse response,
            Principal principal) throws Exception {
        String ssid = contextSecurityService.getSSIDFromRequest(request);

        List<String> oauthRequestParams = Arrays.asList("client_id", "response_type", "redirect_uri");
        
        if (!request.getParameterMap().keySet().containsAll(oauthRequestParams)) {
            String queryString = MessageFormat.format("client_id={0}&response_type=code&redirect_uri={1}",OAUTH2_CLIENT_ID, DEFAULT_SUCCESS_REDIRECT);
            model.addAttribute("actionUrl", "/oauth/authorize/oauthchooseaccount?" + queryString);
            return "redirect:/oauth/authorize/oauthchooseaccount?" + queryString;
        }
        
        if (ssid != null) {
            // session principals with user details (*)
            List<AuthenticatedAccountDetails> authenticatedAccounts = httpSessionPrincipalsRepository
                    .findAuthenticatedAccountDetailsBySessionId(UUID.fromString(ssid));

            model.addAttribute("authenticatedAccounts", authenticatedAccounts);
        }
        model.addAttribute("backgroundColor", BACKGROUND_COLOR);

        return "oauth/oauthchooseaccount.html";
    }

    @PostMapping("/authorize/oauthchooseaccount")
    public String oauthChooseAccountForm(@RequestParam("oauthuser") String oauthUser, Model model,
            HttpServletRequest request, HttpServletResponse response, Principal principal) throws Exception {
        String queryString = request.getQueryString();

        List<String> oauthRequestParams = Arrays.asList("client_id", "response_type", "redirect_uri");

        this.contextSecurityService.switchOAuthUser(oauthUser, request);

        if (request.getParameterMap().keySet().containsAll(oauthRequestParams)) {
            return "redirect:/oauth/authorize?" + queryString;
        }

        return "redirect:/oauth/authorize/oauthchooseaccount";
    }

}