/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.aspectj.validation.internal;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;

import org.hibernate.validator.aspectj.validation.Validate;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * An aspect that contains all the pointcuts and validation advices for method/constructor
 * validation. Uses {@link ServiceLoaderBasedValidatorFactoryProducer} to get an instance
 * of {@link Validator}.
 *
 * @author Marko Bekhta
 */
@Aspect
public aspect ValidationAspect {

	private final ExecutableValidator executableValidator;

	{
		// initialize an executable validator from a loaded validator factory:
		ServiceLoaderBasedValidatorFactoryProducer producer = new ServiceLoaderBasedValidatorFactoryProducer();
		executableValidator = producer.getValidatorFactory().getValidator().forExecutables();
	}

	/**
	 * Defines a pointcut that looks for {@code @Validate} annotated elements. Used to build up advices.
	 *
	 * @param validate a reference to the annotation
	 */
	pointcut annotationPointCutDefinition(Validate validate): @annotation(validate);

	/**
	 * Defines a pointcut that looks for any method executions. Used to build up advices.
	 */
	pointcut atMethodExecution(): execution(* *(..));

	/**
	 * Defines a pointcut that looks for any constructor executions. Used to build up advices.
	 * @param jp a joint point of constructor execution
	 */
	pointcut atConstructorExecution(): execution(*.new(..));

	/**
	 * Defines an advice for validation of method parameters
	 */
	before(Validate validate): annotationPointCutDefinition(validate) && atMethodExecution() {
		Set<ConstraintViolation<Object>> violations = executableValidator.validateParameters(
				thisJoinPoint.getTarget(),
				( (MethodSignature) thisJoinPoint.getSignature() ).getMethod(),
				thisJoinPoint.getArgs(),
				validate.groups()
		);

		// if there are any violations - throw a ConstraintViolationException
		if ( !violations.isEmpty() ) {
			throw new ConstraintViolationException( violations );
		}
	}

	/**
	 * Defines an advice for validation of method return value
	 */
	after(Validate validate) returning(Object returnValue): annotationPointCutDefinition(validate) && atMethodExecution() {
		Set<ConstraintViolation<Object>> violations = executableValidator.validateReturnValue(
				thisJoinPoint.getTarget(),
				( (MethodSignature) thisJoinPoint.getSignature() ).getMethod(),
				returnValue,
				validate.groups()
		);

		// if there are any violations - throw a ConstraintViolationException
		if ( !violations.isEmpty() ) {
			throw new ConstraintViolationException( violations );
		}
	}

	/**
	 * Defines an advice for validation of constructor parameters
	 */
	before(Validate validate): annotationPointCutDefinition(validate) && atConstructorExecution() {
		Set<ConstraintViolation<Object>> violations = executableValidator.validateConstructorParameters(
				( (ConstructorSignature) thisJoinPoint.getSignature() ).getConstructor(),
				thisJoinPoint.getArgs(),
				validate.groups()
		);

		// if there are any violations - throw a ConstraintViolationException
		if ( !violations.isEmpty() ) {
			throw new ConstraintViolationException( violations );
		}
	}


	/**
	 * Defines an advice for validation of method return value
	 */
	after(Validate validate): annotationPointCutDefinition(validate) && atConstructorExecution() {
		Set<ConstraintViolation<Object>> violations = executableValidator.validateConstructorReturnValue(
				( (ConstructorSignature) thisJoinPoint.getSignature() ).getConstructor(),
				thisJoinPoint.getTarget(),
				validate.groups()
		);

		// if there are any violations - throw a ConstraintViolationException
		if ( !violations.isEmpty() ) {
			throw new ConstraintViolationException( violations );
		}
	}

}
