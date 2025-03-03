package es.upm.api.functionaltests;

import es.upm.api.resources.view.UserDto;
import es.upm.api.resources.view.TokenDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static es.upm.api.resources.UserResource.*;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@RestTestConfig
class UserDtoResourceFunctionalTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RestTestService restTestService;

    private String baseUrl;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port + USERS;
    }

    @Test
    void testLogin() {
        ResponseEntity<TokenDto> response = restTemplate.postForEntity(this.baseUrl + TOKEN, restTestService.basicAuth("6", "6"), TokenDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testReadUser() {
        ResponseEntity<UserDto> response = restTemplate.exchange(baseUrl + MOBILE_ID, HttpMethod.GET, restTestService.loginAdmin(), UserDto.class, "6");
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
