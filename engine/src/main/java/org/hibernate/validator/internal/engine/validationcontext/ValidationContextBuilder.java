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
import org.hibernate.validator.engine.HibernateConstrainedType;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;

/**
 * Builder for creating {@link AbstractValidationContext}s suited for the different kinds of validation.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
public class ValidationContextBuilder {

	private final ConstraintValidatorManager constraintValidatorManager;
	private final ConstraintValidatorFactory constraintValidatorFactory;
	private final TraversableResolver traversableResolver;
	private final HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext;
	private final ValidatorScopedContext validatorScopedContext;

	public ValidationContextBuilder(
			ConstraintValidatorManager constraintValidatorManager,
			ConstraintValidatorFactory constraintValidatorFactory,
			ValidatorScopedContext validatorScopedContext,
			TraversableResolver traversableResolver,
			HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext) {
		this.constraintValidatorManager = constraintValidatorManager;
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.traversableResolver = traversableResolver;
		this.constraintValidatorInitializationContext = constraintValidatorInitializationContext;
		this.validatorScopedContext = validatorScopedContext;
	}

	public <T> BaseBeanValidationContext<T> forValidate(HibernateConstrainedType<T> constrainedType, BeanMetaData<T> rootBeanMetaData, T rootBean) {
		return new BeanValidationContext<>(
				constraintValidatorManager,
				constraintValidatorFactory,
				validatorScopedContext,
				traversableResolver,
				constraintValidatorInitializationContext,
				rootBean,
				constrainedType.getActuallClass(),
				rootBeanMetaData
		);
	}

	public <T> BaseBeanValidationContext<T> forValidateProperty(HibernateConstrainedType<T> constrainedType, BeanMetaData<T> rootBeanMetaData, T rootBean, PathImpl propertyPath) {
		return new PropertyValidationContext<>(
				constraintValidatorManager,
				constraintValidatorFactory,
				validatorScopedContext,
				traversableResolver,
				constraintValidatorInitializationContext,
				rootBean,
				constrainedType.getActuallClass(),
				rootBeanMetaData,
				propertyPath.getLeafNode().getName()
		);
	}

	public <T> BaseBeanValidationContext<T> forValidateValue(HibernateConstrainedType<T> constrainedType, BeanMetaData<T> rootBeanMetaData, PathImpl propertyPath) {
		return new PropertyValidationContext<>(
				constraintValidatorManager,
				constraintValidatorFactory,
				validatorScopedContext,
				traversableResolver,
				constraintValidatorInitializationContext,
				null, //root bean
				constrainedType.getActuallClass(),
				rootBeanMetaData,
				propertyPath.getLeafNode().getName()
		);
	}

	public <T> ExecutableValidationContext<T> forValidateParameters(
			HibernateConstrainedType<T> constrainedType,
			BeanMetaData<T> rootBeanMetaData,
			T rootBean,
			Executable executable,
			Object[] executableParameters) {
		return new ParameterExecutableValidationContext<>(
				constraintValidatorManager,
				constraintValidatorFactory,
				validatorScopedContext,
				traversableResolver,
				constraintValidatorInitializationContext,
				rootBean,
				constrainedType.getActuallClass(),
				rootBeanMetaData,
				executable,
				rootBeanMetaData.getMetaDataFor( executable ),
				executableParameters
		);
	}

	public <T> ExecutableValidationContext<T> forValidateReturnValue(
			HibernateConstrainedType<T> constrainedType,
			BeanMetaData<T> rootBeanMetaData,
			T rootBean,
			Executable executable,
			Object executableReturnValue) {
		return new ReturnValueExecutableValidationContext<>(
				constraintValidatorManager,
				constraintValidatorFactory,
				validatorScopedContext,
				traversableResolver,
				constraintValidatorInitializationContext,
				rootBean,
				constrainedType.getActuallClass(),
				rootBeanMetaData,
				executable,
				rootBeanMetaData.getMetaDataFor( executable ),
				executableReturnValue
		);
	}
}
