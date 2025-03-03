package es.upm.api.infrastructure.postgres.daos;

import es.upm.api.domain.model.Scope;
import es.upm.api.infrastructure.postgres.entities.UserEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@Repository
public class DatabaseStarting {

    private static final String SUPER_USER = "admin";
    private static final String MOBILE = "6";
    private static final String PASSWORD = "6";

    private final UserRepository userRepository;

    @Autowired
    public DatabaseStarting(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.initialize();
    }

    public void initialize() {
        if (this.userRepository.findByScopeIn(List.of(Scope.ADMIN)).isEmpty()) {
            UserEntity user = UserEntity.builder().mobile(MOBILE).firstName(SUPER_USER)
                    .password(new BCryptPasswordEncoder().encode(PASSWORD))
                    .scope(Scope.ADMIN).registrationDate(LocalDateTime.now()).active(true).build();
            this.userRepository.save(user);
            log.warn("------- Created Admin -----------");
        }
    }

}
