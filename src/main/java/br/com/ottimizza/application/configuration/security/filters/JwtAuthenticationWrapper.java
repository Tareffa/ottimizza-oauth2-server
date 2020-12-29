package br.com.ottimizza.application.configuration.security.filters;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationWrapper {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_HEADER_PREFIX = "Bearer";

    @Autowired
    private TokenStore jwtTokenStore;

    public Optional<OAuth2Authentication> readAuthenticationFromRequest(HttpServletRequest request) {
        if (request.getHeader(AUTHORIZATION_HEADER) == null) {
            return Optional.empty();
        }

        String jwtTokenValue = request.getHeader(AUTHORIZATION_HEADER).replace(AUTHORIZATION_HEADER_PREFIX, "").trim();

        OAuth2Authentication authetication = this.jwtTokenStore.readAuthentication(jwtTokenValue);

        return Optional.of(authetication);
    }

}
