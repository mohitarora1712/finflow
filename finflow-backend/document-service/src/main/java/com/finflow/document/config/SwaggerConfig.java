package com.finflow.document.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
	    return new OpenAPI()

	            .servers(List.of(
	            		new Server().url("/documents")
	            ))

	            .info(new Info()
	                    .title("FinFlow API")
	                    .version("1.0.0"))

	            .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))

	            .components(new Components()
	                    .addSecuritySchemes("BearerAuth",
	                            new SecurityScheme()
	                                    .name("Authorization")
	                                    .type(SecurityScheme.Type.HTTP)
	                                    .scheme("bearer")
	                                    .bearerFormat("JWT")
	                    )
	            );
	}
}