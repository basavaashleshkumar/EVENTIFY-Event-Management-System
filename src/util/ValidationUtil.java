package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Utility class for validating user input prior to database persistence.
 * Prevents erroneous and malformed data from reaching JDBC queries.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[0-9\\-\\+\\s]{7,15}$"
    );

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) {
            return true; // Optional field
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isPositiveInteger(String text) {
        if (!isNotEmpty(text)) {
            return false;
        }
        try {
            int val = Integer.parseInt(text.trim());
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeDouble(String text) {
        if (!isNotEmpty(text)) {
            return false;
        }
        try {
            double val = Double.parseDouble(text.trim());
            return val >= 0.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidDate(String dateStr) {
        if (!isNotEmpty(dateStr)) {
            return false;
        }
        try {
            LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
