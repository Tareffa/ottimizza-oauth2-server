
package br.com.ottimizza.application.controllers;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
// Spring - Security
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Controller;
// Spring - Web
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.ottimizza.application.client.TareffaClient;
// Models 
import br.com.ottimizza.application.model.PasswordResetToken;
import br.com.ottimizza.application.model.user.User;
// Repositories
import br.com.ottimizza.application.repositories.PasswordRecoveryRepository;
import br.com.ottimizza.application.repositories.users.UsersRepository;
import br.com.ottimizza.application.services.MailContentBuilder;
import br.com.ottimizza.application.services.MailServices;
// Services
import br.com.ottimizza.application.services.SecurityService;

@Controller
public class PasswordRecoveryController {

    @Value("${oauth2-config.server-url}")
    private String hostname;

    @Value("${oauth2-config.client-id}")
    private String OAUTH2_CLIENT_ID;

    @Value("${oauth2-config.client-secret}")
    private String OAUTH2_CLIENT_SECRET;
    
    @Inject
    private UsersRepository userRepository;

    @Inject
    private MailContentBuilder mailContentBuilder;

    @Inject
    private PasswordRecoveryRepository passwordRecoveryRepository;

    @Inject
    private SecurityService securityService;
    
    @Inject
    TareffaClient tareffaClient;
    
    @Inject
    MailServices mailServices;

    @GetMapping(value = "/password_reset") //@formatter:off
    public String passwordResetPage(@RequestParam(name = "username", defaultValue = "") String username, 
                                    @RequestParam(name = "token", defaultValue = "") String token,
                                    HttpServletRequest request, HttpServletResponse response,
                                    Locale locale, Model model) throws Exception {
        if (username.equals("")) {
            model.addAttribute("error", "username.empty");
            return "password_reset.html";
        }
        if (token.equals("")) {
            model.addAttribute("error", "token.empty");
            return "password_reset.html";
        }
        
        String passwordResetTokenValidationResult = securityService.validatePasswordRecoveryToken(
                    username, token, request
        );
        System.out.println("Password Reset Token  >>  " + passwordResetTokenValidationResult);
        if (passwordResetTokenValidationResult != null) {
            // implementar tela com erros.
            // model.addAttribute("token", token);
        }
        model.addAttribute("token", token);
        return "password_reset.html";
    } //@formatter:on

    @PostMapping(value = "/user/password_recovery") //@formatter:off
    public void passwordRecovery(@RequestParam("email") String email, 
                                 HttpServletRequest request, 
                                 HttpServletResponse response) throws Exception {
        String passwordRecoveryToken = UUID.randomUUID().toString();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            response.sendRedirect("/password_recovery?error");
            return;
        }

        PasswordResetToken passwordRecoveryTokenObject = new PasswordResetToken(passwordRecoveryToken, user);
        passwordRecoveryRepository.save(passwordRecoveryTokenObject);

        sendResetPasswordEmail(user, passwordRecoveryToken);

        response.sendRedirect("/password_recovery?success");
    }

    @PostMapping(value = "/user/password_reset") //@formatter:off
    public void passwordRecoveryPage(@RequestParam("token") String token,
                                     @RequestParam("new-password") String newPassoword,
                                     @RequestParam("new-password-check") String newPassowordCheck, 
                                     Principal principal, Locale locale, Model model, 
                                     HttpServletRequest request, HttpServletResponse response,
                                     SecurityContextHolderAwareRequestWrapper securityContext) 
                                     throws Exception {

        //if (!newPassoword.equals(newPassowordCheck)) {
            // response.sendRedirect("/password_reset?error=password_mismatch");
        //}

        PasswordResetToken passwordResetToken = passwordRecoveryRepository.findByToken(token);

        // atualiza a senha do usuario
        userRepository.updatePassword(
            new BCryptPasswordEncoder().encode(newPassoword), 
            passwordResetToken.getUser().getUsername()
        );
        
        
        //ENVIA NOVA SENHA PARA O TAREFFA
        //SENHA: newPassoword
        //EMAIL: passwordResetToken.getUser().getUsername()
        try {
            // Removida thread para evitar memory leaks
            // new Thread() {
            //     @Override
            //     public void run() {

            // Removida requisição 08/07/20        
            //        // String credentials = OAUTH2_CLIENT_ID + ":" + OAUTH2_CLIENT_SECRET;
            //        // String encodedCredentials = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
            //
            //        // tareffaClient.updateUserPasswordTareffa(
            //        //     encodedCredentials, 
            //        //     new UsuarioTareffa(passwordResetToken.getUser().getUsername(), newPassoword)
            //        // );
                    
            //    }
            // }.start();
        } catch (Exception e) {}

        // if (result != null) {
        //     // model.addAttribute("message", messages.getMessage("auth.message." + result, null, locale));
        //     return "redirect:/login?lang=" + locale.getLanguage();
        // }
        // return "result"; // + locale.getLanguage(); .html?lang=pt-br
        response.sendRedirect("/password_reset?success");
    }
    
    private void sendResetPasswordEmail(User user, String resetPasswordToken) throws Exception {
        String subject = "Redefinição de Senha";
        String username = user.getUsername();
        String fullname = MessageFormat.format("{0} {1}", user.getFirstName(), user.getLastName());
        
        String greeting = MessageFormat.format("Olá {0}!", fullname);
        String passwordResetURL = new URIBuilder(MessageFormat.format("{0}/password_reset", hostname))
                                    .addParameter("username", username)
                                    .addParameter("token", resetPasswordToken)
                                    .toString();
        
        String content = mailContentBuilder.build(greeting, passwordResetURL);

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setFrom("redefinicao@ottimizza.com.br");
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(content, true);
        };
        
//        mailSender.send(messagePreparator);
        mailServices.send(user.getEmail(), subject, content); 

    }

}