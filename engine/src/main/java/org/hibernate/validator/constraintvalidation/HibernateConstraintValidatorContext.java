/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraintvalidation;

import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.Incubating;
import org.hibernate.validator.messageinterpolation.MessageParameterEscaping;

/**
 * A custom {@link ConstraintValidatorContext} which allows to set additional message parameters for
 * interpolation.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public interface HibernateConstraintValidatorContext extends ConstraintValidatorContext {

	@Override
	HibernateConstraintViolationBuilder buildConstraintViolationWithTemplate(String messageTemplate);

	/**
	 * Allows to set an additional named parameter which can be interpolated in the constraint violation message.
	 * The variable will be available for interpolation for all constraint violations generated for this constraint.
	 * This includes the default one as well as all violations created by the {@link ConstraintViolationBuilder}.
	 * <p>
	 * Message parameters added via this method are subject to the default parameter escaping
	 * configured for the current validation factory (see {@link MessageParameterEscaping} and
	 * {@link org.hibernate.validator.BaseHibernateValidatorConfiguration#MESSAGE_PARAMETER_ESCAPING}).
	 * <p>
	 * To create multiple constraint violations with different variable values, this method can be called
	 * between successive calls to {@link ConstraintViolationBuilder#addConstraintViolation()}.
	 * <p>
	 * For example:
	 * <pre>
	 * {@code
	 * public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
	 *     HibernateConstraintValidatorContext context = constraintValidatorContext.unwrap( HibernateConstraintValidatorContext.class );
	 *
	 *     context.addMessageParameter( "foo", "bar" );
	 *     context.buildConstraintViolationWithTemplate( "{foo}" )
	 *            .addConstraintViolation();
	 *
	 *     context.addMessageParameter( "foo", "snafu" );
	 *     context.buildConstraintViolationWithTemplate( "{foo}" )
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
	 *
	 * @since 5.4.1
	 */
	HibernateConstraintValidatorContext addMessageParameter(String name, Object value);

	/**
	 * Allows to set an additional named parameter with explicit control over escaping behavior.
	 * <p>
	 * Same as {@link #addMessageParameter(String, Object)}, but allows explicitly specifying the message parameter
	 * escaping option.
	 * <p>
	 * When {@link MessageParameterEscaping#ESCAPE} is specified, non-{@link Number} values are escaped to prevent
	 * injection into message interpolation expressions. When {@link MessageParameterEscaping#NONE} is specified, the
	 * value is stored as-is.
	 *
	 * @param name the name under which to bind the parameter, cannot be {@code null}
	 * @param value the value to be bound to the specified name
	 * @param escaping the escaping behavior to use for this parameter
	 *
	 * @return a reference to itself to allow method chaining
	 *
	 * @throws IllegalArgumentException in case the provided name is {@code null}
	 *
	 * @since 6.2.6
	 */
	@Incubating
	HibernateConstraintValidatorContext addMessageParameter(String name, Object value, MessageParameterEscaping escaping);

	/**
	 * Allows to set an additional expression variable which will be available as an EL variable during interpolation. The
	 * variable will be available for interpolation for all constraint violations generated for this constraint.
	 * This includes the default one as well as all violations created by the {@link ConstraintViolationBuilder}.
	 * To create multiple constraint violations with different variable values, this method can be called
	 * between successive calls to {@link javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder#addConstraintViolation()}.
	 * <p>
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
	 * @param name the name under which to bind the expression variable, cannot be {@code null}
	 * @param value the value to be bound to the specified name
	 *
	 * @return a reference to itself to allow method chaining
	 *
	 * @throws IllegalArgumentException in case the provided name is {@code null}
	 */
	HibernateConstraintValidatorContext addExpressionVariable(String name, Object value);

	/**
	 *
	 * Allows to set an object that may further describe the violation.
	 *
	 * The user is responsible himself to ensure that this payload is serializable in case the
	 * {@code javax.validation.ConstraintViolation} has to be serialized.
	 *
	 * @param payload an object representing additional information about the violation
	 *
	 * @return a reference to itself to allow method chaining
	 *
	 * @since 5.3
	 */
	HibernateConstraintValidatorContext withDynamicPayload(Object payload);

	/**
	 * Returns an instance of the specified type or {@code null} if the current constraint validator payload isn't of
	 * the given type.
	 *
	 * @param type the type of payload to retrieve
	 * @return an instance of the specified type or {@code null} if the current constraint validator payload isn't of
	 * the given type
	 *
	 * @since 6.0.9
	 * @see HibernateValidatorConfiguration#constraintValidatorPayload(Object)
	 */
	@Incubating
	<C> C getConstraintValidatorPayload(Class<C> type);

}
