package es.upm.api.domain.services;

import es.upm.api.domain.model.Scope;
import es.upm.api.domain.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface UserPersistence {
    Optional<User> readByMobile(String mobile);

    Stream<User> findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe
            (String mobile, String firstName, String familyName, String email, String dni, List<Scope> roles);

    void create(User user);

    Stream<User> findByScopeIn(List<Scope> roles);
}

