package es.upm.api.functionaltests;

import es.upm.api.data.entities.Scope;
import es.upm.api.resources.view.UserDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static es.upm.api.resources.UserResource.*;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserResourceFunctionalTest {
    private final TestRestTemplate testRestTemplate;
    private final HttpEntityFactory httpEntityFactory;

    @Autowired
    UserResourceFunctionalTest(@Value("${miw.oauth2.api-client-id}") String apiClientId, @Value("${miw.oauth2.api-client-secret}") String apiClientSecret, TestRestTemplate testRestTemplate) {
        this.testRestTemplate = testRestTemplate;
        this.httpEntityFactory = new HttpEntityFactory(testRestTemplate, apiClientId, apiClientSecret);
    }

    @Test
    void testReadUser() {
        ResponseEntity<UserDto> response = testRestTemplate.exchange(USERS + MOBILE_ID, HttpMethod.GET, this.httpEntityFactory.create(Scope.ADMIN), UserDto.class, "6");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFirstName()).isEqualTo("admin");
    }

    @Test
    void testReadUserNotFound() {
        ResponseEntity<UserDto> response = testRestTemplate.exchange(USERS + MOBILE_ID, HttpMethod.GET, httpEntityFactory.create(Scope.ADMIN), UserDto.class, "666000666");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testReadUserUnauthorized() {
        ResponseEntity<UserDto> response = testRestTemplate.exchange(USERS + MOBILE_ID, HttpMethod.GET, httpEntityFactory.create(), UserDto.class, "6");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testCreateUserWithAdmin() {
        UserDto userDto = UserDto.builder().mobile("666001666").firstName("daemon").build();
        ResponseEntity<Void> response = testRestTemplate.exchange(USERS, HttpMethod.POST, httpEntityFactory.create(userDto, Scope.ADMIN), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testCreateUserConflict() {
        UserDto userDto = UserDto.builder().mobile("666666000").firstName("daemon").build();
        ResponseEntity<Void> response = testRestTemplate.exchange(USERS, HttpMethod.POST, httpEntityFactory.create(userDto, Scope.ADMIN), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void testCreateUserBadNumber() {
        UserDto userDto = UserDto.builder().mobile("1").firstName("daemon").build();
        ResponseEntity<Void> response = testRestTemplate.exchange(USERS, HttpMethod.POST, httpEntityFactory.create(userDto, Scope.ADMIN), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCreateUserWithoutNumber() {
        UserDto userDto = UserDto.builder().mobile(null).firstName("daemon").build();
        ResponseEntity<Void> response = testRestTemplate.exchange(USERS, HttpMethod.POST, httpEntityFactory.create(userDto, Scope.ADMIN), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testReadOperator() {
        ResponseEntity<UserDto[]> response = testRestTemplate.exchange(USERS, HttpMethod.GET, httpEntityFactory.create(Scope.OPERATOR), UserDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Arrays.stream(response.getBody()).map(UserDto::getFirstName).toList())
                .contains("c1", "c2")
                .doesNotContain("man", "admin");
    }

    @Test
    void testSearch() {
        ResponseEntity<UserDto[]> response = testRestTemplate.exchange(USERS + SEARCH + "?dni=c", HttpMethod.GET, httpEntityFactory.create(Scope.MANAGER), UserDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Arrays.stream(response.getBody()).map(UserDto::getFirstName).toList())
                .contains("man")
                .doesNotContain("ope", "admin");
    }

    @Test
    void testSearchDoesNotContainNull() {
        ResponseEntity<String> response = testRestTemplate.exchange(USERS + SEARCH, HttpMethod.GET, httpEntityFactory.create(Scope.MANAGER), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).doesNotContain("null");
        log.debug("json: {}", response.getBody());
    }

}
