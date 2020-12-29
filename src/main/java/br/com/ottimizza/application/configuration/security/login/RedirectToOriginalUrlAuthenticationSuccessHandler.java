package br.com.ottimizza.application.configuration.security.login;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Component;

import br.com.ottimizza.application.configuration.WebSecurityConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RedirectToOriginalUrlAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final String DEFAULT_TARGET_URL = "/";

    public RedirectToOriginalUrlAuthenticationSuccessHandler() {
        super(DEFAULT_TARGET_URL);
        super.setTargetUrlParameter(WebSecurityConfig.TARGET_AFTER_SUCCESSFUL_LOGIN_PARAM);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        // User userInfo = (User) authentication.getPrincipal();
        // userInfo.setColour(request.getParameter(WebSecurityConfiguration.COLOUR_PARAM));
        super.onAuthenticationSuccess(request, response, authentication);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {
        String targetUrl = super.determineTargetUrl(request, response, authentication);

        log.info("Request: " + request.getMethod());
        log.info("Request URI: " + request.getRequestURI() + "?" + request.getQueryString());
        log.info("Target Param: " + this.getTargetUrlParameter());
        log.info("TargetURL: " + targetUrl);

        if (UrlUtils.isAbsoluteUrl(targetUrl)) {
            log.warn("Absolute target URL {} identified and suppressed", targetUrl);
            return DEFAULT_TARGET_URL;
        }

        try {
            targetUrl = URLDecoder.decode(targetUrl, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return targetUrl;
    }
}