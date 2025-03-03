package es.upm.api.data.entities;

import es.upm.api.resources.view.UserDto;
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
public class User {
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

    public User(UserDto userDto) {
        BeanUtils.copyProperties(userDto, this);
    }

    public UserDto toUser() {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(this, userDto);
        return userDto;
    }
}
