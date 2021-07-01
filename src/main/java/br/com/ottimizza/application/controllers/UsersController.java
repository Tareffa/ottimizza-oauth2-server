package br.com.ottimizza.application.controllers;

import java.math.BigInteger;
import java.security.Principal;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.ottimizza.application.domain.dtos.OrganizationDTO;
import br.com.ottimizza.application.domain.dtos.UserDTO;
import br.com.ottimizza.application.domain.dtos.UserShortDTO;
import br.com.ottimizza.application.domain.dtos.criterias.SearchCriteria;
import br.com.ottimizza.application.domain.responses.GenericPageableResponse;
import br.com.ottimizza.application.domain.responses.GenericResponse;
import br.com.ottimizza.application.model.user.User;
import br.com.ottimizza.application.model.user.UserAuthorities;
import br.com.ottimizza.application.model.user.UserProducts;
import br.com.ottimizza.application.model.user_organization.UserOrganizationInvite;
import br.com.ottimizza.application.services.UserService;
import net.minidev.json.JSONObject;

@RestController // @formatter:off
@RequestMapping(value = "/api/v1/users")
public class UsersController {

    @Inject
    UserService userService;

    @GetMapping 
    public HttpEntity<?> fetchAll(@ModelAttribute UserDTO filter,
                                  @ModelAttribute SearchCriteria criteria, 
                                  Principal principal) throws Exception {
        return ResponseEntity.ok(
            new GenericPageableResponse<UserDTO>(userService.fetchAll(filter, criteria, principal)));
    }
    
    

    @GetMapping("/{id}")
    public HttpEntity<?> fetchById(@PathVariable("id") BigInteger id, Principal principal) throws Exception {
        return ResponseEntity.ok(
            new GenericResponse<UserDTO>(UserDTO.fromEntityWithOrganization(userService.findById(id))));
    }

    @PostMapping 
    public HttpEntity<?> create(@RequestBody UserDTO userDTO, Principal principal) throws Exception {
        return ResponseEntity.ok(new GenericResponse<UserDTO>(userService.create(userDTO, principal)));
    }

    @PatchMapping("/{id}")
    public HttpEntity<?> patch(@PathVariable("id") BigInteger id, @RequestBody UserDTO userDTO, Principal principal)
            throws Exception {
        return ResponseEntity.ok(new GenericResponse<UserDTO>(userService.patch(id, userDTO, principal)));
    }

    /* ********************************************************************************************* *
     *
     * Organizations
     * 
     * ********************************************************************************************* */
    @GetMapping("/{id}/organizations") 
    public HttpEntity<?> fetchOrganization(@PathVariable("id") BigInteger id,
                                           @ModelAttribute SearchCriteria<OrganizationDTO> criteria, Principal principal) throws Exception {
        return ResponseEntity.ok(
                new GenericPageableResponse<OrganizationDTO>(userService.fetchOrganizations(id, criteria.getFilter(), criteria, principal))
        );
    }

    @PostMapping("/{id}/organizations") 
    public HttpEntity<?> appendOrganization(@PathVariable("id") BigInteger id,
            @RequestBody OrganizationDTO organizationDTO, Principal principal) throws Exception {
        return ResponseEntity.ok(
                new GenericResponse<OrganizationDTO>(userService.appendOrganization(id, organizationDTO, principal))
        );
    }

    @DeleteMapping("/{id}/organizations/{organizationId}") 
    public HttpEntity<?> appendOrganization(@PathVariable("id") BigInteger id,
                                            @PathVariable("organizationId") BigInteger organizationId, Principal principal) throws Exception {
        return ResponseEntity.ok(
                new GenericResponse<OrganizationDTO>(userService.removeOrganization(id, organizationId, principal))
        );
    }

    /* ********************************************************************************************* *
     *
     * Invtations
     * 
     * ********************************************************************************************* */
    @GetMapping("/invited")
    public HttpEntity<?> fetchAllInvitedUsers(@ModelAttribute UserDTO filter,
            @RequestParam(name = "page_index", defaultValue = "0") int pageIndex,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize, Principal principal) throws Exception {
        return ResponseEntity.ok(new GenericPageableResponse<UserOrganizationInvite>(
                userService.fetchInvitedUsers(filter.getEmail(), pageIndex, pageSize, principal)));
    }

