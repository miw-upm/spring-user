package es.upm.api.services;

import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.Scope;
import es.upm.api.data.entities.User;
import es.upm.api.services.exceptions.ConflictException;
import es.upm.api.services.exceptions.ForbiddenException;
import es.upm.api.services.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createUser(User user, Scope scope) {
        if (!authorizedScopes(scope).contains(user.getScope())) {
            throw new ForbiddenException("Insufficient role to create this userDto: " + user);
        }
        this.assertNoExistByMobile(user.getMobile());
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        user.setRegistrationDate(LocalDateTime.now());
        this.userRepository.save(user);
    }

    public Stream<User> readAll(Scope scope) {
        return this.userRepository.findByScopeIn(authorizedScopes(scope)).stream();
    }

    private List<Scope> authorizedScopes(Scope scope) {
        if (Scope.ADMIN.equals(scope)) {
            return List.of(Scope.ADMIN, Scope.MANAGER, Scope.OPERATOR, Scope.CUSTOMER);
        } else if (Scope.MANAGER.equals(scope)) {
            return List.of(Scope.MANAGER, Scope.OPERATOR, Scope.CUSTOMER);
        } else if (Scope.OPERATOR.equals(scope)) {
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
            String mobile, String firstName, String familyName, String email, String dni, Scope scope) {
        return this.userRepository.findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
                mobile, firstName, familyName, email, dni, this.authorizedScopes(scope)
        ).stream();
    }

    public User read(String mobile) {
        return this.userRepository.findByMobile(mobile)
                .orElseThrow(() -> new NotFoundException("The mobile don't exist: " + mobile));
    }
}
