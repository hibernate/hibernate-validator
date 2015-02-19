/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraintvalidation;

import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.spi.time.TimeProvider;

/**
 * A custom {@link ConstraintValidatorContext} which allows to set additional message parameters for
 * interpolation.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
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
	 * @hv.experimental Adding custom parameters to the context is considered experimental, especially the exact semantics
	 * regarding to which generated constraint violation these parameters apply might change. At the moment they apply
	 * to all violations.
	 */
	HibernateConstraintValidatorContext addExpressionVariable(String name, Object value);

	/**
	 * Returns the provider for obtaining the current time, e.g. when validating the {@code Future} and {@code Past}
	 * constraints.
	 *
	 * @return the provider for obtaining the current time, never {@code null}. If no specific provider has been
	 * configured during bootstrap, a default implementation using the current system time and the current
	 * default time zone will be returned.
	 *
	 * @since 5.2
	 */
	TimeProvider getTimeProvider();
}
