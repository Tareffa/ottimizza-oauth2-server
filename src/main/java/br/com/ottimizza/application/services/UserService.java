package br.com.ottimizza.application.services;

import java.math.BigInteger;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.ottimizza.application.client.ReceitaWSClient;
import br.com.ottimizza.application.domain.dtos.AdditionalInformationDTO;
import br.com.ottimizza.application.domain.dtos.ImportDataModel;
import br.com.ottimizza.application.domain.dtos.OrganizationDTO;
import br.com.ottimizza.application.domain.dtos.UserDTO;
import br.com.ottimizza.application.domain.dtos.UserShortDTO;
import br.com.ottimizza.application.domain.dtos.criterias.SearchCriteria;
import br.com.ottimizza.application.domain.exceptions.OrganizationAlreadyRegisteredException;
import br.com.ottimizza.application.domain.exceptions.OrganizationNotFoundException;
import br.com.ottimizza.application.domain.exceptions.UserAlreadyRegisteredException;
import br.com.ottimizza.application.domain.exceptions.users.UserNotFoundException;
import br.com.ottimizza.application.model.Organization;
import br.com.ottimizza.application.model.user.User;
import br.com.ottimizza.application.model.user.UserAuthorities;
import br.com.ottimizza.application.model.user.UserProducts;
import br.com.ottimizza.application.model.user_organization.UserOrganization;
import br.com.ottimizza.application.model.user_organization.UserOrganizationID;
import br.com.ottimizza.application.model.user_organization.UserOrganizationInvite;
import br.com.ottimizza.application.repositories.UserOrganizationInviteRepository;
import br.com.ottimizza.application.repositories.UserProductsRepository;
import br.com.ottimizza.application.repositories.organizations.OrganizationRepository;
import br.com.ottimizza.application.repositories.users.UsersRepository;
import br.com.ottimizza.application.repositories.users_organizations.UserOrganizationRepository;

@Service
public class UserService {

    @Autowired
    ReceitaWSClient receitaWSClient;

    @Inject
    MailServices mailServices;

    @Inject
    UsersRepository userRepository;

    @Inject
    UserOrganizationRepository userOrganizationRepository;

    @Inject
    UserOrganizationInviteRepository userOrganizationInviteRepository;

    @Inject
    OrganizationRepository organizationRepository;
    
    @Inject
    UserProductsRepository userProductsRepository;

    @Value("${accountingDepartments}")
    private String DEPARTMENTS;

    @Value("${roles}")
    private String ROLES;
    
    @Value("${application-id}")
    private String APPLICATION_ID;

