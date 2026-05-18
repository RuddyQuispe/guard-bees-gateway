package edu.bo.uyunicode.api.gateway.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import edu.bo.uyunicode.api.gateway.config.KeycloakUmaProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakUmaClient {

    private static final String UMA_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:uma-ticket";

    private final KeycloakUmaProperties properties;
    private final WebClient.Builder webClientBuilder;

    private WebClient webClient;
    private Cache<String, HttpStatus> authorizationCache;

    @PostConstruct
    void init() {
        log.info("Initializing Keycloak UMA client with server URL: {}", properties.getServerUrl());
        webClient = webClientBuilder
                .baseUrl(properties.getServerUrl())
                .build();
        authorizationCache = Caffeine.newBuilder()
                .expireAfterWrite(properties.getCacheTtlSeconds(), TimeUnit.SECONDS)
                .build();
    }

    public Mono<HttpStatus> isAuthorized(String accessToken, String jti, String permission) {
        log.info("Checking authorization for JTI: {}, permission: {}", jti, permission);
        String cacheKey = jti + ":" + permission;
        HttpStatus cached = authorizationCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.info("Cache hit for JTI: {}, permission: {}, status: {}", jti, permission, cached);
            return Mono.just(cached);
        }
        return callKeycloakUma(accessToken, permission)
                .doOnNext(status -> {
                    log.info("Authorization result for JTI: {}, permission: {}, status: {}", jti, permission, status);
                    if (!status.is5xxServerError()) {
                        authorizationCache.put(cacheKey, status);
                    }
                });
    }

    private Mono<HttpStatus> callKeycloakUma(String accessToken, String permission) {
        String tokenUrl = "/realms/" + properties.getRealm() + "/protocol/openid-connect/token";
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", UMA_GRANT_TYPE);
        body.add("audience", properties.getAudience());
        body.add("permission", permission);
        log.info("Calling Keycloak UMA endpoint: {}, permission: {}", tokenUrl, permission);
        log.info("Request body: {}", body);
        return webClient.post()
                .uri(tokenUrl)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .exchangeToMono(response -> {
                    HttpStatus status = (HttpStatus) response.statusCode();
                    log.info("Keycloak UMA response: {}", status);
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .doOnNext(responseBody -> log.info("Keycloak UMA body: {}", responseBody))
                            .thenReturn(status);
                })
                .onErrorResume(ex -> {
                    log.error("Keycloak UMA unreachable: {}", ex.getMessage());
                    return Mono.just(HttpStatus.SERVICE_UNAVAILABLE);
                });
    }
}
