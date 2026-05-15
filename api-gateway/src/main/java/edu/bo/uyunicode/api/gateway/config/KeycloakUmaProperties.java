package edu.bo.uyunicode.api.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "keycloak.uma")
public class KeycloakUmaProperties {

    private String serverUrl;
    private String realm;
    private String audience;
    private long cacheTtlSeconds = 30;
}
