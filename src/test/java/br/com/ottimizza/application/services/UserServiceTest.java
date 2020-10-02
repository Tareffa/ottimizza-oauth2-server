
package br.com.ottimizza.application.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import br.com.ottimizza.application.SpringbootOauth2ServerApplication;
import br.com.ottimizza.application.domain.exceptions.PasswordResetTokenInvalidException;
import br.com.ottimizza.application.domain.exceptions.users.UserNotFoundException;
import br.com.ottimizza.application.model.PasswordResetToken;
import br.com.ottimizza.application.model.user.User;
import br.com.ottimizza.application.repositories.users.UsersRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringbootOauth2ServerApplication.class)
class UserServiceTest {

    @Autowired
    private UsersRepository usersRepository;

    @Test
    public void givenGenericEntityRepository_whenSaveAndRetreiveEntity_thenOK() throws Exception {
        System.out.println("asdasdadasdasdadad");
        System.out.println("asdasdadasdasdadad");
        System.out.println("asdasdadasdasdadad");
        System.out.println("asdasdadasdasdadad");
        System.out.println("asdasdadasdasdadad");
        User user = new User();
        user.setUsername("test@ottimizza.com.br");

        user = usersRepository.save(user);

        User foundEntity = usersRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        assertNotNull(foundEntity);
    }
}
