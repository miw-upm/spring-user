package es.upm.api.infrastructure.resources;


import es.upm.api.domain.model.Scope;
import es.upm.api.domain.model.User;
import es.upm.api.domain.services.UserService;
import es.upm.api.infrastructure.postgres.daos.UserRepository;
import es.upm.api.infrastructure.postgres.entities.UserEntity;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

@Log4j2

@RestController
@RequestMapping(UserResource.USERS)
public class UserResource {
    public static final String USERS = "/users";
    public static final String TOKEN = "/token";
    public static final String MOBILE_ID = "/{mobile}";
    public static final String SEARCH = "/search";
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserResource(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public void createUser(@Valid @RequestBody User creationUser) {
        creationUser.doDefault();
        this.userService.createUser(creationUser, this.extractRoleClaims());
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(MOBILE_ID)
    public User readUser(@PathVariable String mobile) {
        return this.userService.findByMobileAssured(mobile);
    }

    @PreAuthorize("hasAuthority('SCOPE_admin')")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public Stream<User> readAll() {
       return this.userService.readAll(this.extractRoleClaims()).map(User::ofMobileFirstName);
    }

    @SecurityRequirement(name = "bearerAuth")

    @GetMapping(value = SEARCH)
    public Stream<User> findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String familyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String dni) {
        return this.userService.findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
                mobile, firstName, familyName, email, dni, this.extractRoleClaims()
        ).map(User::ofMobileFirstName);
    }

    private Scope extractRoleClaims() {
        List<String> roleClaims = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
        return Scope.of(roleClaims.getFirst());
    }

}
