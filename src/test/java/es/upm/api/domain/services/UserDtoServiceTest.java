package es.upm.api.domain.services;

import es.upm.api.TestConfig;
import es.upm.api.data.entities.User;
import es.upm.api.services.exceptions.ForbiddenException;
import es.upm.api.data.entities.Scope;
import es.upm.api.resources.view.UserDto;
import es.upm.api.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertThrows;

@TestConfig
class UserDtoServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testCreateUserForbidden() {
        User userDto = User.builder().mobile("666000666").firstName("k").scope(Scope.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.createUser(userDto, Scope.MANAGER));
    }
}
