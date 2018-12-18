/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.validationcontext;

import java.lang.reflect.Executable;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.TraversableResolver;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;

/**
 * Builder for creating {@link AbstractValidationContext}s suited for the different kinds of validation.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
public class ValidationContextBuilder {

	private final BeanMetaDataManager beanMetaDataManager;
	private final ConstraintValidatorManager constraintValidatorManager;
	private final ConstraintValidatorFactory constraintValidatorFactory;
	private final TraversableResolver traversableResolver;
	private final HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext;
	private final ValidatorScopedContext validatorScopedContext;

	public ValidationContextBuilder(
			BeanMetaDataManager beanMetaDataManager,
			ConstraintValidatorManager constraintValidatorManager,
			ConstraintValidatorFactory constraintValidatorFactory,
			ValidatorScopedContext validatorScopedContext,
			TraversableResolver traversableResolver,
			HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext) {
		this.beanMetaDataManager = beanMetaDataManager;
		this.constraintValidatorManager = constraintValidatorManager;
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.traversableResolver = traversableResolver;
		this.constraintValidatorInitializationContext = constraintValidatorInitializationContext;
		this.validatorScopedContext = validatorScopedContext;
	}

	public <T> BaseBeanValidationContext<T> forValidate(T rootBean) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = (Class<T>) rootBean.getClass();
		BeanMetaData<T> rootBeanMetaData = beanMetaDataManager.getBeanMetaData( rootBeanClass );

		return new BeanValidationContext<>(
				constraintValidatorManager,
				constraintValidatorFactory,
				validatorScopedContext,
				traversableResolver,
				constraintValidatorInitializationContext,
				rootBean,
				rootBeanClass,
				rootBeanMetaData
		);
	}

	public <T> BaseBeanValidationContext<T> forValidateProperty(T rootBean, PathImpl propertyPath) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = (Class<T>) rootBean.getClass();
		BeanMetaData<T> rootBeanMetaData = beanMetaDataManager.getBeanMetaData( rootBeanClass );

		return new PropertyValidationContext<>(
				constraintValidatorManager,
				constraintValidatorFactory,
				validatorScopedContext,
				traversableResolver,
				constraintValidatorInitializationContext,
				rootBean,
				rootBeanClass,
				rootBeanMetaData,
				propertyPath.getLeafNode().getName()
		);
	}

	public <T> BaseBeanValidationContext<T> forValidateValue(Class<T> rootBeanClass, PathImpl propertyPath) {
		BeanMetaData<T> rootBeanMetaData = beanMetaDataManager.getBeanMetaData( rootBeanClass );

		return new PropertyValidationContext<>(
				constraintValidatorManager,
				constraintValidatorFactory,
				validatorScopedContext,
				traversableResolver,
				constraintValidatorInitializationContext,
				null, //root bean
				rootBeanClass,
				rootBeanMetaData,
				propertyPath.getLeafNode().getName()
		);
	}

	public <T> ExecutableValidationContext<T> forValidateParameters(
			T rootBean,
			Executable executable,
			Object[] executableParameters) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = rootBean != null ? (Class<T>) rootBean.getClass() : (Class<T>) executable.getDeclaringClass();
		BeanMetaData<T> rootBeanMetaData = beanMetaDataManager.getBeanMetaData( rootBeanClass );

		return new ParameterExecutableValidationContext<>(
				constraintValidatorManager,
				constraintValidatorFactory,
				validatorScopedContext,
				traversableResolver,
				constraintValidatorInitializationContext,
				rootBean,
				rootBeanClass,
				rootBeanMetaData,
				executable,
				rootBeanMetaData.getMetaDataFor( executable ),
				executableParameters
		);
	}

	public <T> ExecutableValidationContext<T> forValidateReturnValue(
			T rootBean,
			Executable executable,
			Object executableReturnValue) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = rootBean != null ? (Class<T>) rootBean.getClass() : (Class<T>) executable.getDeclaringClass();
		BeanMetaData<T> rootBeanMetaData = beanMetaDataManager.getBeanMetaData( rootBeanClass );

		return new ReturnValueExecutableValidationContext<>(
				constraintValidatorManager,
				constraintValidatorFactory,
				validatorScopedContext,
				traversableResolver,
				constraintValidatorInitializationContext,
				rootBean,
				rootBeanClass,
				rootBeanMetaData,
				executable,
				rootBeanMetaData.getMetaDataFor( executable ),
				executableReturnValue
		);
	}
}
