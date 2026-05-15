package edu.bo.uyunicode.api.gateway.filter;

import edu.bo.uyunicode.api.gateway.client.KeycloakUmaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UmaAuthorizationGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of("/realms/");

    private final KeycloakUmaClient umaClient;

    @Override
    public int getOrder() {
        // Run after Spring Security (-1) but before routing (Integer.MIN_VALUE)
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        log.info("Received request for path: {}", path);
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info("Authorization header: {}", authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return respond(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        String jti = extractJti(token);
        log.info("Extracted JTI: {}, token: {}", jti, token);
        if (jti == null) {
            return respond(exchange, HttpStatus.UNAUTHORIZED);
        }

        String entity = extractEntity(path);
        log.info("Extracted entity: {}", entity);
        if (entity == null) {
            return respond(exchange, HttpStatus.FORBIDDEN);
        }

        String permission = entity + "#" + resolveScope(exchange.getRequest().getMethod());
        log.info("Resolved permission: {}", permission);

        return umaClient.isAuthorized(token, jti, permission)
                .flatMap(authorized -> authorized
                        ? chain.filter(exchange)
                        : respond(exchange, HttpStatus.FORBIDDEN));
    }

    // Extracts entity from /v1/{service}/{entity}/... → segment at index 3
    private String extractEntity(String path) {
        String[] segments = path.split("/");
        return segments.length >= 4 ? segments[3] : null;
    }

    private String resolveScope(HttpMethod method) {
        log.info("Resolving scope for method: {}", method);
        if (HttpMethod.POST.equals(method)) return "create";
        if (HttpMethod.PUT.equals(method) || HttpMethod.PATCH.equals(method) || HttpMethod.DELETE.equals(method))
            return "update"; // DELETE is logical delete (status change)
        return "read";
    }

    // JWT already validated by Spring Security — safe to parse without re-verifying signature
    private String extractJti(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            int jtiIndex = payload.indexOf("\"jti\":");
            if (jtiIndex == -1) return null;
            int start = payload.indexOf('"', jtiIndex + 6) + 1;
            int end = payload.indexOf('"', start);
            return payload.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    private Mono<Void> respond(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
