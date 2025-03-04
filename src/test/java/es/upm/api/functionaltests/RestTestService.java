package es.upm.api.functionaltests;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;


@Service
public class RestTestService {
    @Value("${miw.oauth2.api-client-id}")
    private String apiClientId;
    @Value("${miw.oauth2.api-client-secret}")
    private String apiClientSecret;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    private String obtainAccessToken(String scope) {
        String accessTokenUrl = "http://localhost:8080/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = apiClientId + ":" + apiClientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", scope);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return Objects.requireNonNull(restTemplate.postForEntity(accessTokenUrl, request, Map.class).getBody())
                .get("access_token").toString();
    }

    public HttpEntity<Void> createHttpEntity(String scope) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.obtainAccessToken(scope));
        return new HttpEntity<>(headers);
    }

    public <T> HttpEntity<T> createHttpEntity(T body, String scope) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.obtainAccessToken(scope));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    public HttpEntity<Void> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        return new HttpEntity<>(headers);
    }

    public HttpEntity<Void> loginAdmin() {
        return this.createHttpEntity("admin");
    }

    public <T> HttpEntity<T> loginAdmin(T body) {
        return this.createHttpEntity(body, "admin");
    }

    public HttpEntity<Void> loginManager() {
        return this.createHttpEntity("manager");
    }

    public HttpEntity<Void> loginOperator() {
        return this.createHttpEntity("operator");
    }

    public HttpEntity<Void> loginCustomer() {
        return this.createHttpEntity("customer");
    }

}