    @PostMapping("/invite")
    public HttpEntity<?> invite(@RequestBody Map<String, String> args, Principal principal) throws Exception {
        User authorizedUser = userService.findByUsername(principal.getName());
        return ResponseEntity.ok(new GenericResponse<Map<String, String>>(userService.invite(args, authorizedUser)));
    }

    /**
     * 
     * IMPORTAÇÃO
     * 
     */
    @PostMapping("/import")
    public HttpEntity<?> upload(@RequestParam("file") MultipartFile file, Principal principal) throws Exception {
        return ResponseEntity.ok("");
    }
    
    //JD em diante
    
    @GetMapping("/getInfo")
    public HttpEntity<?> fetchUserShortDTO(@ModelAttribute UserDTO filter,
                                  		   @ModelAttribute SearchCriteria criteria, 
                                  		   Principal principal) throws Exception {
        return ResponseEntity.ok(new GenericPageableResponse<UserShortDTO>(userService.fetchUserShortDTO(filter, criteria, principal)));
    }
    
    @GetMapping("/usersIds")
    public HttpEntity<?> fetchIds(@ModelAttribute UserDTO filter, Principal principal) throws Exception{
    	return ResponseEntity.ok(userService.fetchIds(filter, principal));
    }
    
    @GetMapping("/products/{group}")
    public HttpEntity<?> fetchAllProducts(@PathVariable("group") String group) throws Exception {
    	return ResponseEntity.ok(userService.fetchAllProducts(group));
    }
    
    @PostMapping("/products")
    public HttpEntity<?> saveUsersProducts(@RequestBody UserProducts userProducts) throws Exception {
    	JSONObject response = new JSONObject();
    	try{
    		userService.saveUserProducts(userProducts);
    		response.put("status", "success");
    		response.put("record", userProducts);
    		return ResponseEntity.ok(response);
    	}
    	catch(Exception ex) {
    		response.put("status", "error");
    		response.put("message", ex.getMessage());
    		return ResponseEntity.ok(response);
    	}
    }
    
    @DeleteMapping("/products")
    public HttpEntity<?> deleteUsersProducts(@ModelAttribute UserProducts userProducts) throws Exception {
    	JSONObject response = new JSONObject();
    	try{
    		userService.deleteUserProducts(userProducts);
    		response.put("status", "success");
    		response.put("record", userProducts);
    		return ResponseEntity.ok(response);
    	}
    	catch(Exception ex) {
    		response.put("status", "error");
    		response.put("message", ex.getMessage());
    		return ResponseEntity.ok(response);
    	}
    }
    
    @PostMapping("/authorities")
    public HttpEntity<?> saveUsersAuhtorities(@RequestBody UserAuthorities userAuthorities) throws Exception {
    	JSONObject response = new JSONObject();
    	try{
    		userService.saveUserAuthorities(userAuthorities);
    		response.put("status", "success");
    		response.put("record", userAuthorities);
    		return ResponseEntity.ok(response);
    	}
    	catch(Exception ex) {
    		response.put("status", "error");
    		response.put("message", ex.getMessage());
    		return ResponseEntity.ok(response);
    	}
    }
    
    @DeleteMapping("/authorities")
    public HttpEntity<?> deleteUsersAuhtorities(@ModelAttribute UserAuthorities userAuthorities) throws Exception {
    	JSONObject response = new JSONObject();
    	try{
    		userService.deleteUserAuthorities(userAuthorities);
    		response.put("status", "success");
    		response.put("record", userAuthorities);
    		return ResponseEntity.ok(response);
    	}
    	catch(Exception ex) {
    		response.put("status", "error");
    		response.put("message", ex.getMessage());
    		return ResponseEntity.ok(response);
    	}
    }

    @GetMapping("/informations")
    public HttpEntity<?> getInformations() throws Exception {
        return ResponseEntity.ok(new GenericResponse<>(
                userService.getInformations()
        ));
    }

    @GetMapping("/{organizationId}/has_phone")
    public HttpEntity<?> getUserByOrganizationIdAndPhone(@PathVariable BigInteger organizationId) throws Exception {
        return ResponseEntity.ok(new GenericResponse<>(
                userService.getUsersByOrganizationIdAndPhone(organizationId)
        ));
    }

    @GetMapping("/{organizationId}/organization")
    public HttpEntity<?> getUserByOrganizationId(@PathVariable BigInteger organizationId) throws Exception {
        return ResponseEntity.ok(new GenericResponse<>(
                userService.getUsersByOrganizationId(organizationId)
        ));
    }
    
}
