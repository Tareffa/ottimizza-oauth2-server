package br.com.ottimizza.application.services;

import java.util.Calendar;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import br.com.ottimizza.application.domain.exceptions.PasswordResetTokenExpiredException;
import br.com.ottimizza.application.domain.exceptions.PasswordResetTokenInvalidException;
import br.com.ottimizza.application.model.PasswordResetToken;
import br.com.ottimizza.application.repositories.PasswordRecoveryRepository;
import br.com.ottimizza.application.repositories.users.UsersRepository;

@Service
public class SecurityService {

    @Inject
    UsersRepository userRepository;

    @Inject
    PasswordRecoveryRepository passwordRecoveryRepository;

    protected boolean validatePasswordResetTokenForUser(String username, PasswordResetToken passwordRecoveryToken)
            throws PasswordResetTokenInvalidException {
        if (!passwordRecoveryToken.getUser().getUsername().equals(username) || passwordRecoveryToken == null) {
            throw new PasswordResetTokenInvalidException("invalid");
        }
        return true;
    }

    private void validatePasswordResetTokenExpiryDate(PasswordResetToken passwordRecoveryToken)
            throws PasswordResetTokenExpiredException {
        if (Calendar.getInstance().getTime().getTime() > passwordRecoveryToken.getExpiryDate().getTime()) {
            throw new PasswordResetTokenExpiredException("expired");
        }
    }

    public String validatePasswordRecoveryToken(String username, String token, HttpServletRequest request) { // @formatter:off
        PasswordResetToken passwordRecoveryToken = passwordRecoveryRepository.findByToken(token);
        try {
            validatePasswordResetTokenForUser(username, passwordRecoveryToken);  
            validatePasswordResetTokenExpiryDate(passwordRecoveryToken);
        } catch (PasswordResetTokenExpiredException expiredEx) {
            return expiredEx.getMessage();
        } catch (PasswordResetTokenInvalidException invalidEx) {
            return invalidEx.getMessage();
        }
        // this.authenticate(user, Arrays.asList(new SimpleGrantedAuthority("CHANGE_PASSWORD_PRIVILEGE")), request);
        return null;
    }
    
    // //@formatter:on
    // public void authenticate(String login, String password, HttpServletRequest
    // httpRequest) {
    // UsernamePasswordAuthenticationToken token = new
    // UsernamePasswordAuthenticationToken(login, password);
    // token.setDetails(new WebAuthenticationDetails(httpRequest));
    // ServletContext servletContext = httpRequest.getSession().getServletContext();
    // WebApplicationContext wac =
    // WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    // AuthenticationManager authManager = wac.getBean(AuthenticationManager.class);
    // Authentication authentication = authManager.authenticate(token);
    // SecurityContextHolder.getContext().setAuthentication(authentication);
    // }

    // //@formatter:off 
    // public void authenticate(User user, List<SimpleGrantedAuthority> authorities, HttpServletRequest httpRequest) {
    //     Authentication authentication = new UsernamePasswordAuthenticationToken(user, null,
    //             AuthorityUtils.createAuthorityList("CHANGE_PASSWORD_PRIVILEGE")
    //     );
    //     SecurityContextHolder.getContext().setAuthentication(authentication);
    // }

}
