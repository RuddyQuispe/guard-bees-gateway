package edu.bo.uyunicode.api.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import edu.bo.uyunicode.api.gateway.config.KeycloakUmaProperties;

@SpringBootApplication
@EnableConfigurationProperties(KeycloakUmaProperties.class)
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
