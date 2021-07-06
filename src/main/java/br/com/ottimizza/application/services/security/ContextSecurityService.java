package br.com.ottimizza.application.services.security;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

import br.com.ottimizza.application.configuration.security.filters.CookieAuthenticationWrapper;
import br.com.ottimizza.application.configuration.security.filters.JwtAuthenticationWrapper;
import br.com.ottimizza.application.model.httpsession.HttpSessionDetails;
import br.com.ottimizza.application.model.httpsession.HttpSessionPrincipal;
import br.com.ottimizza.application.repositories.httpsession.HttpSessionPrincipalsRepository;
import br.com.ottimizza.application.repositories.httpsession.HttpSessionRepository;

@Service
public class ContextSecurityService {

    @Autowired
    private CookieAuthenticationWrapper cookieAuthenticationWrapper;

    @Autowired
    private JwtAuthenticationWrapper jwtAuthenticationWrapper;

    @Autowired
    private HttpSessionRepository httpSessionDetailsRepository;

    @Autowired
    private HttpSessionPrincipalsRepository httpSessionPrincipalsRepository;

    //
    public void createHttpSession(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        UUID ssid = UUID.randomUUID();


    }

    public void appendSessionPrincipal(UUID ssid, Authentication authentication) {

    }


    // @formatter:off    
    public String getSSIDFromRequest(HttpServletRequest request) {
        return cookieAuthenticationWrapper.getSSIDFromRequest(request);
    }

    public boolean logoutOAuthUser(String oauthuser, HttpServletRequest request) {
        UUID hsid = UUID.fromString(getSSIDFromRequest(request));

        HttpSessionDetails session = this.httpSessionDetailsRepository.findById(hsid).orElse(null);
        List<HttpSessionPrincipal> principals = this.httpSessionPrincipalsRepository.findBySessionId(hsid);

        // descobrir qual principal estamos removendo...
        HttpSessionPrincipal principal = principals.stream()
                                            .filter(p -> p.getAlias().equals(oauthuser))
                                            .findFirst().orElse(null);
        
        
        if (principals.size() > 1) { 

            // removing the specified principal.
            this.httpSessionPrincipalsRepository.delete(principal);
            
            // find another principal to replace the removed one.
            HttpSessionPrincipal switchPrincipal = principals.stream()
                                            .filter(p -> !p.getAlias().equals(oauthuser))
                                            .findFirst().orElse(null);

            // @method pass a principal
            session.setAlias(switchPrincipal.getAlias());
            session.setPrincipal(switchPrincipal.getPrincipal());

            this.httpSessionDetailsRepository.save(session);
            // @method pass a principal

            this.updateSecurityContext(session);

            return false;
        } else {
            // removing the specified principal.
            this.httpSessionPrincipalsRepository.delete(principal);
            
            // removing the session.
            this.httpSessionDetailsRepository.delete(session);

            this.emptySecurityContext();

            return true;
        }

    }

    public void switchOAuthUser(String oauthuser, HttpServletRequest request) {
        UUID hsid = UUID.fromString(getSSIDFromRequest(request));

        HttpSessionDetails session = this.httpSessionDetailsRepository.findById(hsid).orElse(null);
        HttpSessionPrincipal principal = this.httpSessionPrincipalsRepository.findBySessionIdAndAlias(hsid, oauthuser);

        session.setAlias(principal.getAlias());
        session.setPrincipal(principal.getPrincipal());

        this.httpSessionDetailsRepository.save(session);

        this.updateSecurityContext(session);
    }

    //
    public Optional<UsernamePasswordAuthenticationToken> readCookieAuthenticationFromRequest(HttpServletRequest request) {
        return cookieAuthenticationWrapper.readAuthenticationFromRequest(request);
    }

    public Optional<OAuth2Authentication> readJwtAuthenticationFromRequest(HttpServletRequest request) {
        return jwtAuthenticationWrapper.readAuthenticationFromRequest(request);
    }

    //
    private void emptySecurityContext() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(context);
    }

    private void updateSecurityContext(HttpSessionDetails session) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            session.getPrincipal(), "", Arrays.asList()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
