package es.upm.api.functionaltests;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;


@Service
public class RestTestService {



    public HttpEntity<Void> createHttpEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    public <T> HttpEntity<T> createHttpEntity(T body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    public HttpEntity<Void> basicAuth(String user, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(user, password);
        return new HttpEntity<>(headers);
    }

    public HttpEntity<Void> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        return new HttpEntity<>(headers);
    }


    public HttpEntity<Void> loginAdmin() {
        return this.createHttpEntity();
    }

    public <T> HttpEntity<T> loginAdmin(T body) {
        return this.createHttpEntity(body,"");
    }

    public HttpEntity<Void> loginManager() {
        return this.createHttpEntity("");
    }

    public HttpEntity<Void> loginOperator() {
        return this.createHttpEntity("");
    }

    public HttpEntity<Void> loginCustomer() {
        return this.createHttpEntity("");
    }


}
