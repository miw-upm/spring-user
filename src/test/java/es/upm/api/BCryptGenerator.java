package es.upm.api;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("my-secret");
        System.out.println("BCrypt Hashed Client Secret: " + hashedPassword);
    }
}
