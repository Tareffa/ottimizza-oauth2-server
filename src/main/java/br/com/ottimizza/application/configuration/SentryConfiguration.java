package br.com.ottimizza.application.configuration;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import io.sentry.spring.SentryExceptionResolver;

@Configuration
public class SentryConfiguration {

    List<String> ignoredMessages = Arrays.asList(
        "Invalid authorization code", 
        "Invalid refresh token (expired)", 
        "Token was not recognised"
    );

    List<String> ignoredClasses = Arrays.asList(
        // "java.lang.IllegalArgumentException"
    );

    @Bean
    public HandlerExceptionResolver sentryExceptionResolver() {
        return new SentryExceptionResolver() {
            @Override
            public ModelAndView resolveException(HttpServletRequest request,
                    HttpServletResponse response,
                    Object handler,
                    Exception ex) {
                Throwable rootCause = ex;

                while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                    rootCause = rootCause.getCause();
                }

                if (validateIgnoredMessages(rootCause.getMessage()) && validateIgnoredClasses(rootCause.toString())) {
                    super.resolveException(request, response, handler, ex);
                }

                return null;
            }   

        };
    }

    private boolean validateIgnoredMessages(String message) {
        if (ignoredMessages.size() > 0) {
            for (String ignoredMessage : ignoredMessages) {
                if (ignoredMessage.contains(message)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateIgnoredClasses(String resume) {
        if (ignoredClasses.size() > 0) {
            for (String ignoredClass : ignoredClasses) {
                if (!ignoredClass.contains(resume)) {
                    return false;
                }
            }
        }
        return true;
    }

    // @Bean
    // public HandlerExceptionResolver sentryExceptionResolver() {
    //     return new io.sentry.spring.SentryExceptionResolver();
    // }

    @Bean
    public ServletContextInitializer sentryServletContextInitializer() {
        return new io.sentry.spring.SentryServletContextInitializer();
    }

}