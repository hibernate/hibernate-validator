/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import static org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager.DUMMY_CONSTRAINT_VALIDATOR;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;

import org.hibernate.validator.internal.engine.ValidationContext;
import org.hibernate.validator.internal.engine.ValueContext;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Due to constraint composition a single constraint annotation can lead to a whole constraint tree being validated.
 * This class encapsulates such a tree.
 *
 * @author Hardy Ferentschik
 * @author Federico Mancini
 * @author Dag Hovland
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2012 SERLI
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public abstract class ConstraintTree<A extends Annotation> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * The constraint descriptor for the constraint represented by this constraint tree.
	 */
	protected final ConstraintDescriptorImpl<A> descriptor;

	private final Type validatedValueType;

	/**
	 * Either the initialized constraint validator for the default constraint validator factory or
	 * {@link ConstraintValidatorManager#DUMMY_CONSTRAINT_VALIDATOR}.
	 */
	private volatile ConstraintValidator<A, ?> constraintValidatorForDefaultConstraintValidatorFactory;

	protected ConstraintTree(ConstraintDescriptorImpl<A> descriptor, Type validatedValueType) {
		this.descriptor = descriptor;
		this.validatedValueType = validatedValueType;
	}

	public static <U extends Annotation> ConstraintTree<U> of(ConstraintDescriptorImpl<U> composingDescriptor, Type validatedValueType) {
		if ( composingDescriptor.getComposingConstraintImpls().isEmpty() ) {
			return new SimpleConstraintTree<>( composingDescriptor, validatedValueType );
		}
		else {
			return new ComposingConstraintTree<>( composingDescriptor, validatedValueType );
		}
	}

	public final <T> boolean validateConstraints(ValidationContext<T> executionContext, ValueContext<?, ?> valueContext) {
		Set<ConstraintViolation<T>> constraintViolations = newHashSet( 5 );
		validateConstraints( executionContext, valueContext, constraintViolations );
		if ( !constraintViolations.isEmpty() ) {
			executionContext.addConstraintFailures( constraintViolations );
			return false;
		}
		return true;
	}

	protected abstract <T> void validateConstraints(ValidationContext<T> executionContext, ValueContext<?, ?> valueContext, Set<ConstraintViolation<T>> constraintViolations);

	public final ConstraintDescriptorImpl<A> getDescriptor() {
		return descriptor;
	}

	public final Type getValidatedValueType() {
		return this.validatedValueType;
	}

	private ValidationException getExceptionForNullValidator(Type validatedValueType, String path) {
		if ( descriptor.getConstraintType() == ConstraintDescriptorImpl.ConstraintType.CROSS_PARAMETER ) {
			return LOG.getValidatorForCrossParameterConstraintMustEitherValidateObjectOrObjectArrayException(
					descriptor.getAnnotationType()
			);
		}
		else {
			String className = validatedValueType.toString();
			if ( validatedValueType instanceof Class ) {
				Class<?> clazz = (Class<?>) validatedValueType;
				if ( clazz.isArray() ) {
					className = clazz.getComponentType().toString() + "[]";
				}
				else {
					className = clazz.getName();
				}
			}
			return LOG.getNoValidatorFoundForTypeException( descriptor.getAnnotationType(), className, path );
		}
	}

	protected final <T> ConstraintValidator<A, ?> getInitializedConstraintValidator(ValidationContext<T> validationContext, ValueContext<?, ?> valueContext) {
		ConstraintValidator<A, ?> validator;

		if ( validationContext.getConstraintValidatorFactory() == validationContext.getConstraintValidatorManager().getDefaultConstraintValidatorFactory() ) {
			validator = constraintValidatorForDefaultConstraintValidatorFactory;

			if ( validator == null ) {
				synchronized ( this ) {
					validator = constraintValidatorForDefaultConstraintValidatorFactory;
					if ( validator == null ) {
						validator = getInitializedConstraintValidator( validationContext );
						constraintValidatorForDefaultConstraintValidatorFactory = validator;
					}
				}
			}
		}
		else {
			// For now, we don't cache the result in the ConstraintTree if we don't use the default constraint validator
			// factory. Creating a lot of CHM for that cache might not be a good idea and we prefer being conservative
			// for now. Note that we have the ConstraintValidatorManager cache that mitigates the situation.
			// If you come up with a use case where it makes sense, please reach out to us.
			validator = getInitializedConstraintValidator( validationContext );
		}

		if ( validator == DUMMY_CONSTRAINT_VALIDATOR ) {
			throw getExceptionForNullValidator( validatedValueType, valueContext.getPropertyPath().asString() );
		}

		return validator;
	}

	@SuppressWarnings("unchecked")
	private ConstraintValidator<A, ?> getInitializedConstraintValidator(ValidationContext<?> validationContext) {
		ConstraintValidator<A, ?> validator = validationContext.getConstraintValidatorManager().getInitializedValidator(
				validatedValueType,
				descriptor,
				validationContext.getConstraintValidatorFactory(),
				validationContext.getConstraintValidatorInitializationContext()
		);

		if ( validator != null ) {
			return validator;
		}
		else {
			return (ConstraintValidator<A, ?>) DUMMY_CONSTRAINT_VALIDATOR;
		}
	}

	protected final <T, V> Set<ConstraintViolation<T>> validateSingleConstraint(ValidationContext<T> executionContext,
			ValueContext<?, ?> valueContext,
			ConstraintValidatorContextImpl constraintValidatorContext,
			ConstraintValidator<A, V> validator) {
		boolean isValid;
		try {
			@SuppressWarnings("unchecked")
			V validatedValue = (V) valueContext.getCurrentValidatedValue();
			isValid = validator.isValid( validatedValue, constraintValidatorContext );
		}
		catch (RuntimeException e) {
			if ( e instanceof ConstraintDeclarationException ) {
				throw e;
			}
			throw LOG.getExceptionDuringIsValidCallException( e );
		}
		if ( !isValid ) {
			//We do not add these violations yet, since we don't know how they are
			//going to influence the final boolean evaluation
			return executionContext.createConstraintViolations(
					valueContext, constraintValidatorContext
			);
		}
		return Collections.emptySet();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ConstraintTree" );
		sb.append( "{ descriptor=" ).append( descriptor );
		sb.append( '}' );
		return sb.toString();
	}

}
