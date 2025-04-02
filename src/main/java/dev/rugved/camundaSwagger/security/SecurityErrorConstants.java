package au.com.optus.renaissanceCamunda.security;

/**
 * Constants for security-related error codes and messages.
 * This provides consistency in error responses across the application.
 */
public final class SecurityErrorConstants {

    // Error codes
    public static final String UNAUTHORIZED_ERROR_CODE = "CAM-401";
    public static final String FORBIDDEN_ERROR_CODE = "CAM-403";

    // Error messages
    public static final String UNAUTHORIZED_ERROR_MESSAGE = "Unauthorized";
    public static final String FORBIDDEN_ERROR_MESSAGE = "Forbidden";

    // Error descriptions
    public static final String UNAUTHORIZED_DESCRIPTION_PREFIX = "Authentication failed: ";
    public static final String FORBIDDEN_DESCRIPTION_PREFIX = "Access denied: ";

    // Common messages
    public static final String TOKEN_EXPIRED = "JWT token has expired";
    public static final String INVALID_TOKEN = "Invalid JWT token";
    public static final String INSUFFICIENT_SCOPE = "Insufficient scope for this resource";

    private SecurityErrorConstants() {
        // Private constructor to prevent instantiation
    }
}