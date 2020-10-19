package br.com.ottimizza.application.controllers;

import java.security.Principal;

import javax.inject.Inject;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.ottimizza.application.domain.dtos.UserDTO;
import br.com.ottimizza.application.domain.exceptions.users.UserNotFoundException;
import br.com.ottimizza.application.domain.responses.ErrorResponse;
import br.com.ottimizza.application.domain.responses.GenericPageableResponse;
import br.com.ottimizza.application.services.OrganizationService;
import br.com.ottimizza.application.services.UserService;

@RestController
@RequestMapping(value = "/api/v1/customers")
public class CustomersController {

    @Inject
    UserService userService;

    @Inject
    OrganizationService organizationService;

    @GetMapping
    public HttpEntity<?> fetchCustomers(@RequestParam(name = "email", defaultValue = "") String email,
            @RequestParam(name = "page_index", defaultValue = "0") int pageIndex,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize, Principal principal) {
        try {
            GenericPageableResponse<UserDTO> response = new GenericPageableResponse<UserDTO>(
                    userService.fetchCustomers(email, pageIndex, pageSize, principal));
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("user_not_found", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("illegal_arguments", "Illegal arguments."));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("internal_server_error", "Something wrong happened."));
        }
    }

}
