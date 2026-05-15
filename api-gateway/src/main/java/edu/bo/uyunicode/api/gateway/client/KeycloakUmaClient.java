package edu.bo.uyunicode.api.gateway.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import edu.bo.uyunicode.api.gateway.config.KeycloakUmaProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private Cache<String, Boolean> authorizationCache;

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

    public Mono<Boolean> isAuthorized(String accessToken, String jti, String resourceName) {
        log.debug("Checking authorization for JTI: {}, resource: {}", jti, resourceName);
        String cacheKey = jti + ":" + resourceName;
        Boolean cached = authorizationCache.getIfPresent(cacheKey);
        if (cached != null) {
            return Mono.just(cached);
        }
        return callKeycloakUma(accessToken, resourceName)
                .doOnNext(result -> {
                    log.info("Authorization result for JTI: {}, resource: {}, result: {}", jti, resourceName, result);
                    authorizationCache.put(cacheKey, result);
                });
    }

    private Mono<Boolean> callKeycloakUma(String accessToken, String resourceName) {
        log.debug("Calling Keycloak UMA server for resource: {}", resourceName);
        String tokenUrl = "/realms/" + properties.getRealm() + "/protocol/openid-connect/token";
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", UMA_GRANT_TYPE);
        body.add("audience", properties.getAudience());
        body.add("permission", resourceName);
        log.info("Calling Keycloak UMA server with body: {}, token url: {}, access token: {}, resource name: {}", body, tokenUrl, accessToken, resourceName);
        return webClient.post()
                .uri(tokenUrl)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    log.info("Keycloak UMA server response: {}", response);
                    return response.getStatusCode().is2xxSuccessful();
                })
                .onErrorReturn(false);
    }
}
