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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintDescriptor;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.UnexpectedTypeException;
import javax.validation.ValidationException;

import org.slf4j.Logger;

import org.hibernate.validation.AmbiguousConstraintUsageException;
import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.ValidatorTypeHelper;

/**
 * Due to constraint conposition a single constraint annotation can lead to a whole constraint tree beeing validated.
 * This class encapsulates such a tree for a single constraint annotation.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintTree<A extends Annotation> {

	private static final Logger log = LoggerFactory.make();

	private final ConstraintTree<?> parent;
	private final List<ConstraintTree<?>> children;
	private final ConstraintDescriptor<A> descriptor;

	public ConstraintTree(ConstraintDescriptor<A> descriptor) {
		this( descriptor, null );
	}

	private ConstraintTree(ConstraintDescriptor<A> descriptor, ConstraintTree<?> parent) {
		this.parent = parent;
		this.descriptor = descriptor;
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

	public <T, V> void validateConstraints(V value, ExecutionContext<T> executionContext, List<ConstraintViolationImpl<T>> constraintViolations) {
		for ( ConstraintTree<?> tree : getChildren() ) {
			tree.validateConstraints( value, executionContext, constraintViolations );
		}

		if ( log.isTraceEnabled() ) {
			log.trace( "Validating value {} against constraint defined by {}", value, descriptor );
		}
		ConstraintValidator<A, V> validator = getInitalizedValidator(
				value, executionContext.getConstraintValidatorFactory()
		);
		executionContext.setConstraintDescriptor( descriptor );
		if ( !validator.isValid( value, executionContext ) ) {
			constraintViolations.addAll( executionContext.createConstraintViolations( value ) );
		}
		if ( reportAsSingleViolation() && constraintViolations.size() > 0 ) {
			constraintViolations.clear();
			final String message = ( String ) getParent().getDescriptor().getParameters().get( "message" );
			final String property = executionContext.peekPropertyPath();
			ExecutionContext<T>.ErrorMessage error = executionContext.new ErrorMessage( message, property );
			constraintViolations.add(executionContext.createConstraintViolation( value, error ));
		}
	}

	private boolean reportAsSingleViolation() {
		return getParent() != null
				&& getParent().getDescriptor().isReportAsSingleViolation();
	}

	/**
	 * @param value The value to be validated.
	 * @param constraintFactory constraint factory used to instantiate the constraint validator.
	 *
	 * @return A initalized constraint validator matching the type of the value to be validated.
	 */
	private <V> ConstraintValidator<A, V> getInitalizedValidator(Object value, ConstraintValidatorFactory constraintFactory) {
		Class<? extends ConstraintValidator<?, ?>> validatorClass;
		//FIXME This sounds really bad, why value can be null. Why are we deciding of the validator based on the value? 
		if ( value == null ) {
			validatorClass = descriptor.getConstraintValidatorClasses().get( 0 );
		}
		else {
			validatorClass = findMatchingValidatorClass( value );
		}
		//
		@SuppressWarnings("unchecked")
		ConstraintValidator<?, ?> constraintValidator =
				constraintFactory.getInstance( validatorClass );
		initializeConstraint( descriptor, constraintValidator );
		return ( ConstraintValidator<A, V> ) constraintValidator;
	}

	/**
	 * Runs the validator resolution algorithm.
	 *
	 * @param value the object to validate
	 *
	 * @return The class of a matching validator.
	 */
	private Class<? extends ConstraintValidator<?, ?>> findMatchingValidatorClass(Object value) {

		Class valueClass = determineValueClass( value );

		Map<Class<?>, Class<? extends ConstraintValidator<?, ?>>> validatorsTypes =
				ValidatorTypeHelper.getValidatorsTypes( ( List<Class<? extends ConstraintValidator<A, ?>>> ) descriptor.getConstraintValidatorClasses() );
		List<Class> assignableClasses = findAssignableClasses( valueClass, validatorsTypes );

		resolveAssignableClasses( assignableClasses );
		verifyResolveWasUnique( valueClass, assignableClasses );

		return validatorsTypes.get( assignableClasses.get( 0 ) );
	}

	private void verifyResolveWasUnique(Class valueClass, List<Class> assignableClasses) {
		if ( assignableClasses.size() == 0 ) {
			throw new UnexpectedTypeException( "No validator could be found for type: " + valueClass.getName() );
		}
		else if ( assignableClasses.size() > 1 ) {
			StringBuilder builder = new StringBuilder();
			builder.append( "There are multiple validators which could validate the type " );
			builder.append( valueClass );
			builder.append( ". The validator classes are: " );
			for ( Class clazz : assignableClasses ) {
				builder.append( clazz.getName() );
				builder.append( ", " );
			}
			builder.delete( builder.length() - 2, builder.length() );
			throw new AmbiguousConstraintUsageException( builder.toString() );
		}
	}

	private List<Class> findAssignableClasses(Class valueClass, Map<Class<?>, Class<? extends ConstraintValidator<?, ?>>> validatorsTypes) {
		List<Class> assignableClasses = new ArrayList<Class>();
		for ( Class clazz : validatorsTypes.keySet() ) {
			if ( clazz.isAssignableFrom( valueClass ) ) {
				assignableClasses.add( clazz );
			}
		}
		return assignableClasses;
	}

	private Class determineValueClass(Object value) {
		Class valueClass = value.getClass();
		if ( valueClass.isArray() ) {
			valueClass = Array.class;
		}
		return valueClass;
	}

	/**
	 * Tries to reduce all assignable classes down to a single class.
	 *
	 * @param assignableClasses The set of all classes which are assignable to the class of the value to be validated and
	 * which are handled by at least one of the  validators for the specified constraint.
	 */
	private void resolveAssignableClasses(List<Class> assignableClasses) {
		if ( assignableClasses.size() == 0 || assignableClasses.size() == 1 ) {
			return;
		}

		List<Class> classesToRemove = new ArrayList<Class>();
		do {
			classesToRemove.clear();
			Class clazz = assignableClasses.get( 0 );
			for ( int i = 1; i < assignableClasses.size(); i++ ) {
				if ( clazz.isAssignableFrom( assignableClasses.get( i ) ) ) {
					classesToRemove.add( clazz );
				}
				else if ( assignableClasses.get( i ).isAssignableFrom( clazz ) ) {
					classesToRemove.add( assignableClasses.get( i ) );
				}
			}
			assignableClasses.removeAll( classesToRemove );
		} while ( classesToRemove.size() > 0 );
	}

	private void initializeConstraint
			(ConstraintDescriptor<A>
					descriptor, ConstraintValidator
					constraintValidator) {
		try {
			constraintValidator.initialize( descriptor.getAnnotation() );
		}
		catch ( RuntimeException e ) {
			throw new ValidationException( "Unable to intialize " + constraintValidator.getClass().getName(), e );
		}
	}
}
