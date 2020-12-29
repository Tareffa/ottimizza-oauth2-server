package br.com.ottimizza.application.configuration.security;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SaveContextOnUpdateOrErrorResponseWrapper;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import br.com.ottimizza.application.configuration.security.cookie.SSIDCookie;
import br.com.ottimizza.application.configuration.security.filters.CookieAuthenticationWrapper;
import br.com.ottimizza.application.configuration.security.filters.JwtAuthenticationWrapper;
import br.com.ottimizza.application.model.httpsession.HttpSessionDetails;
import br.com.ottimizza.application.model.httpsession.HttpSessionPrincipal;
import br.com.ottimizza.application.model.httpsession.HttpSessionPrincipalId;
import br.com.ottimizza.application.repositories.httpsession.HttpSessionPrincipalsRepository;
import br.com.ottimizza.application.repositories.httpsession.HttpSessionRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j // @formatter:off
@Component
public class CustomSecurityContextRepository implements SecurityContextRepository {

    private static final String EMPTY_CREDENTIALS = "";

    private static final String ANONYMOUS_USER = "anonymousUser";
    
    @Value("${auth.cookie.hmac-key}")
    private String cookieHmacKey;

    @Autowired
    JwtAuthenticationWrapper jwtAuthenticationWrapper;

    @Autowired
    CookieAuthenticationWrapper cookieAuthenticationWrapper;

    @Autowired
    private HttpSessionRepository httpSessionDetailsRepository;

    @Autowired
    private HttpSessionPrincipalsRepository httpSessionPrincipalsRepository;


    @Override 
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        HttpServletRequest request = requestResponseHolder.getRequest();
        HttpServletResponse response = requestResponseHolder.getResponse();

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        requestResponseHolder.setResponse(new SaveToCookieResponseWrapper(request, response));

        // checking first for a Authorization Header...
        OAuth2Authentication jwtAuthentication = jwtAuthenticationWrapper.readAuthenticationFromRequest(request).orElse(null);

        if (jwtAuthentication != null) {
            context.setAuthentication(jwtAuthentication);
        } else {
            UsernamePasswordAuthenticationToken cookieAuthentication = cookieAuthenticationWrapper.readAuthenticationFromRequest(request).orElse(null);

            if (cookieAuthentication != null) {
                context.setAuthentication(cookieAuthentication);
            }
        }

        return context;
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
    }

     @Override
    public boolean containsContext(HttpServletRequest request) {
        return cookieAuthenticationWrapper.isCookiePresent(request);
    }


    
    // @Slf4j
    private class SaveToCookieResponseWrapper extends SaveContextOnUpdateOrErrorResponseWrapper {
        private final HttpServletRequest request;

        SaveToCookieResponseWrapper(HttpServletRequest request, HttpServletResponse response) {
            super(response, true);
            this.request = request;
        }

        @Override
        protected void saveContext(SecurityContext securityContext) {
            HttpServletResponse response = (HttpServletResponse) getResponse();
            Authentication authentication = securityContext.getAuthentication();

            if (authentication == null) {
                // LOG.debug("No securityContext.authentication, skip saveContext");
                return;
            }

            if (ANONYMOUS_USER.equals(authentication.getPrincipal())) {
                // LOG.debug("Anonymous User SecurityContext, skip saveContext");
                return;
            }

            if (!(authentication.getPrincipal() instanceof User)) {
                // LOG.warn("securityContext.authentication.principal of unexpected type {},
                // skip saveContext",
                // authentication.getPrincipal().getClass().getCanonicalName());
                return;
            }

            User userInfo = (User) authentication.getPrincipal();

            cookieAuthenticationWrapper.isCookiePresent(request);
            
            if (!cookieAuthenticationWrapper.isCookiePresent(request)) {
                UUID ssid = UUID.randomUUID();
                String principalName = userInfo.getUsername();
                String principalAlias = "0";

                HttpSessionPrincipal principal = HttpSessionPrincipal.builder()
                        .id(
                            HttpSessionPrincipalId.builder()
                                .id(ssid).principal(principalName)
                                .build())
                        .sessionId(ssid)
                        .alias(principalAlias)
                        .principal(principalName).build();

                HttpSessionDetails sessionDetails = HttpSessionDetails.builder()
                        .id(ssid)
                        .alias(principalAlias)
                        .principal(principalName).build();

                httpSessionDetailsRepository.save(sessionDetails);
                httpSessionPrincipalsRepository.save(principal);

                // String ssid = cookie.getSSID() ? cookie.getSSID() : "";
                // User userInfo = (User) authentication.getPrincipal();
                SSIDCookie cookie = new SSIDCookie(
                        sessionDetails.getId().toString(), userInfo, cookieHmacKey);
                cookie.setSecure(request.isSecure());

                response.addCookie(cookie);
            } else {
                UUID ssid = UUID.fromString(cookieAuthenticationWrapper.getSSIDFromRequest(request));
                
                // busca sessao por id
                HttpSessionDetails session = httpSessionDetailsRepository.findById(ssid).orElse(null);

                if (session != null) {
                    // List all the principals
                    String principalAlias = "";
                    String principalName = "";

                    List<HttpSessionPrincipal> authenticated = httpSessionPrincipalsRepository.findBySessionId(ssid);

                    List<HttpSessionPrincipal> principalsFiltered = authenticated.stream()
                            .filter(p -> p.getPrincipal().equals(userInfo.getUsername())).collect(Collectors.toList());

                    if (principalsFiltered.size() == 0) {
                        principalAlias = String.valueOf(authenticated.size());
                        principalName = userInfo.getUsername();
                    } else {
                        principalAlias = principalsFiltered.get(0).getAlias();
                        principalName = principalsFiltered.get(0).getPrincipal();
                    }

                    HttpSessionPrincipal principal = new HttpSessionPrincipal();

                    HttpSessionPrincipalId principalId = new HttpSessionPrincipalId();
                    principalId.setId(ssid);
                    principalId.setPrincipal(principalName);

                    principal.setId(principalId);
                    principal.setAlias(principalAlias);
                    principal.setSessionId(ssid);
                    principal.setPrincipal(principalName);

                    httpSessionPrincipalsRepository.save(principal);

                    session.setAlias(principalAlias);
                    session.setPrincipal(principalName);
                    httpSessionDetailsRepository.save(session);

                } else {

                    session = new HttpSessionDetails();
                    session.setId(ssid);
                    session.setAlias("0");
                    session.setPrincipal(userInfo.getUsername());
                    httpSessionDetailsRepository.save(session);

                }
            }

        }

    }

}