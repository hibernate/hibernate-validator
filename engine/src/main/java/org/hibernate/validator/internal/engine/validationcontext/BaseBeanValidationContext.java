/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.validationcontext;

import jakarta.validation.TraversableResolver;
import jakarta.validation.Validator;

import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.internal.engine.path.ModifiablePath;
import org.hibernate.validator.internal.engine.valuecontext.ValueContext;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;

/**
 * Interface that exposes contextual information required for a validation call related to a bean.
 * <p>
 * Provides ability to collect failing constraints and gives access to resources like constraint validator factory,
 * traversable resolver, etc.
 *
 * @author Hardy Ferentschik
 * @author Emmanuel Bernard
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public interface BaseBeanValidationContext<T> extends ValidationContext<T> {

	T getRootBean();

	Class<T> getRootBeanClass();

	BeanMetaData<T> getRootBeanMetaData();

	TraversableResolver getTraversableResolver();

	boolean isBeanAlreadyValidated(Object value, Class<?> group, ModifiablePath path);

	void markCurrentBeanAsProcessed(ValueContext<?, ?> valueContext);

	boolean hasMetaConstraintBeenProcessed(Object bean, ModifiablePath path, MetaConstraint<?> metaConstraint);

	void markConstraintProcessed(Object bean, ModifiablePath path, MetaConstraint<?> metaConstraint);

	/**
	 * @return {@code true} if current validation context can and should process passed meta constraint. Is used in
	 * {@link ValidatorImpl} to check if validation is required in case of calls to
	 * {@link Validator#validateValue(Class, String, Object, Class[])} or
	 * {@link Validator#validateProperty(Object, String, Class[])}. In these cases, as we iterate through all meta
	 * constraints of the bean, we expect those that are not defined for the validated property.
	 */
	default boolean appliesTo(MetaConstraint<?> metaConstraint) {
		return true;
	}
}
