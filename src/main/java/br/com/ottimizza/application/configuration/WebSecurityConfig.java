package br.com.ottimizza.application.configuration;

import br.com.ottimizza.application.configuration.security.CustomSecurityContextRepository;
import br.com.ottimizza.application.configuration.security.cookie.SSIDCookie;
import br.com.ottimizza.application.configuration.security.login.LoginWithTargetUrlAuthenticationEntryPoint;
import br.com.ottimizza.application.configuration.security.login.RedirectToOriginalUrlAuthenticationSuccessHandler;
import br.com.ottimizza.application.services.impl.UserDetailsService;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity(debug = false)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String LOGIN_FORM_URL = "/login";
    public static final String TARGET_AFTER_SUCCESSFUL_LOGIN_PARAM = "redirect";

    @Value("${oauth2-config.default-success-redirect}")
    private String DEFAULT_SUCCESS_URL;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //
    //
    //
    @Autowired
    private CustomSecurityContextRepository customSecurityContextRepository;

    @Autowired
    private LoginWithTargetUrlAuthenticationEntryPoint loginWithTargetUrlAuthenticationEntryPoint;

    @Autowired
    private RedirectToOriginalUrlAuthenticationSuccessHandler redirectToOriginalUrlAuthenticationSuccessHandler;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //@formatter:off
        String[] resources = Arrays.asList(new String[] {
                "/assets/**", "/css/**", "/js/**", "/images/**"
        }).toArray(new String[] {});
        
        String[] allowed = Arrays.asList(new String[] {
                "/password_recovery*", 
                "/password_reset*", 
                "/register*", 
                "/user/password_recovery*", 
                "/user/password_reset*",
                "/maintenance",
                "/tokens",
                "/auth/**"
                // "/oauth/userinfo*",
                // "/oauth/tokeninfo*"
        }).toArray(new String[] {});
        
        // http
        //     .authorizeRequests()
        //         .antMatchers("/api/**").authenticated();

        http
            .authorizeRequests()
                .antMatchers(resources).permitAll();

        http.authorizeRequests()
                .antMatchers(allowed).permitAll();

        // http.authorizeRequests()
        //         .antMatchers("/oauth/tokeninfo*").permitAll();

        String TOKENINFO_URL = "/oauth/tokeninfo";
        String USERINFO_URL = "/oauth/userinfo";
        String INFO_URL = "/oauth/info";

        http
            // deactivate session creation
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and().csrf().disable()
            // store SecurityContext in Cookie / delete Cookie on logout
            .securityContext()
                .securityContextRepository(customSecurityContextRepository)
            .and().logout().permitAll().deleteCookies(SSIDCookie.NAME)

            // deactivate RequestCache and append originally requested URL as query parameter to login form request
            .and().requestCache().disable()
            .exceptionHandling().authenticationEntryPoint(loginWithTargetUrlAuthenticationEntryPoint)

            // configure form-based login
            .and()
            .formLogin()
            .loginPage(LOGIN_FORM_URL).permitAll()
            // after successful login forward user to originally requested URL
            .successHandler(redirectToOriginalUrlAuthenticationSuccessHandler);
        http

            .authorizeRequests()
                .antMatchers("/login**", "/user/**", "/api/**", USERINFO_URL, TOKENINFO_URL, INFO_URL).permitAll() 
                .anyRequest().authenticated()
            // .and()
            //     .formLogin()
            //     .loginPage("/login")
            //     // .defaultSuccessUrl(DEFAULT_SUCCESS_URL, false)
            //     // .success
            //     .permitAll()
                    
            //     // after successful login forward user to originally requested URL
            //     .successHandler(redirectToOriginalUrlAuthenticationSuccessHandler)

            .and()
                .logout()
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "SESSION")
                .clearAuthentication(true)
                //.logoutSuccessUrl("/exit")
                .permitAll();
        //@formatter:on
    }

}
