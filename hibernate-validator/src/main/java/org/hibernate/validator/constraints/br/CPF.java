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
package org.hibernate.validator.constraints.br;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.ModCheck;
import org.hibernate.validator.constraints.ModCheck.ModType;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates a CPF (Cadastro de Pessoa F\u00edsica)
 *
 * @author George Gastaldi
 */
@Pattern.List({
		@Pattern(regexp = "([0-9]{3}[.]?[0-9]{3}[.]?[0-9]{3}-[0-9]{2})|([0-9]{11})"),
		// 000.000.000-00 is not a valid CPF, but passes the mod check. Needs to be singled out via regexp
		@Pattern(regexp = "^(?:(?!000\\.000\\.000-00).)*$"),
		// same for 999.999.999-99
		@Pattern(regexp = "^(?:(?!999\\.999\\.999-99).)*$")
})
@ModCheck.List({
		@ModCheck(modType = ModType.MOD11,
				checkDigitPosition = 9,
				multiplier = 10,
				endIndex = 9),
		@ModCheck(modType = ModType.MOD11,
				checkDigitPosition = 10,
				multiplier = 11,
				endIndex = 10)
})
@ReportAsSingleViolation
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface CPF {
	String message() default "{org.hibernate.validator.constraints.br.CPF.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
