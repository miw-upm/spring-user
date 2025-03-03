package es.upm.api.functionaltests;

import es.upm.api.resources.view.UserDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.Map;

import static es.upm.api.resources.UserResource.*;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserResourceFunctionalTest {
    @Value("${miw.oauth2.client-id}")
    private String clientId;
    @Value("${miw.oauth2.client-secret}")
    private String clientSecret;
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RestTestService restTestService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String baseUrl;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port + USERS;
    }



    private String obtainAccessToken(String username, String password) {
        String tokenUrl = "http://localhost:" + port + "/oauth2/token";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("username", username);
        params.add("password", password);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("scope", "admin manager operator customer");

        ResponseEntity<Map> response;
        System.out.println(">>>>"+tokenUrl);
        System.out.println(">>>>"+AuthorizationGrantType.PASSWORD.getValue());

        try {
            response = restTemplate.postForEntity(tokenUrl, new HttpEntity<>(params), Map.class);
        } catch (HttpClientErrorException e) {
            System.err.println("❌ Error al obtener el token: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("Error en la autenticación: " + e.getMessage());
        }

        if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
            throw new RuntimeException("❌ No se recibió un access_token válido. Respuesta: " + response);
        }

        return response.getBody().get("access_token").toString();
    }


    @Test
    void testReadUser() {


        String token = obtainAccessToken("6", "6");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);


        ResponseEntity<UserDto> response = restTemplate.exchange(baseUrl + MOBILE_ID, HttpMethod.GET, new HttpEntity<>(headers), UserDto.class, "6");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFirstName()).isEqualTo("admin");
    }

    @Test
    void testReadUserNotFound() {
        ResponseEntity<UserDto> response = restTemplate.exchange(baseUrl + MOBILE_ID, HttpMethod.GET, restTestService.loginAdmin(), UserDto.class, "666000666");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testReadUserUnauthorized() {
        ResponseEntity<UserDto> response = restTemplate.exchange(baseUrl + MOBILE_ID, HttpMethod.GET, restTestService.createHttpEntity(), UserDto.class, "6");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testCreateUserWithAdmin() {
        UserDto userDto = UserDto.builder().mobile("666001666").firstName("daemon").build();
        ResponseEntity<Void> response = restTemplate.exchange(baseUrl, HttpMethod.POST, restTestService.loginAdmin(userDto), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testCreateUserConflict() {
        UserDto userDto = UserDto.builder().mobile("666666000").firstName("daemon").build();
        ResponseEntity<Void> response = restTemplate.exchange(baseUrl, HttpMethod.POST, restTestService.loginAdmin(userDto), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void testCreateUserBadNumber() {
        UserDto userDto = UserDto.builder().mobile("1").firstName("daemon").build();
        ResponseEntity<Void> response = restTemplate.exchange(baseUrl, HttpMethod.POST, restTestService.loginAdmin(userDto), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCreateUserWithoutNumber() {
        UserDto userDto = UserDto.builder().mobile(null).firstName("daemon").build();
        ResponseEntity<Void> response = restTemplate.exchange(baseUrl, HttpMethod.POST, restTestService.loginAdmin(userDto), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testReadOperator() {
        ResponseEntity<UserDto[]> response = restTemplate.exchange(baseUrl, HttpMethod.GET, restTestService.loginOperator(), UserDto[].class);
        System.out.println(Arrays.toString(response.getBody()));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Arrays.stream(response.getBody()).map(UserDto::getFirstName).toList())
                .contains("c1", "c2")
                .doesNotContain("man", "admin");
    }

    @Test
    void testSearch() {
        ResponseEntity<UserDto[]> response = restTemplate.exchange(baseUrl + SEARCH + "?dni=c", HttpMethod.GET, restTestService.loginManager(), UserDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Arrays.stream(response.getBody()).map(UserDto::getFirstName).toList())
                .contains("man")
                .doesNotContain("ope", "admin");
    }

    @Test
    void testSearchDoesNotContainNull() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + SEARCH, HttpMethod.GET, restTestService.loginManager(), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).doesNotContain("null");
        log.debug("json: {}", response.getBody());
    }

}
