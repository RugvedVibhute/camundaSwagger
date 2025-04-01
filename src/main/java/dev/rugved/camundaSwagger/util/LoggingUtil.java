package dev.rugved.camundaSwagger.util;

import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility class for safe logging to prevent PII data exposure in logs.
 * This class provides methods to mask sensitive data in log messages.
 */
public class LoggingUtil {

    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList(
            "email", "emailAddress", "postCode", "phoneNumber", "stateOrProvince",
            "street2", "street1", "city", "name", "id", "value", "mobile",
            "country", "firstName", "lastName", "formattedName", "givenName",
            "fullName", "familyName", "preferredGivenName", "location", "customer",
            "account", "user", "gender", "dateOfBirth", "address", "creditCard",
            "passport", "medicare", "driversLicence"
    ));

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{7,15}");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}");

    /**
     * Masks sensitive information in the provided value.
     *
     * @param key The key/field name to check
     * @param value The value to potentially mask
     * @return Masked value if sensitive, original value otherwise
     */
    public static String maskSensitiveData(String key, String value) {
        if (key == null || value == null) {
            return value;
        }

        String lowerKey = key.toLowerCase();

        if (SENSITIVE_FIELDS.stream().anyMatch(lowerKey::contains)) {
            return "*** MASKED ***";
        }

        // Check for common patterns regardless of field name
        if (EMAIL_PATTERN.matcher(value).matches() ||
                PHONE_PATTERN.matcher(value).matches() ||
                CREDIT_CARD_PATTERN.matcher(value).matches()) {
            return "*** MASKED ***";
        }

        return value;
    }

    /**
     * Logs object properties safely, masking sensitive data.
     *
     * @param logger The SLF4J logger
     * @param level The log level ("debug", "info", "warn", "error")
     * @param prefix Message prefix
     * @param data Map of key-value pairs to log
     */
    public static void logSafely(Logger logger, String level, String prefix, Map<String, Object> data) {
        if (logger == null || data == null) {
            return;
        }

        StringBuilder safeLog = new StringBuilder(prefix != null ? prefix : "");
        safeLog.append(" {");

        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                safeLog.append(", ");
            }
            String key = entry.getKey();
            Object value = entry.getValue();

            safeLog.append(key).append(": ");

            if (value instanceof String) {
                safeLog.append(maskSensitiveData(key, (String) value));
            } else if (value != null) {
                // For non-string values, we'll just use toString but still check if it's sensitive
                safeLog.append(maskSensitiveData(key, value.toString()));
            } else {
                safeLog.append("null");
            }

            first = false;
        }

        safeLog.append("}");

        switch (level.toLowerCase()) {
            case "debug":
                logger.debug(safeLog.toString());
                break;
            case "info":
                logger.info(safeLog.toString());
                break;
            case "warn":
                logger.warn(safeLog.toString());
                break;
            case "error":
                logger.error(safeLog.toString());
                break;
            default:
                logger.info(safeLog.toString());
        }
    }

    /**
     * Sanitizes a message by removing or masking any detected PII.
     * This is a basic implementation and should be expanded based on specific requirements.
     *
     * @param message The original message
     * @return Sanitized message
     */
    public static String sanitizeMessage(String message) {
        if (message == null) {
            return null;
        }

        // Mask email addresses
        message = EMAIL_PATTERN.matcher(message).replaceAll("*** EMAIL MASKED ***");

        // Mask phone numbers
        message = PHONE_PATTERN.matcher(message).replaceAll("*** PHONE MASKED ***");

        // Mask credit card numbers
        message = CREDIT_CARD_PATTERN.matcher(message).replaceAll("*** CC MASKED ***");

        return message;
    }
}