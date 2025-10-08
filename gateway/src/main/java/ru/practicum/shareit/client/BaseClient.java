package ru.practicum.shareit.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class BaseClient {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final RestTemplate restTemplate;

    protected BaseClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected ResponseEntity<Object> get(String path) {
        return makeRequest(HttpMethod.GET, path, null, null, null);
    }

    protected ResponseEntity<Object> get(String path, Long userId) {
        return makeRequest(HttpMethod.GET, path, userId, null, null);
    }

    protected ResponseEntity<Object> get(String path, Long userId, Map<String, Object> parameters) {
        return makeRequest(HttpMethod.GET, path, userId, parameters, null);
    }

    protected ResponseEntity<Object> get(String path, Map<String, Object> parameters) {
        return makeRequest(HttpMethod.GET, path, null, parameters, null);
    }

    protected ResponseEntity<Object> post(String path, Object body) {
        return makeRequest(HttpMethod.POST, path, null, null, body);
    }

    protected ResponseEntity<Object> post(String path, Long userId, Object body) {
        return makeRequest(HttpMethod.POST, path, userId, null, body);
    }

    protected ResponseEntity<Object> post(String path, Long userId, Map<String, Object> parameters, Object body) {
        return makeRequest(HttpMethod.POST, path, userId, parameters, body);
    }

    protected ResponseEntity<Object> patch(String path, Long userId, Object body) {
        return makeRequest(HttpMethod.PATCH, path, userId, null, body);
    }

    protected ResponseEntity<Object> patch(String path, Long userId, Map<String, Object> parameters, Object body) {
        return makeRequest(HttpMethod.PATCH, path, userId, parameters, body);
    }

    protected ResponseEntity<Object> delete(String path) {
        return makeRequest(HttpMethod.DELETE, path, null, null, null);
    }

    protected ResponseEntity<Object> delete(String path, Long userId) {
        return makeRequest(HttpMethod.DELETE, path, userId, null, null);
    }

    protected ResponseEntity<Object> delete(String path, Long userId, Map<String, Object> parameters) {
        return makeRequest(HttpMethod.DELETE, path, userId, parameters, null);
    }

    private ResponseEntity<Object> makeRequest(HttpMethod method, String path, Long userId,
                                               Map<String, Object> parameters, Object body) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));
        try {
            if (parameters != null && !parameters.isEmpty()) {
                return restTemplate.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                return restTemplate.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            HttpHeaders headers = new HttpHeaders();
            if (e.getResponseHeaders() != null) {
                headers.putAll(e.getResponseHeaders());
                headers.remove(HttpHeaders.TRANSFER_ENCODING);
                headers.remove(HttpHeaders.CONTENT_LENGTH);
            }
            if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }
            return ResponseEntity.status(e.getStatusCode())
                    .headers(headers)
                    .body(e.getResponseBodyAsString());
        }
    }

    private HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.add(USER_HEADER, String.valueOf(userId));
        }
        return headers;
    }
}