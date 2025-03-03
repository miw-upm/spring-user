package es.upm.api.infrastructure.postgres.entities;

import es.upm.api.domain.model.Scope;
import es.upm.api.domain.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "betcaUser") // conflict with user table
public class UserEntity {
    @Id
    @GeneratedValue
    private int id;
    @Column(unique = true, nullable = false)
    private String mobile;
    private String firstName;
    private String familyName;
    private String email;
    private String dni;
    private String address;
    private String password;
    @Enumerated(EnumType.STRING)
    private Scope scope;
    private LocalDateTime registrationDate;
    private Boolean active;

    public UserEntity(User user) {
        BeanUtils.copyProperties(user, this);
    }

    public User toUser() {
        User user = new User();
        BeanUtils.copyProperties(this, user);
        return user;
    }
}
