package lt_web.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

public final class ValidationUtil {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    private ValidationUtil() {
    }

    public static <T> Set<ConstraintViolation<T>> validate(T value) {
        return VALIDATOR.validate(value);
    }

    public static <T> String firstMessage(T value) {
        return validate(value).stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(null);
    }

    public static void close() {
        FACTORY.close();
    }
}
