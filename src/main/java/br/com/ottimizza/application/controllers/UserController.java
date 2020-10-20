package br.com.ottimizza.application.controllers;

import java.security.Principal;

import javax.inject.Inject;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.ottimizza.application.domain.exceptions.users.UserNotFoundException;
import br.com.ottimizza.application.domain.responses.ErrorResponse;
import br.com.ottimizza.application.model.user.User;
// Repositories
import br.com.ottimizza.application.repositories.PasswordRecoveryRepository;
import br.com.ottimizza.application.repositories.users.UsersRepository;
import br.com.ottimizza.application.services.SecurityService;
import br.com.ottimizza.application.services.UserService;

@RestController //@formatter:off 
@RequestMapping(value = "/user")
public class UserController {

    @Inject
    SecurityService securityService;

    @Inject
    UsersRepository userRepository;

    @Inject
    PasswordRecoveryRepository passwordRecoveryRepository;

    
    @Inject
    UserService userService;

    @RequestMapping("/{username}")
    public HttpEntity<?> findByUsername(@PathVariable("username") String username, Principal principal) {
        try {
            User authorizedUser = userService.findByUsername(principal.getName());
            return ResponseEntity.ok(userService.findByUsername(username));
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("user_not_found", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("internal_server_error", "Something wrong happened."));
        }
    }

    @RequestMapping("/info")
    public Principal getCurrentLoggedInUser(Principal user) {
        return user;
    }

    @RequestMapping(value = "/info/username", method = RequestMethod.GET)
    public ResponseEntity<String> save(Principal principal) {
        return ResponseEntity.ok(principal.getName());
    }

    @RequestMapping(value = { "", "/" }, method = RequestMethod.POST)
    public ResponseEntity<User> save(@RequestBody User user, Principal principal) {

        User current = userRepository.findByUsername(user.getUsername()).orElse(null);

        if (current != null) {
            user.setId(current.getId());
        }

        // Encrypts the Password
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));

        // Persists the User.
        user = userRepository.save(user);

        return ResponseEntity.ok(user);
    }

}
