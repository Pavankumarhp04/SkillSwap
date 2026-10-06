package com.qsp.skillswap_backend_project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Describes the API for Swagger UI (http://localhost:8080/swagger-ui.html). The
 * "Authorize" button there takes the token from POST /api/auth/login, and
 * Swagger then sends it on every call.
 */
@Configuration
public class OpenApiConfig {

	private static final String SCHEME = "bearerAuth";

	@Bean
	public OpenAPI skillSwapOpenApi() {
		return new OpenAPI()
				.info(new Info().title("SkillSwap API").version("1.0")
						.description("Peer learning platform: mentors share slots, learners book them with credits, "
								+ "both sides leave reviews. Log in at POST /api/auth/login, then press Authorize "
								+ "and paste the token."))
				.components(new Components().addSecuritySchemes(SCHEME,
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList(SCHEME));
	}
}