    public User findById(BigInteger id) throws UserNotFoundException, Exception {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    public User findByUsername(String username) throws UserNotFoundException, Exception {
        return userRepository.findByUsername(username.trim()).orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    public Page<UserDTO> fetchAll(UserDTO filter, SearchCriteria searchCriteria, Principal principal)
            throws UserNotFoundException, Exception {
        User authorizedUser = findByUsername(principal.getName());

        if (authorizedUser.getType().equals(User.Type.CUSTOMER)) {
            return userRepository.fetchAllCustomers(
                filter, UserDTO.getPageRequest(searchCriteria), authorizedUser)
                    .map(UserDTO::fromEntityWithOrganization);
        }
        filter.setOrganizationId(authorizedUser.getOrganization().getId());
        return userRepository.fetchAll(filter, UserDTO.getPageRequest(searchCriteria), authorizedUser)
                .map(UserDTO::fromEntityWithOrganization);
    }

    public User create(User user, Principal principal) throws OrganizationNotFoundException, UserAlreadyRegisteredException, Exception {
        user.setUpdatedBy(principal.getName());
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        checkIfEmailIsAlreadyRegistered(user);
        User usuario = userRepository.save(user);
        if(APPLICATION_ID.equals("tareffa")) {
        	userProductsRepository.saveUserProductsTareffa(usuario.getId());
        	
        }
        
        return usuario;
    }

    public UserDTO create(UserDTO userDTO, Principal principal) // @formatter:off
            throws OrganizationNotFoundException, UserAlreadyRegisteredException, Exception {
        User authorizedUser = findByUsername(principal.getName());
        userDTO.setUpdatedBy(principal.getName());
        User user = userDTO.toEntity(); 
        if (authorizedUser.getType().equals(User.Type.ADMINISTRATOR)) {
            Organization accounting = this.findAccounting(userDTO);
            if (accounting == null) {
                user.setType(User.Type.ADMINISTRATOR);
                user.setOrganization(authorizedUser.getOrganization());
            } else {
                user.setOrganization(accounting);
            }
            return UserDTO.fromEntity(create(user, principal));
        } else {
            user.setOrganization(authorizedUser.getOrganization());
        }
        return UserDTO.fromEntity(create(user, principal));
    }

    public UserDTO upsert(UserDTO userDTO, Principal principal) throws UserNotFoundException, Exception {
        User authorizedUser = findByUsername(principal.getName());
        userDTO.setUpdatedBy(principal.getName());
        if (userRepository.emailIsAlreadyRegistered(userDTO.getUsername().trim())) {
            return UserDTO.fromEntity(userRepository.save(userDTO.patch(findByUsername(userDTO.getUsername()))));
        } else {
            User user = userDTO.toEntity(); 
            if (authorizedUser.getType().equals(User.Type.ADMINISTRATOR)) {
                Organization accounting = this.findAccounting(userDTO);
                if (accounting == null) {
                    user.setType(User.Type.ADMINISTRATOR);
                    user.setOrganization(authorizedUser.getOrganization());
                }
                return UserDTO.fromEntity(create(user, principal));
            } else {
                user.setOrganization(authorizedUser.getOrganization());
            }
            return UserDTO.fromEntity(userRepository.save(user));
        }
    }

    public UserDTO patch(BigInteger id, UserDTO userDTO, Principal principal)
            throws OrganizationNotFoundException, OrganizationAlreadyRegisteredException, Exception {
        userDTO.setUpdatedBy(principal.getName());
        User current = findById(id);
        current = userDTO.patch(current);
        checkIfEmailIsAlreadyRegistered(current.getEmail().trim(), current);
        return UserDTO.fromEntity(userRepository.save(current));
    }

    // @formatter:off
    public Page<UserDTO> findAllByAccountingId(String filter, int pageIndex, int pageSize, Principal principal)
            throws UserNotFoundException, Exception {
        User authorizedUser = findByUsername(principal.getName());
        return userRepository.findAllByAccountingId(
            "%" + filter + "%", authorizedUser.getOrganization().getId(), PageRequest.of(pageIndex, pageSize)
        ).map(UserDTO::fromEntity);
    } // @formatter:on

    // @formatter:off
    public Page<UserDTO> fetchCustomers(String email, int pageIndex, int pageSize, Principal principal)
            throws UserNotFoundException, Exception {
        User authorizedUser = findByUsername(principal.getName());
        return userRepository.findAllByEmailAndTypeAndAccountingId(
            "%" + email + "%", 2, authorizedUser.getOrganization().getId(), PageRequest.of(pageIndex, pageSize)
        ).map(UserDTO::fromEntity);
    } // @formatter:on

    public OrganizationDTO appendOrganization(BigInteger id, OrganizationDTO organizationDTO, Principal principal)
            throws OrganizationNotFoundException, Exception {
        User authorizedUser = findByUsername(principal.getName());
        User user = findById(id);
        Organization organization = null;

        BigInteger organizationId = organizationDTO.getId();
        String organizationCnpj = organizationDTO.getCnpj();

        if (organizationId != null) {
            organization = Optional.ofNullable(organizationRepository.fetchById(organizationId))
                    .orElseThrow(() -> new OrganizationNotFoundException(
                            "Não foi encontrada nenhuma organização com o ID informado!"));
        } else if (organizationCnpj != null && !organizationCnpj.equals("")) {
            organization = Optional.ofNullable(organizationRepository.fetchByCnpj(organizationCnpj))
                    .orElseThrow(() -> new OrganizationNotFoundException(
                            "Não foi encontrada nenhuma organização com o CNPJ informado!"));
        } else {
            throw new OrganizationNotFoundException("Informe o ID ou o CNPJ da organização!");
        }

        UserOrganization userOrganization = new UserOrganization();
        userOrganization.setId(new UserOrganizationID(user.getId(), organization.getId()));
        userOrganization.setUser(user);
        userOrganization.setOrganization(organization);

        userOrganizationRepository.save(userOrganization);

        return OrganizationDTO.fromEntity(organization);
    }
    
    public OrganizationDTO removeOrganization(BigInteger id, BigInteger organizationId, Principal principal)
            throws OrganizationNotFoundException, Exception {
        User authorizedUser = findByUsername(principal.getName());
        User user = findById(id);

        Organization organization = Optional.ofNullable(organizationRepository.fetchById(organizationId))
                                            .orElseThrow(() -> new OrganizationNotFoundException(
                                            "Não foi encontrada nenhuma organização com o ID informado!"));

        UserOrganization userOrganization = new UserOrganization();
        userOrganization.setId(new UserOrganizationID(user.getId(), organization.getId()));
        userOrganization.setUser(user);
        userOrganization.setOrganization(organization);

        userOrganizationRepository.delete(userOrganization);

        return OrganizationDTO.fromEntity(organization);
    }

    public Page<OrganizationDTO> fetchOrganizations(BigInteger id, OrganizationDTO filter,
            SearchCriteria<OrganizationDTO> searchCriteria, Principal principal)
            throws OrganizationNotFoundException, Exception {
        User authorizedUser = findByUsername(principal.getName());

        if (filter == null)
            filter = new OrganizationDTO();

        // Garante que não terá acesso a dados de outras contabilidades.
        filter.setOrganizationId(authorizedUser.getOrganization().getId());

        return organizationRepository.fetchAllByCustomerId(id, filter, OrganizationDTO.getPageRequest(searchCriteria))
                .map(OrganizationDTO::fromEntity);
    }

    private Organization findAccounting(UserDTO userDTO) throws OrganizationNotFoundException {
        if (userDTO.getOrganizationId() != null) {
            return Optional.ofNullable(organizationRepository.fetchAccountingById(userDTO.getOrganizationId()))
                    .orElseThrow(() -> new OrganizationNotFoundException(
                            "Nenhuma contabilidade encontrada com o ID especificado!"));
        } else if (userDTO.getOrganization() != null) {
            if (userDTO.getOrganization().getId() != null) {
                return Optional
                        .ofNullable(organizationRepository.fetchAccountingById(userDTO.getOrganization().getId()))
                        .orElseThrow(() -> new OrganizationNotFoundException(
                                "Nenhuma contabilidade encontrada com o ID especificado!"));
            }
            if (userDTO.getOrganization().getCnpj() != null && !userDTO.getOrganization().getCnpj().equals("")) {
                return organizationRepository.findAccountingByCnpj(userDTO.getOrganization().getCnpj())
                        .orElseThrow(() -> new OrganizationNotFoundException(
                                "Nenhuma contabilidade encontrada com o CNPJ especificado!"));
            }
        }
        return null;
    }

    //
    // INVITE
    //
    public Map<String, String> invite(Map<String, String> args, User authorizedUser)
            throws OrganizationNotFoundException, Exception {
        if (authorizedUser.getType().equals(User.Type.ACCOUNTANT)) {
            String token = UUID.randomUUID().toString();
            String email = args.getOrDefault("email", "");
            if (email.equals("")) {
                throw new IllegalArgumentException("Email cannot be blank.");
            }
            UserOrganizationInvite invite = new UserOrganizationInvite();
            List<UserOrganizationInvite> invites = userOrganizationInviteRepository.findByEmailAndOrganizationId(email,
                    authorizedUser.getOrganization().getId());
            if (invites.size() == 0) {
                invite.setEmail(email);
                invite.setToken(token);
                invite.setType(User.Type.ACCOUNTANT);
                invite.setOrganization(authorizedUser.getOrganization());
                // saves the token to database.
                invite = userOrganizationInviteRepository.save(invite);
            } else {
                invite = invites.get(0);
            }
            // sends the token to the invited user.
            sendInviteByEmail(invite, authorizedUser);
            args.put("token", token);
        }
        return args;
    }

    @Async
    private void sendInviteByEmail(UserOrganizationInvite invite, User authorizedUser) {
        // @formatter:off
        String accountingName = authorizedUser.getOrganization().getName();
        MailServices.Builder messageBuilder = new MailServices.Builder()
                .withName(accountingName)
                .withTo(invite.getEmail())
                .withCc(authorizedUser.getOrganization().getEmail())
                .withSubject(MessageFormat.format("Conta {0}.", accountingName))
                .withHtml(mailServices.inviteCustomerTemplate(authorizedUser, invite.getToken()));
        mailServices.send(messageBuilder); 
        // @formatter:on
    }

    public Page<UserOrganizationInvite> fetchInvitedUsers(String email, int pageIndex, int pageSize,
            Principal principal) throws UserNotFoundException, Exception {
        User authorizedUser = findByUsername(principal.getName());
        return userOrganizationInviteRepository.fetchInvitedUsersByAccountingId(email,
                authorizedUser.getOrganization().getId(), PageRequest.of(pageIndex, pageSize));
    } // @formatter:on

    // public String importUsersFromCSV(MultipartFile file, Principal principal)
    // throws Exception {
    // InputStream is = file.getInputStream();

    // BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    // while (reader.ready()) {
    // String line = reader.readLine();

    // //

    // }

    // return "{}";
    // }
    public String importUsersFromJSON(List<ImportDataModel> data, Principal principal) throws Exception { // @formatter:off
        User authorizedUser = findByUsername(principal.getName());

        Date lastCallToAPI = null;

        Organization lastAccounting = new Organization();
        Organization lastOrganization = new Organization();
        System.out.println("***************************");
        System.out.println("*  Importação de Usuários *");
        System.out.println("***************************");

        for (ImportDataModel object : data) {

            System.out.println("\n**********************");
            System.out.println("First Name ..........: " + object.getFirstName());
            System.out.println("E-mail ..............: " + object.getEmail());
            System.out.println("Company Name ........: " + object.getOrganizationName());
            System.out.println("Company CNPJ ........: " + object.getOrganizationCnpj().replaceAll("\\D", ""));
            System.out.println("Accounting CNPJ .....: " + object.getAccountingName().replaceAll("\\D", ""));
            System.out.println("Accounting CNPJ .....: " + object.getAccountingCnpj().replaceAll("\\D", ""));
            System.out.println("**********************");
            
            Organization accounting = Organization.builder()
                    .name(object.getAccountingName())
                    .cnpj(object.getAccountingCnpj().replaceAll("\\D", ""))
                    .type(Organization.Type.ACCOUNTING).build();
                    
            Organization organization = Organization.builder()
                    .name(object.getOrganizationName())
                    .cnpj(object.getOrganizationCnpj().replaceAll("\\D", ""))
                    .codigoERP(object.getOrganizationCode())
                    .organization(accounting)
                    .type(Organization.Type.CLIENT).build();

            if (!organization.getCnpj().equals(lastOrganization.getCnpj())) {
                if (!accounting.getCnpj().equals(lastAccounting.getCnpj())) {
                    
                    accounting = organizationRepository
                                    .findAccountingByCnpj(accounting.getCnpj())
                                    .orElse(accounting);
                                    
                    if (accounting.getId() == null) {
                        // tenta buscar dados da receita, se obtiver sucesso, 
                        // substitui os dados encontrados e cria um usuário admin 
                        
                        // realiza calculo para fazer requests a cada 20s, para não estourar limite de uso 3 req/min 
                        //long timeout = lastCallToAPI == null ? 0 : 20000 - (new Date().getTime() - lastCallToAPI.getTime());

                        try {
                            // se timeout > 0, pausa o processo pelo tempo determinado em milisegundos..
                            //TimeUnit.MILLISECONDS.sleep(timeout);
                            //DadosReceitaWS info = receitaWSClient.getInfo(accounting.getCnpj()).getBody();
                            //lastCallToAPI = new Date();
                           /* if (info.getEmail() != null && !info.getEmail().isEmpty()) {
                            	accounting.setEmail(info.getEmail());
                            } else {
                                accounting.setEmail(MessageFormat.format("c{0}@ottimizza.com.br", accounting.getCnpj()));
                            }

                            if (info.getNome() != null && !info.getNome().isEmpty()) {
                                accounting.setName(info.getNome());
                            }
                            */
                            if (object.getEmail() != null && !object.getEmail().isEmpty()) {
                            	accounting.setEmail(object.getEmail());
                            } else {
                                accounting.setEmail(MessageFormat.format("c{0}@ottimizza.com.br", accounting.getCnpj()));
                            }

                            if (object.getOrganizationName() != null && !object.getOrganizationName().isEmpty()) {
                                accounting.setName(object.getOrganizationName());
                            }
                        } catch (Exception e) {
                            accounting.setEmail(MessageFormat.format("c{0}@ottimizza.com.br", accounting.getCnpj()));
                        }
                        accounting = organizationRepository.save(accounting);

                        User accountant = User.builder()
                            .firstName(accounting.getName())
                            .email(accounting.getEmail())
                            .username(accounting.getEmail())
                            .password("ottimizza")
                            .type(User.Type.ACCOUNTANT)
                            .organization(accounting).build();

                        accountant = create(accountant, principal);
                    } else {
                        System.out.println("\n --- Contabilidade --- ");
                        System.out.println(MessageFormat.format(" Id: {0} ", accounting.getId()));
                        System.out.println(MessageFormat.format(" Nome: {0} ", accounting.getName()));
                    }

                } else {
                    accounting = lastAccounting;
                }
                
                if (organization.getCnpj() != null && !organization.getCnpj().equals("")) {
                    organization.setOrganization(accounting);
                    organization = organizationRepository.findOrganizationByCnpjAndAccountingId(
                        organization.getCnpj(), accounting.getId()
                    ).orElse(organization);
                    if (organization.getId() == null) {
                        System.out.println("\n\t --- Nova Empresa --- ");
                        System.out.println(MessageFormat.format("\t Nome: {0} ", organization.getName()));

                        organization = organizationRepository.save(organization);
                    } else {
                        System.out.println("\n\t --- Epresa --- ");
                        System.out.println(MessageFormat.format("\t Id: {0} ", organization.getId()));
                        System.out.println(MessageFormat.format("\t Nome: {0} ", organization.getName()));
                    }
                }
            } else {
                organization = lastOrganization;
                accounting = lastAccounting;
            }
            
            List<String> emails = Arrays.asList(object.getEmail());
            if (object.getEmail().contains(",")) {
                emails = Arrays.asList(object.getEmail().split(","));
            }
            
            for (String email : emails) {
                email = email.toLowerCase().trim();

                if (email == null || email.isEmpty()) {
                    continue;
                }

                User user = User.builder()
                    .firstName(email)
                    .username(email)
                    .email(email)
                    .organization(accounting)
                    .type(
                        organization.getCnpj() == null || organization.getCnpj().equals("") 
                            ? User.Type.ACCOUNTANT : User.Type.CUSTOMER
                    ).build();

                System.out.println("\n\t\t --- Novo usuário --- ");
                System.out.println(MessageFormat.format("\t\t E-mail: {0} ", email));

                try {
                    user.setId(null);
                    user.setFirstName(email);
                    user.setEmail(email);
                    user.setUsername(email);

                    // verificando se o usuario já está registrado no sistema
                    if (userRepository.emailIsAlreadyRegistered(email.trim())) {
                        User existing = findByUsername(email);

                        // se usuário existe mas não está vinculado a nenhuma contabilidade
                        if (existing.getOrganization() == null) {
                            user = userRepository.save(
                                UserDTO.fromEntity(user).patch(existing)
                                    .toBuilder()
                                        .email(email)
                                        .username(email)
                                        .type(user.getType())
                                        .organization(accounting)
                                    .build()
                            );
                        } else if (existing.getOrganization().equals(user.getOrganization())) { 
                            user = userRepository.save(
                                UserDTO.fromEntity(user).patch(existing)
                                    .toBuilder()
                                        .email(email)
                                        .username(email)
                                        .type(user.getType())
                                    .build()
                            );
                        } else {
                            throw new Exception("");
                        } 
                        if (user.getType().equals(User.Type.CUSTOMER)) {
                            try { userRepository.addOrganization(user.getId(), organization.getId());
                            } catch (Exception e) {}
                        }   
                    } else {
                        // caso não exista, cria um novo usuário.
                        user.setPassword(object.getPassword());
                        user = create(user, principal);

                        if (user.getType().equals(User.Type.CUSTOMER)) {
                            try { userRepository.addOrganization(user.getId(), organization.getId());
                            } catch (Exception e) {}
                        }                        
                    }
                } catch (Exception ex) {
                    System.out.println("\n>>> Exception\n");
                    ex.printStackTrace();
                }

                try {
                    userRepository.addOrganization(user.getId(), organization.getId());
                }catch (Exception e) {}

            }

            lastAccounting = accounting.toBuilder().build();
            lastOrganization = organization.toBuilder().build();
        }

        return "{}";
    }

    //
    //
    //
    //
    //
    //
    public boolean checkIfEmailIsAlreadyRegistered(User user) throws UserAlreadyRegisteredException {
        if (userRepository.emailIsAlreadyRegistered(user.getEmail().trim())) {
            System.out.println("A user with that email address is already registered.");
            throw new UserAlreadyRegisteredException("A user with that email address is already registered.");
        }
        return true;
    }

    public boolean checkIfEmailIsAlreadyRegistered(String email, User user) throws UserAlreadyRegisteredException {
        if (userRepository.emailIsAlreadyRegistered(email.trim(), user.getId())) {
            System.out.println("A user with that email address is already registered.");
            throw new UserAlreadyRegisteredException("A user with that email address is already registered.");
        }
        return true;
    }
    
    public Page<UserShortDTO> fetchUserShortDTO(UserDTO filter, SearchCriteria searchCriteria, Principal principal)
            throws Exception {
    	User authorizedUser = findByUsername(principal.getName());
    	return userRepository.fetchUserShort(filter, UserDTO.getPageRequest(searchCriteria), authorizedUser.getOrganization().getId());
    	
    }
    
    public List<BigInteger> fetchIds(UserDTO filter, Principal principal) throws Exception {
    	User authorizedUser = findByUsername(principal.getName());
    	return userRepository.fetchIds(filter, authorizedUser.getOrganization().getId());
    }
    
    public List<?> fetchAllProducts(String group) throws Exception {
    	return userProductsRepository.fetchAllProducts(group);
    }
    
    public void saveUserProducts(UserProducts userProd) throws Exception {
    	userProductsRepository.saveUserProducts(userProd.getUsersId(), userProd.getProductsId());
    }
    
    public void deleteUserProducts(UserProducts userProd) throws Exception {
    	userProductsRepository.deleteUserProducts(userProd.getUsersId(), userProd.getProductsId());
    }
    
    public void saveUserAuthorities(UserAuthorities userAuthorities) throws Exception {
    	userProductsRepository.saveUserAuhtorities(userAuthorities.getUsersId(), userAuthorities.getAuthoritiesId());
    }
    
    public void deleteUserAuthorities(UserAuthorities userAuthorities) throws Exception {
    	userProductsRepository.deleteUserAuhtorities(userAuthorities.getUsersId(), userAuthorities.getAuthoritiesId());
    }


    public AdditionalInformationDTO getInformations() throws Exception {
        AdditionalInformationDTO informations = new AdditionalInformationDTO();
        informations.setDepartments(Arrays.asList(DEPARTMENTS.split(",").clone()));
        informations.setRoles(Arrays.asList(ROLES.split(",").clone()));
        return informations;
    }
    
}
