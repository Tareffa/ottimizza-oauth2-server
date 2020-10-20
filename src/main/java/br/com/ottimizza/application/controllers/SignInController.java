package br.com.ottimizza.application.controllers;

import java.net.URLEncoder;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.Arrays;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.ottimizza.application.services.SecurityService;

@Controller
public class SignInController {

    @Inject
    SecurityService securityService;

    @Value("${oauth2-config.server-url}")
    private String SERVER_URL;

    @Value("${oauth2-config.client-id}")
    private String CLIENT_ID;

    @Value("${oauth2-config.default-success-redirect}")
    private String DEFAULT_SUCCESS_URL;

    // String DEFAULT_LOGO_URL =
    // "https://ottimizza.com.br/wp-content/themes/ottimizza/images/logo-inverse.png";
    String DEFAULT_LOGO_URL = "/assets/img/logos/tareffa-white.png";

    private String getSignInLogo() {
        return System.getenv("SIGNIN_LOGO");
    }

    private String getSignInBackground() {
        return System.getenv("SIGNIN_BACKGROUND");
    }

    private String getSignInTitle() {
        return System.getenv("SIGNIN_TITLE");
    }

    private SavedRequest getSavedRequest(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null)
            return new HttpSessionRequestCache().getRequest(request, response);
        return null;
    }

    private void getCustomAttributes(Model model, HttpServletRequest request, HttpServletResponse response) {
        String logoURL = getSignInLogo();
        SavedRequest savedRequest = getSavedRequest(request, response);
        if (savedRequest != null) {
            try {
                String logoParameterValue = savedRequest.getParameterMap().get("logo")[0];
                if (logoParameterValue != null && !logoParameterValue.equals(""))
                    logoURL = logoParameterValue;
            } catch (Exception ex) {
            }
        }
        model.addAttribute("logo", logoURL);
        model.addAttribute("background", getSignInBackground());
        model.addAttribute("title", getSignInTitle());
    }

    private String defaultRedirect() throws Exception {
        String redirect = MessageFormat.format("{0}/oauth/authorize?response_type=code&client_id={1}&redirect_uri={2}",
                SERVER_URL, CLIENT_ID, URLEncoder.encode(DEFAULT_SUCCESS_URL, "UTF-8") // URLEncoder.encode(redirect_uri,
                                                                                       // "UTF-8");
        );
        return "redirect:" + redirect;
    }

    @GetMapping("/login")
    public String loginPage(Model model, Principal principal, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        getCustomAttributes(model, request, response);

        try {
            SavedRequest savedRequest = getSavedRequest(request, response);

            if (savedRequest != null) {
                try {
                    String client_id = savedRequest.getParameterMap().get("client_id")[0];
                    String response_type = savedRequest.getParameterMap().get("response_type")[0];
                    String redirect_uri = savedRequest.getParameterMap().get("redirect_uri")[0];

                    if (Arrays.asList(client_id, response_type, redirect_uri).contains(null)) {
                        return defaultRedirect();
                    }
                } catch (Exception ex) {
                    return defaultRedirect();
                }
            } else {
                return defaultRedirect();
            }
        } catch (Exception e) {
            return defaultRedirect();
        }

        // if (savedRequest != null) {
        // try {
        // String logoParameterValue = savedRequest.getParameterMap().get("logo")[0];
        // if (logoParameterValue != null && !logoParameterValue.equals(""))
        // logoURL = logoParameterValue;
        // } catch (Exception ex) {
        // }
        // }

        // if (principal != null) {
        // //depends on your security config, maybe you want to check the security
        // context instead if you allow anonym access
        // String redirect_uri = request.getParameter("redirect_uri");
        // //here you must get all the other attributes thats needed for the authorize
        // url
        // if (redirect_uri == null) {
        // redirect_uri = "https://accounts.ottimizza.com.br";
        // }
        // return
        // "redirect:/oauth/authorize?response_type=token&state=6c2bb162-0f26-4caa-abbe-b65f7e5c6a2e&client_id=admin&redirect_uri="
        // + URLEncoder.encode(redirect_uri, "UTF-8");
        // }
        // return "login";
        return "login.html";
    }

    @GetMapping("/maintenance")
    public String manteinencePage() {
        return "maintenance.html";
    }

}