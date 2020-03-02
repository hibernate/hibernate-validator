/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Guillaume Smet
 */
public abstract class AbstractConstraintValidatorManagerImpl implements ConstraintValidatorManager {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * The explicit or implicit default constraint validator factory. We always cache {@code ConstraintValidator}
	 * instances if they are created via the default instance and with the default initialization context. Constraint
	 * validator instances created via other factory instances (specified eg via {@code ValidatorFactory#usingContext()}
	 * or initialization context are only cached for the most recently used factory and context.
	 */
	private final ConstraintValidatorFactory defaultConstraintValidatorFactory;

	/**
	 * The explicit or implicit default constraint validator initialization context. We always cache
	 * {@code ConstraintValidator} instances if they are created via the default instance and with the default context.
	 * Constraint validator instances created via other factory instances (specified eg via
	 * {@code ValidatorFactory#usingContext()} or initialization context are only cached for the most recently used
	 * factory and context.
	 */
	private final  HibernateConstraintValidatorInitializationContext defaultConstraintValidatorInitializationContext;

	/**
	 * Creates a new {@code ConstraintValidatorManager}.
	 *
	 * @param defaultConstraintValidatorFactory the default validator factory
	 * @param defaultConstraintValidatorInitializationContext the default initialization context
	 */
	public AbstractConstraintValidatorManagerImpl(ConstraintValidatorFactory defaultConstraintValidatorFactory,
			HibernateConstraintValidatorInitializationContext defaultConstraintValidatorInitializationContext) {
		this.defaultConstraintValidatorFactory = defaultConstraintValidatorFactory;
		this.defaultConstraintValidatorInitializationContext = defaultConstraintValidatorInitializationContext;
	}

	@Override
	public ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
		return defaultConstraintValidatorFactory;
	}

	@Override
	public HibernateConstraintValidatorInitializationContext getDefaultConstraintValidatorInitializationContext() {
		return defaultConstraintValidatorInitializationContext;
	}

	protected <A extends Annotation> ConstraintValidator<A, ?> createAndInitializeValidator(
			Type validatedValueType,
			ConstraintDescriptorImpl<A> descriptor,
			ConstraintValidatorFactory constraintValidatorFactory,
			HibernateConstraintValidatorInitializationContext initializationContext) {

		ConstraintValidatorDescriptor<A> validatorDescriptor = findMatchingValidatorDescriptor( descriptor, validatedValueType );
		ConstraintValidator<A, ?> constraintValidator;

		if ( validatorDescriptor == null ) {
			return null;
		}

		constraintValidator = validatorDescriptor.newInstance( constraintValidatorFactory );
		initializeValidator( descriptor, constraintValidator, initializationContext );

		return constraintValidator;
	}

	/**
	 * Runs the validator resolution algorithm.
	 *
	 * @param validatedValueType The type of the value to be validated (the type of the member/class the constraint was placed on).
	 *
	 * @return The class of a matching validator.
	 */
	private <A extends Annotation> ConstraintValidatorDescriptor<A> findMatchingValidatorDescriptor(ConstraintDescriptorImpl<A> descriptor, Type validatedValueType) {
		Map<Type, ConstraintValidatorDescriptor<A>> availableValidatorDescriptors = TypeHelper.getValidatorTypes(
				descriptor.getAnnotationType(),
				descriptor.getMatchingConstraintValidatorDescriptors()
		);

		List<Type> discoveredSuitableTypes = findSuitableValidatorTypes( validatedValueType, availableValidatorDescriptors.keySet() );
		resolveAssignableTypes( discoveredSuitableTypes );

		if ( discoveredSuitableTypes.size() == 0 ) {
			return null;
		}

		if ( discoveredSuitableTypes.size() > 1 ) {
			throw LOG.getMoreThanOneValidatorFoundForTypeException( validatedValueType, discoveredSuitableTypes );
		}

		Type suitableType = discoveredSuitableTypes.get( 0 );
		return availableValidatorDescriptors.get( suitableType );
	}

	private <A extends Annotation> List<Type> findSuitableValidatorTypes(Type type, Iterable<Type> availableValidatorTypes) {
		List<Type> determinedSuitableTypes = newArrayList();
		for ( Type validatorType : availableValidatorTypes ) {
			if ( TypeHelper.isAssignable( validatorType, type )
					&& !determinedSuitableTypes.contains( validatorType ) ) {
				determinedSuitableTypes.add( validatorType );
			}
		}
		return determinedSuitableTypes;
	}

	private <A extends Annotation> void initializeValidator(
			ConstraintDescriptor<A> descriptor,
			ConstraintValidator<A, ?> constraintValidator,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		try {
			if ( constraintValidator instanceof HibernateConstraintValidator ) {
				( (HibernateConstraintValidator<A, ?>) constraintValidator ).initialize( descriptor, initializationContext );
			}
			constraintValidator.initialize( descriptor.getAnnotation() );
		}
		catch (RuntimeException e) {
			if ( e instanceof ConstraintDeclarationException ) {
				throw e;
			}
			throw LOG.getUnableToInitializeConstraintValidatorException( constraintValidator.getClass(), e );
		}
	}

	/**
	 * Tries to reduce all assignable classes down to a single class.
	 *
	 * @param assignableTypes The set of all classes which are assignable to the class of the value to be validated and
	 * which are handled by at least one of the validators for the specified constraint.
	 */
	private void resolveAssignableTypes(List<Type> assignableTypes) {
		if ( assignableTypes.size() == 0 || assignableTypes.size() == 1 ) {
			return;
		}

		List<Type> typesToRemove = new ArrayList<>();
		do {
			typesToRemove.clear();
			Type type = assignableTypes.get( 0 );
			for ( int i = 1; i < assignableTypes.size(); i++ ) {
				if ( TypeHelper.isAssignable( type, assignableTypes.get( i ) ) ) {
					typesToRemove.add( type );
				}
				else if ( TypeHelper.isAssignable( assignableTypes.get( i ), type ) ) {
					typesToRemove.add( assignableTypes.get( i ) );
				}
			}
			assignableTypes.removeAll( typesToRemove );
		} while ( typesToRemove.size() > 0 );
	}
}
