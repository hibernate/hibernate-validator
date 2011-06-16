/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.hibernate.validator.constraints.impl.ModCheckValidator;

/**
 * Modulus check constraint.
 * 
 * @author George Gastaldi
 */
@Documented
@Constraint(validatedBy = ModCheckValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface ModCheck {
    String message() default "{org.hibernate.validator.constraints.ModCheck.message}";

    /**
     * @return The modulus algorithm to be used
     */
    ModType value();

    /**
     * @return The position of the Check Digit in input (After removing all non-numeric characters)
     */
    int checkDigitPosition() default 0;

    /**
     * @return The multiplier to be used by the chosen mod algorithm
     */
    int multiplier();

    /**
     * @return
     */
    int rangeStart() default 0;

    int rangeEnd() default Integer.MAX_VALUE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Defines several {@code @ModCheck} annotations on the same element.
     */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        ModCheck[] value();
    }

    public enum ModType {
        /**
         * Represents a MOD10 algorithm (Also known as Luhn algorithm)
         */
        MOD10,
        /**
         * Represents a MOD11 algorithm
         */
        MOD11
    }
}
