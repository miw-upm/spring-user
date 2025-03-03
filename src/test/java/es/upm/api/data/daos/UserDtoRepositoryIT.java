package es.upm.api.data.daos;


import es.upm.api.TestConfig;
import es.upm.api.data.entities.Scope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static es.upm.api.data.entities.Scope.*;
import static org.assertj.core.api.Assertions.assertThat;

@TestConfig
class UserDtoRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByMobile() {
        assertThat(this.userRepository.findByMobile("6")).isPresent();
    }

    @Test
    void testFindByRoleIn() {
        List<Scope> roles = List.of(ADMIN, MANAGER);
        assertThat(this.userRepository.findByScopeIn(roles))
                .isNotEmpty()
                .allMatch(user -> roles.contains(user.getScope()));
    }

    @Test
    void testFindByMobileAndFirstNameAndFamilyNameAndEmailAndDniNullSafeWithMobile() {
        assertThat(this.userRepository.findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
                "1", null, null, ".com", null, List.of(MANAGER)))
                .anyMatch(user -> "666666001".equals(user.getMobile()));
    }

    @Test
    void testFindByMobileAndFirstNameAndFamilyNameAndEmailAndDniNullSafeWithDni() {
        assertThat(this.userRepository.findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
                null, null, null, null, "kk", List.of(ADMIN, MANAGER, OPERATOR, CUSTOMER)))
                .isEmpty();
    }
}
