package io.bmatch.gateway.helpers.email;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ValidEmailFormatValidator implements ConstraintValidator<ValidEmailFormat, String> {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || EMAIL_PATTERN.matcher(value).matches();
    }
}