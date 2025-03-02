package ua.nincorporation.userservice.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ua.nincorporation.userservice.validation.validator.UniqueEmailValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface UniqueEmail {
    String message() default "Email already in use";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

