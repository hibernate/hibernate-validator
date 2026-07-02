/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.password;

import java.util.List;
import java.util.function.BiConsumer;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.constraints.PasswordPolicy;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.constraintvalidators.hv.password.DefaultPasswordContext;
import org.hibernate.validator.internal.constraintvalidators.hv.password.PasswordPolicyValidationHelper;

/**
 * Base class for class-level {@link PasswordPolicy @PasswordPolicy} validators.
 * <p>
 * Subclasses provide the password extraction logic via {@link #getPassword(Object)} and optionally
 * bind additional properties (such as a username) via {@link #bindProperties(Object, java.util.function.BiConsumer)}.
 * All policy rule resolution, building, and validation is handled by this base class.
 * <p>
 * Example usage:
 * <pre>{@code
 * public class UserDetailsPasswordPolicyValidator
 *         extends AbstractPasswordPolicyValidator<UserDetails> {
 *
 *     @Override
 *     protected char[] getPassword(UserDetails bean) {
 *         return bean.getPassword().toCharArray();
 *     }
 *
 *     @Override
 *     protected void bindProperties(UserDetails bean, BiConsumer<String, Object> propertyBinder) {
 *         propertyBinder.accept( "username", bean.getUsername() );
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of the bean being validated
 *
 * @since 9.2.0
 */
@Incubating
public abstract class AbstractPasswordPolicyValidator<T>
		implements HibernateConstraintValidator<PasswordPolicy, T> {

	private List<PasswordPolicyRule> rules;

	/**
	 * Extracts the password from the bean being validated.
	 * <p>
	 * If the password should be zeroed after validation, return a defensive copy
	 * (e.g. {@code bean.getPassword().toCharArray()}) and override {@link #isValid(Object, ConstraintValidatorContext)}
	 * to zero it in a {@code finally} block. Returning the original array is safe but leaves zeroing
	 * to the caller.
	 *
	 * @param bean the bean instance
	 * @return the password as a character array, or {@code null} if the password is not set
	 */
	protected abstract char[] getPassword(T bean);

	/**
	 * Binds additional properties from the bean to the password context, making them
	 * available to rules via {@link PasswordContext#get(String, Class)}.
	 * <p>
	 * The default implementation does nothing. Override to bind properties such as a username.
	 *
	 * @param bean the bean instance
	 * @param propertyBinder accepts a property name and value to bind
	 */
	protected void bindProperties(T bean, BiConsumer<String, Object> propertyBinder) {
	}

	@Override
	public void initialize(ConstraintDescriptor<PasswordPolicy> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		this.rules = PasswordPolicyValidationHelper.buildRules( constraintDescriptor, initializationContext );
	}

	@Override
	public boolean isValid(T value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		char[] password = getPassword( value );
		if ( password == null ) {
			return true;
		}
		DefaultPasswordContext passwordContext = PasswordPolicyValidationHelper.createContext( password );
		bindProperties( value, passwordContext::property );
		return PasswordPolicyValidationHelper.validate( passwordContext, rules, context );
	}
}
