package br.com.ottimizza.application.configuration.security.login;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.ottimizza.application.configuration.WebSecurityConfig;

@Component
public class LoginWithTargetUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    public LoginWithTargetUrlAuthenticationEntryPoint() {
        super(WebSecurityConfig.LOGIN_FORM_URL);
    }

    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) {

        String encodedUrlString = getFullURI(request);

        try {
            encodedUrlString = URLEncoder.encode(encodedUrlString, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
        }

        return UriComponentsBuilder.fromUriString(super.determineUrlToUseForThisRequest(request, response, exception))
                .queryParam(WebSecurityConfig.TARGET_AFTER_SUCCESSFUL_LOGIN_PARAM, encodedUrlString)
                .toUriString();
    }

    private String getFullURI(HttpServletRequest request) {
        StringBuilder requestURL = new StringBuilder(request.getRequestURI().toString());
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

}
