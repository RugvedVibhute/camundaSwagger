package au.com.optus.renaissanceCamunda.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Custom JWT authentication converter that provides better error messages
 * for JWT authentication failures.
 */
public class JwtAuthenticationErrorConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
    private final JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

    public JwtAuthenticationErrorConverter() {
        delegate.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        try {
            return delegate.convert(jwt);
        } catch (JwtException e) {
            // Convert JwtException to OAuth2AuthenticationException with better error details
            OAuth2Error error = new OAuth2Error(
                    "invalid_token",
                    "Invalid JWT token: " + e.getMessage(),
                    null
            );
            throw new OAuth2AuthenticationException(error, error.getDescription());
        } catch (Exception e) {
            // Handle any other exceptions during token conversion
            OAuth2Error error = new OAuth2Error(
                    "authentication_error",
                    "Authentication error: " + e.getMessage(),
                    null
            );
            throw new OAuth2AuthenticationException(error, error.getDescription());
        }
    }

    // Add method to set authorities claim name if needed
    public void setAuthoritiesClaimName(String authoritiesClaimName) {
        authoritiesConverter.setAuthoritiesClaimName(authoritiesClaimName);
        delegate.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    }
}