package es.upm.api.services;

import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.User;
import es.upm.api.services.exceptions.ConflictException;
import es.upm.api.services.exceptions.ForbiddenException;
import es.upm.api.services.exceptions.NotFoundException;
import es.upm.api.data.entities.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createUser(User user, Scope roleClaim) {
        if (!authorizedRoles(roleClaim).contains(user.getScope())) {
            throw new ForbiddenException("Insufficient role to create this userDto: " + user);
        }
        this.assertNoExistByMobile(user.getMobile());
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.setRegistrationDate(LocalDateTime.now());
        this.userRepository.save(user);
    }

    public Stream<User> readAll(Scope roleClaim) {
        return this.userRepository.findByScopeIn(authorizedRoles(roleClaim)).stream();
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
        if (this.userRepository.findByMobile(mobile).isPresent()) {
            throw new ConflictException("The mobile already exists: " + mobile);
        }
    }

    public Stream<User> findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
            String mobile, String firstName, String familyName, String email, String dni, Scope roleClaim) {
        return this.userRepository.findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
                mobile, firstName, familyName, email, dni, this.authorizedRoles(roleClaim)
        ).stream();
    }

    public User findByMobileAssured(String mobile) {
        return this.userRepository.findByMobile(mobile)
                .orElseThrow(() -> new NotFoundException("The mobile don't exist: " + mobile));
    }
}
