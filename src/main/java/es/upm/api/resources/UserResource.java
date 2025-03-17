package es.upm.api.resources;

import es.upm.api.data.entities.Scope;
import es.upm.api.resources.view.UserDto;
import es.upm.api.services.UserService;
import es.upm.api.services.exceptions.BadRequestException;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.stream.Stream;

@Log4j2
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RestController
@RequestMapping(UserResource.USERS)
public class UserResource {
    public static final String USERS = "/users";
    public static final String MOBILE_ID = "/{mobile}";
    public static final String SEARCH = "/search";
    private final UserService userService;

    @Autowired
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/debug-auth")
    public String debugAuth(Principal principal) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Principal: {}", principal);
        log.info("Authorities: {}", authentication.getAuthorities());
        return "Check logs for authorities";
    }

    @PostMapping
    public void createUser(@Valid @RequestBody UserDto creationUserDto) {
        creationUserDto.doDefault();
        this.userService.createUser(creationUserDto.toUser(), this.extractRoleClaims());
    }

    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR + " or " + Security.CUSTOMER_OWNER)
    @GetMapping(MOBILE_ID)
    public UserDto readUser(@PathVariable String mobile) {
        return new UserDto(this.userService.read(mobile));
    }


    @GetMapping
    public Stream<UserDto> readAll() {
        return this.userService.readAll(this.extractRoleClaims())
                .map(UserDto::new)
                .map(UserDto::ofMobileFirstName);
    }

    @GetMapping(value = SEARCH)
    public Stream<UserDto> findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String familyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String dni) {
        return this.userService.findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
                        mobile, firstName, familyName, email, dni, this.extractRoleClaims())
                .map(UserDto::new)
                .map(UserDto::ofMobileFirstName);
    }

    private Scope extractRoleClaims() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(Scope::of)
                .orElseThrow(() -> new BadRequestException("Don't has scope"));
    }

}
