/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine.methodvalidation.returnvaluevalidation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;

/**
 * @author Hardy Ferentschik
 */
@NotNull
@Constraint(validatedBy = AtLeastOneContactProvided.AtLeastOneContactProvidedValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneContactProvided {

	String message() default "none or more than one contact";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	public class AtLeastOneContactProvidedValidator
			implements ConstraintValidator<AtLeastOneContactProvided, ContactBean> {

		@Override
		public void initialize(final AtLeastOneContactProvided nonRecursive) {
		}

		@Override
		public boolean isValid(final ContactBean bean, final ConstraintValidatorContext constraintValidatorContext) {
			if ( bean.getEmail() == null && bean.getPhone() == null ) {
				return false;
			}
			return bean.getEmail() != null || bean.getPhone() != null;
		}
	}
}
