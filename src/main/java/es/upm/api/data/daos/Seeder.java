package es.upm.api.data.daos;

import es.upm.api.data.entities.Scope;
import es.upm.api.data.entities.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;

@Log4j2
@Repository
@Profile({"dev", "test"})
public class Seeder {

    private final DatabaseStarting databaseStarting;
    private final UserRepository userRepository;

    @Autowired
    public Seeder(UserRepository userRepository, DatabaseStarting databaseStarting) {
        this.userRepository = userRepository;
        this.databaseStarting = databaseStarting;
        this.deleteAllAndInitializeAndSeedDataBase();
    }

    public void deleteAllAndInitializeAndSeedDataBase() {
        this.deleteAllAndInitialize();
        this.seedDataBase();
    }

    public void deleteAllAndInitialize() {
        this.userRepository.deleteAll();
        log.warn("------- Deleted All -----------");
        this.databaseStarting.initialize();
    }

    private void seedDataBase() {
        log.warn("------- Initial Load from JAVA -----------");
        String pass = new BCryptPasswordEncoder().encode("6");
        User[] users = {
                User.builder().mobile("666666000").firstName("adm").password(pass).dni(null).address("C/TPV, 0")
                        .email("adm@gmail.com").scope(Scope.ADMIN).registrationDate(LocalDateTime.now()).active(true)
                        .build(),
                User.builder().mobile("666666001").firstName("man").password(pass).dni("66666601C").address("C/TPV, 1")
                        .email("man@gmail.com").scope(Scope.MANAGER).registrationDate(LocalDateTime.now()).active(true)
                        .build(),
                User.builder().mobile("666666002").firstName("ope").password(pass).dni("66666602K").address("C/TPV, 2")
                        .email("ope@gmail.com").scope(Scope.OPERATOR).registrationDate(LocalDateTime.now()).active(true)
                        .build(),
                User.builder().mobile("666666003").firstName("c1").familyName("ac1").password(pass).dni("66666603E")
                        .address("C/TPV, 3").email("c1@gmail.com").scope(Scope.CUSTOMER)
                        .registrationDate(LocalDateTime.now()).active(true).build(),
                User.builder().mobile("666666004").firstName("c2").familyName("ac2").password(pass).dni("66666604T")
                        .address("C/TPV, 4").email("c2@gmail.com").scope(Scope.CUSTOMER)
                        .registrationDate(LocalDateTime.now()).active(true).build(),
                User.builder().mobile("666666005").firstName("c3").password(pass).scope(Scope.CUSTOMER)
                        .registrationDate(LocalDateTime.now()).active(true).build(),
                User.builder().mobile("66").firstName("customer").password(pass).scope(Scope.CUSTOMER)
                        .registrationDate(LocalDateTime.now()).active(true).build(),
        };
        this.userRepository.saveAll(Arrays.asList(users));
        log.warn("        ------- users");
    }

}
