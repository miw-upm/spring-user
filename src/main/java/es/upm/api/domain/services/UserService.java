package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.ConflictException;
import es.upm.api.domain.exceptions.ForbiddenException;
import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.Scope;
import es.upm.api.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
public class UserService {

    private final UserPersistence userPersistence;

    @Autowired
    public UserService(UserPersistence userPersistence) {
        this.userPersistence = userPersistence;
    }

    public void createUser(User user, Scope roleClaim) {
        if (!authorizedRoles(roleClaim).contains(user.getScope())) {
            throw new ForbiddenException("Insufficient role to create this user: " + user);
        }
        this.assertNoExistByMobile(user.getMobile());
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.setRegistrationDate(LocalDateTime.now());
        this.userPersistence.create(user);
    }

    public Stream<User> readAll(Scope roleClaim) {
        return this.userPersistence.findByScopeIn(authorizedRoles(roleClaim));
    }

    private List<Scope> authorizedRoles(Scope roleClaim) {
        if (Scope.ADMIN.equals(roleClaim)) {
            return List.of(Scope.ADMIN, Scope.MANAGER, Scope.OPERATOR, Scope.CUSTOMER);
        } else if (Scope.MANAGER.equals(roleClaim)) {
            return List.of(Scope.MANAGER, Scope.OPERATOR, Scope.CUSTOMER);
        } else if (Scope.OPERATOR.equals(roleClaim)) {
            return List.of(Scope.CUSTOMER);
        } else {
            return List.of();
        }
    }

    private void assertNoExistByMobile(String mobile) {
        if (this.userPersistence.readByMobile(mobile).isPresent()) {
            throw new ConflictException("The mobile already exists: " + mobile);
        }
    }

    public Stream<User> findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
            String mobile, String firstName, String familyName, String email, String dni, Scope roleClaim) {
        return this.userPersistence.findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
                mobile, firstName, familyName, email, dni, this.authorizedRoles(roleClaim)
        );
    }

    public User findByMobileAssured(String mobile) {
        return this.userPersistence.readByMobile(mobile)
                .orElseThrow(() -> new NotFoundException("The mobile don't exist: " + mobile));
    }
}
