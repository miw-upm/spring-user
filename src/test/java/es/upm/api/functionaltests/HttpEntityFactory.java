package es.upm.api.functionaltests;

import es.upm.api.data.entities.Scope;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;


public class HttpEntityFactory {
    private final TestRestTemplate testRestTemplate;
    private final String apiClientId;
    private final String apiClientSecret;

    public HttpEntityFactory(TestRestTemplate testRestTemplate, String apiClientId, String apiClientSecret) {
        this.testRestTemplate = testRestTemplate;
        this.apiClientId = apiClientId;
        this.apiClientSecret = apiClientSecret;
    }

    private String obtainAccessToken(String scope) {
        String accessTokenUrl = "/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = apiClientId + ":" + apiClientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", scope);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return Objects.requireNonNull(testRestTemplate.postForEntity(accessTokenUrl, request, Map.class).getBody())
                .get("access_token").toString();
    }

    private HttpHeaders buildHeaders(String scope) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (scope != null && !scope.isEmpty()) {
            String token = obtainAccessToken(scope);
            headers.setBearerAuth(token);
        }
        return headers;
    }

    public HttpEntity<Void> create(Scope scope) {
        return new HttpEntity<>(buildHeaders(scope.value()));
    }

    public <T> HttpEntity<T> create(T body, Scope scope) {
        return new HttpEntity<>(body, buildHeaders(scope.value()));
    }

    public HttpEntity<Void> create() {
        return new HttpEntity<>(buildHeaders(null));
    }

}
