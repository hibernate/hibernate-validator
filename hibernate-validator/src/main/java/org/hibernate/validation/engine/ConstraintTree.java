// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.UnexpectedTypeException;
import javax.validation.ValidationException;
import javax.validation.metadata.ConstraintDescriptor;

import com.googlecode.jtype.TypeUtils;
import org.slf4j.Logger;

import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.ValidatorTypeHelper;

/**
 * Due to constraint conposition a single constraint annotation can lead to a whole constraint tree beeing validated.
 * This class encapsulates such a tree.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintTree<A extends Annotation> {

	private static final Logger log = LoggerFactory.make();

	private final ConstraintTree<?> parent;
	private final List<ConstraintTree<?>> children;
	private final ConstraintDescriptor<A> descriptor;

	private final Map<Class<? extends ConstraintValidator<?, ?>>, ConstraintValidator<A, ?>> constraintValidatorCache;

	public ConstraintTree(ConstraintDescriptor<A> descriptor) {
		this( descriptor, null );
	}

	private ConstraintTree(ConstraintDescriptor<A> descriptor, ConstraintTree<?> parent) {
		this.parent = parent;
		this.descriptor = descriptor;
		this.constraintValidatorCache = new HashMap<Class<? extends ConstraintValidator<?, ?>>, ConstraintValidator<A, ?>>();

		final Set<ConstraintDescriptor<?>> composingConstraints = descriptor.getComposingConstraints();
		children = new ArrayList<ConstraintTree<?>>( composingConstraints.size() );

		for ( ConstraintDescriptor<?> composingDescriptor : composingConstraints ) {
			ConstraintTree<?> treeNode = createConstraintTree( composingDescriptor );
			children.add( treeNode );
		}
	}

	private <U extends Annotation> ConstraintTree<U> createConstraintTree(ConstraintDescriptor<U> composingDescriptor) {
		return new ConstraintTree<U>( composingDescriptor, this );
	}

	public boolean isRoot() {
		return parent == null;
	}

	public ConstraintTree getParent() {
		return parent;
	}

	public List<ConstraintTree<?>> getChildren() {
		return children;
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	public ConstraintDescriptor<A> getDescriptor() {
		return descriptor;
	}

	public <T, U, V> void validateConstraints(Type type, GlobalExecutionContext<T> executionContext, LocalExecutionContext<U, V> localExecutionContext, List<ConstraintViolation<T>> constraintViolations) {
		// first validate composing constraints
		for ( ConstraintTree<?> tree : getChildren() ) {
			List<ConstraintViolation<T>> tmpViolations = new ArrayList<ConstraintViolation<T>>();
			tree.validateConstraints( type, executionContext, localExecutionContext, tmpViolations );
			constraintViolations.addAll( tmpViolations );
		}

		ConstraintValidatorContextImpl constraintValidatorContext = new ConstraintValidatorContextImpl(
				localExecutionContext.getPropertyPath(), descriptor
		);

		// we could have a composing constraint which does not need its own validator.
		if ( !descriptor.getConstraintValidatorClasses().isEmpty() ) {
			if ( log.isTraceEnabled() ) {
				log.trace(
						"Validating value {} against constraint defined by {}",
						localExecutionContext.getCurrentValidatedValue(),
						descriptor
				);
			}
			ConstraintValidator<A, V> validator = getInitalizedValidator(
					localExecutionContext.getCurrentValidatedValue(),
					type,
					executionContext.getConstraintValidatorFactory()
			);

			validateSingleConstraint(
					executionContext,
					localExecutionContext,
					constraintViolations,
					constraintValidatorContext,
					validator
			);
		}

		if ( reportAsSingleViolation() && constraintViolations.size() > 0 ) {
			constraintViolations.clear();
			final String message = ( String ) getDescriptor().getAttributes().get( "message" );
			ConstraintValidatorContextImpl.ErrorMessage error = constraintValidatorContext.new ErrorMessage(
					message, localExecutionContext.getPropertyPath()
			);
			ConstraintViolation<T> violation = executionContext.createConstraintViolation(
					localExecutionContext, error, descriptor
			);
			constraintViolations.add( violation );
		}
	}

	private <T, U, V> void validateSingleConstraint(GlobalExecutionContext<T> executionContext, LocalExecutionContext<U, V> localExecutionContext, List<ConstraintViolation<T>> constraintViolations, ConstraintValidatorContextImpl constraintValidatorContext, ConstraintValidator<A, V> validator) {
		boolean isValid;
		try {
			isValid = validator.isValid( localExecutionContext.getCurrentValidatedValue(), constraintValidatorContext );
		}
		catch ( RuntimeException e ) {
			throw new ValidationException( "Unexpected exception during isValid call", e );
		}
		if ( !isValid ) {
			constraintViolations.addAll(
					executionContext.createConstraintViolations(
							localExecutionContext, constraintValidatorContext
					)
			);
		}
	}

	private boolean reportAsSingleViolation() {
		return getDescriptor().isReportAsSingleViolation();
	}

	/**
	 * @param value The value to be validated.
	 * @param type The type of the value to be validated (the type of the member/class the constraint was placed on).
	 * @param constraintFactory constraint factory used to instantiate the constraint validator.
	 *
	 * @return A initalized constraint validator matching the type of the value to be validated.
	 */
	@SuppressWarnings("unchecked")
	private <V> ConstraintValidator<A, V> getInitalizedValidator(V value, Type type, ConstraintValidatorFactory constraintFactory) {
		Class<? extends ConstraintValidator<?, ?>> validatorClass = findMatchingValidatorClass( value, type );

		ConstraintValidator<A, V> constraintValidator;

		if ( !constraintValidatorCache.containsKey( validatorClass ) ) {
			constraintValidator = ( ConstraintValidator<A, V> ) constraintFactory.getInstance(
					validatorClass
			);
			if ( constraintValidator == null ) {
				throw new ValidationException(
						"Constraint factory returned null when trying to create instance of " + validatorClass.getName()
				);
			}
			constraintValidatorCache.put( validatorClass, constraintValidator );
		}
		else {
			if ( log.isTraceEnabled() ) {
				log.trace( "Constraint validator {} found in cache" );
			}
			constraintValidator = ( ConstraintValidator<A, V> ) constraintValidatorCache.get( validatorClass );
		}

		initializeConstraint( descriptor, constraintValidator );
		return constraintValidator;
	}

	/**
	 * Runs the validator resolution algorithm.
	 *
	 * @param value The value to be validated.
	 * @param type The type of the value to be validated (the type of the member/class the constraint was placed on).
	 *
	 * @return The class of a matching validator.
	 */
	private Class<? extends ConstraintValidator<?, ?>> findMatchingValidatorClass(Object value, Type type) {
		Map<Type, Class<? extends ConstraintValidator<?, ?>>> validatorsTypes =
				ValidatorTypeHelper.getValidatorsTypes( descriptor.getConstraintValidatorClasses() );

		List<Type> suitableTypes = new ArrayList<Type>();
		findSuitableValidatorTypes( type, validatorsTypes, suitableTypes );

		if ( value != null ) {
			findSuitableValidatorTypes( value.getClass(), validatorsTypes, suitableTypes );
		}

		resolveAssignableTypes( suitableTypes );
		verifyResolveWasUnique( type, suitableTypes );

		return validatorsTypes.get( suitableTypes.get( 0 ) );
	}

	private void verifyResolveWasUnique(Type valueClass, List<Type> assignableClasses) {
		if ( assignableClasses.size() == 0 ) {
			throw new UnexpectedTypeException( "No validator could be found for type: " + valueClass );
		}
		else if ( assignableClasses.size() > 1 ) {
			StringBuilder builder = new StringBuilder();
			builder.append( "There are multiple validators which could validate the type " );
			builder.append( valueClass );
			builder.append( ". The validator classes are: " );
			for ( Type clazz : assignableClasses ) {
				builder.append( clazz );
				builder.append( ", " );
			}
			builder.delete( builder.length() - 2, builder.length() );
			throw new UnexpectedTypeException( builder.toString() );
		}
	}

	private void findSuitableValidatorTypes(Type type, Map<Type, Class<? extends ConstraintValidator<?, ?>>> validatorsTypes, List<Type> suitableTypes) {
		for ( Type validatorType : validatorsTypes.keySet() ) {
			if ( TypeUtils.isAssignable( validatorType, type ) && !suitableTypes.contains( validatorType ) ) {
				suitableTypes.add( validatorType );
			}
		}
	}

	/**
	 * Tries to reduce all assignable classes down to a single class.
	 *
	 * @param assignableTypes The set of all classes which are assignable to the class of the value to be validated and
	 * which are handled by at least one of the  validators for the specified constraint.
	 */
	private void resolveAssignableTypes(List<Type> assignableTypes) {
		if ( assignableTypes.size() == 0 || assignableTypes.size() == 1 ) {
			return;
		}

		List<Type> typesToRemove = new ArrayList<Type>();
		do {
			typesToRemove.clear();
			Type type = assignableTypes.get( 0 );
			for ( int i = 1; i < assignableTypes.size(); i++ ) {
				if ( TypeUtils.isAssignable( type, assignableTypes.get( i ) ) ) {
					typesToRemove.add( type );
				}
				else if ( TypeUtils.isAssignable( assignableTypes.get( i ), type ) ) {
					typesToRemove.add( assignableTypes.get( i ) );
				}
			}
			assignableTypes.removeAll( typesToRemove );
		} while ( typesToRemove.size() > 0 );
	}

	private <V> void initializeConstraint
			(ConstraintDescriptor<A>
					descriptor, ConstraintValidator<A, V>
					constraintValidator) {
		try {
			constraintValidator.initialize( descriptor.getAnnotation() );
		}
		catch ( RuntimeException e ) {
			throw new ValidationException( "Unable to intialize " + constraintValidator.getClass().getName(), e );
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ConstraintTree" );
		sb.append( "{ descriptor=" ).append( descriptor );
		sb.append( ", isRoot=" ).append( parent == null );
		sb.append( ", constraintValidatorCache=" ).append( constraintValidatorCache );
		sb.append( '}' );
		return sb.toString();
	}
}
