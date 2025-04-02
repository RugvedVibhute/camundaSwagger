package au.com.optus.renaissanceCamunda.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.client.provider.issuer-uri}")
    private String issuer;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(
                                        "/actuator/health/readiness",
                                        "/actuator/health/liveness",
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/retrieve/**",
                                        "/process/**"
                                ).permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer
                                .jwt(jwt -> jwt
                                        .decoder(jwtDecoder())
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                                // Add custom authentication entry point for 401 responses
                                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                )
                // Add custom access denied handler for 403 responses
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.accessDeniedHandler(new CustomAccessDeniedHandler())
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuer);
    }

    @Bean
    public JwtAuthenticationErrorConverter jwtAuthenticationConverter() {
        JwtAuthenticationErrorConverter converter = new JwtAuthenticationErrorConverter();
        // You can configure authorities claim name here if needed
        // converter.setAuthoritiesClaimName("roles");
        return converter;
    }
}