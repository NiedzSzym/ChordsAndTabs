package com.chordsandtabs.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ChordFingeringValidator.class)
public @interface ValidChordFingering {
    String message() default "Invalid chord fingering for the selected instrument";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
