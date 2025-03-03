package es.upm.api.domain.services;

import es.upm.api.TestConfig;
import es.upm.api.domain.exceptions.ForbiddenException;
import es.upm.api.domain.model.Scope;
import es.upm.api.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertThrows;

@TestConfig
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testCreateUserForbidden() {
        User user = User.builder().mobile("666000666").firstName("k").scope(Scope.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.createUser(user, Scope.MANAGER));
    }
}
