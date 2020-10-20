package br.com.ottimizza.application.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.ottimizza.application.domain.exceptions.users.UserNotFoundException;
import br.com.ottimizza.application.domain.responses.GenericResponse;
import br.com.ottimizza.application.model.OAuthClientAdditionalInformation;
import br.com.ottimizza.application.model.OAuthClientDetails;
import br.com.ottimizza.application.model.user.User;
import br.com.ottimizza.application.repositories.clients.OAuthClientRepository;
import br.com.ottimizza.application.repositories.users.UsersRepository;

@Service
public class OAuthService {

    @Inject
    UsersRepository userRepository;

    @Inject
    OAuthClientRepository authClientRepository;

    // @Resource(name = "tokenServices")
    @Resource(name = "tokenServices")
    ConsumerTokenServices tokenServices;

    @Resource(name = "tokenStore")
    TokenStore tokenStore;

    public GenericResponse<OAuthClientDetails> save(OAuthClientAdditionalInformation additionalInformation,
            Principal principal) throws UserNotFoundException {
        User authorizedUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado!"));
        try {
            OAuthClientDetails oauthClientDetails = new OAuthClientDetails();

            String clientId = getRandomHexString(20);
            String clientSecret = getRandomHexString(40);

            additionalInformation.setClientId(clientId);
            additionalInformation.setClientSecret(clientSecret);

            String additionalInfo = new ObjectMapper().writeValueAsString(additionalInformation);

            oauthClientDetails.setClientId(clientId);
            oauthClientDetails.setClientSecret(new BCryptPasswordEncoder().encode(clientSecret));
            oauthClientDetails.setAdditionalInformation(additionalInformation);
            oauthClientDetails.setWebServerRedirectUri(additionalInformation.getRedirectUris());
            OAuthClientDetails registered = authClientRepository.save(oauthClientDetails);

            return new GenericResponse<OAuthClientDetails>(registered);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new GenericResponse<OAuthClientDetails>();
    }

    public Page<OAuthClientDetails> fetchAll(Integer pageIndex, Integer pageSize, Principal principal) {
        return authClientRepository.findAll(PageRequest.of(pageIndex, pageSize));
    }

    /**
     * *********************************************************************************
     * * Access Tokens
     * *********************************************************************************
     */
    public List<String> fetchAccessTokensByClientId(String clientId) {
        List<String> tokenValues = new ArrayList<String>();
        Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientId(clientId);
        if (tokens != null) {
            for (OAuth2AccessToken token : tokens) {
                tokenValues.add(token.getValue());
            }
        }
        return tokenValues;
    }

    private String getRandomHexString(int numchars) {
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while (sb.length() < numchars) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, numchars);
    }

}
