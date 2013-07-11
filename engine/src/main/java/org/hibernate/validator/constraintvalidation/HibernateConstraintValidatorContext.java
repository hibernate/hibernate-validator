/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.constraintvalidation;

import javax.validation.ConstraintValidatorContext;

/**
 * A custom {@link ConstraintValidatorContext} which allows to set additional message parameters for
 * interpolation.
 *
 * @author Hardy Ferentschik
 * @hv.experimental Adding custom parameters to the context is considered experimental, especially the exact semantics
 * regarding to which generated constraint violation these parameters apply might change. At the moment they apply
 * to all violations.
 */
public interface HibernateConstraintValidatorContext extends ConstraintValidatorContext {

	/**
	 * Allows to set an additional named variable which can be interpolated in the constraint violation message. The
	 * variable will be available for interpolation for all constraint violations generated for this constraint.
	 * This includes the default one as well as all violations created by the {@link ConstraintViolationBuilder}.
	 * To create multiple constraint violations with different variable values, this method can be called
	 * between successive calls to {@link javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder#addConstraintViolation()}.
	 * For example:
	 * <pre>
	 * {@code
	 * public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
	 *     HibernateConstraintValidatorContext context = constraintValidatorContext.unwrap( HibernateConstraintValidatorContext.class );
	 *
	 *     context.addExpressionVariable( "foo", "bar" );
	 *     context.buildConstraintViolationWithTemplate( "${foo}" )
	 *            .addConstraintViolation();
	 *
	 *     context.addExpressionVariable( "foo", "snafu" );
	 *     context.buildConstraintViolationWithTemplate( "${foo}" )
	 *            .addConstraintViolation();
	 *
	 *     return false;
	 *  }
	 *  }
	 *
	 * </pre>
	 *
	 * @param name the name under which to bind the parameter, cannot be {@code null}
	 * @param value the value to be bound to the specified name
	 *
	 * @return a reference to itself to allow method chaining
	 *
	 * @throws IllegalArgumentException in case the provided name is {@code null}
	 */
	HibernateConstraintValidatorContext addExpressionVariable(String name, Object value);
}
