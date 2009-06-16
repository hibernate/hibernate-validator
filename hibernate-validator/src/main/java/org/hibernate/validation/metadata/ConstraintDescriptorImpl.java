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
package org.hibernate.validation.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintDefinitionException;
import javax.validation.ConstraintValidator;
import javax.validation.OverridesAttribute;
import javax.validation.ReportAsSingleViolation;
import javax.validation.ValidationException;
import javax.validation.groups.Default;
import javax.validation.metadata.ConstraintDescriptor;

import org.slf4j.Logger;

import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.ReflectionHelper;
import org.hibernate.validation.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validation.util.annotationfactory.AnnotationFactory;

/**
 * Describe a single constraint (including it's composing constraints).
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ConstraintDescriptorImpl<T extends Annotation> implements ConstraintDescriptor<T> {
	private static final Logger log = LoggerFactory.make();
	private static final int OVERRIDES_PARAMETER_DEFAULT_INDEX = -1;

	/**
	 * The actual constraint annotation.
	 */
	private final T annotation;

	/**
	 * The set of classes implementing the validation for this constraint. See also
	 * <code>ConstraintValidator</code> resolution algorithm.
	 */
	private final List<Class<? extends ConstraintValidator<T, ?>>> constraintValidatorDefinitonClasses = new ArrayList<Class<? extends ConstraintValidator<T, ?>>>();

	/**
	 * The groups for which to apply this constraint.
	 */
	private final Set<Class<?>> groups = new HashSet<Class<?>>();

	/**
	 * The constraint parameters as map. The key is the paramter name and the value the
	 * parameter value as specified in the constraint.
	 */
	private final Map<String, Object> attributes;

	/**
	 * The composing constraints for this constraints.
	 */
	private final Set<ConstraintDescriptor<?>> composingConstraints = new HashSet<ConstraintDescriptor<?>>();

	/**
	 * Flag indicating if in case of a composing constraint a single error or multiple errors should be raised.
	 */
	private final boolean isReportAsSingleInvalidConstraint;

	/**
	 * Handle to the builtin constraint implementations.
	 */
	private final ConstraintHelper constraintHelper;

	public ConstraintDescriptorImpl(T annotation, ConstraintHelper constraintHelper, Class<?> implicitGroup) {
		this( annotation, constraintHelper );
		this.groups.add( implicitGroup );
	}


	public ConstraintDescriptorImpl(T annotation, ConstraintHelper constraintHelper) {
		this.annotation = annotation;
		this.attributes = getAnnotationParameters( annotation );
		this.constraintHelper = constraintHelper;

		this.isReportAsSingleInvalidConstraint = annotation.annotationType().isAnnotationPresent(
				ReportAsSingleViolation.class
		);

		Class<?>[] groupsFromAnnotation = ReflectionHelper.getAnnotationParameter(
				annotation, "groups", Class[].class
		);
		if ( groupsFromAnnotation.length == 0 ) {
			groups.add( Default.class );
		}
		else {
			this.groups.addAll( Arrays.asList( groupsFromAnnotation ) );
		}

		findConstraintValidatorClasses();
		Map<ClassIndexWrapper, Map<String, Object>> overrideParameters = parseOverrideParameters();
		parseComposingConstraints( overrideParameters );
	}

	private void findConstraintValidatorClasses() {
		if ( constraintHelper.containsConstraintValidatorDefinition( annotation.annotationType() ) ) {
			for ( Class<? extends ConstraintValidator<? extends Annotation, ?>> validator : constraintHelper
					.getConstraintValidatorDefinition( annotation.annotationType() ) ) {
				constraintValidatorDefinitonClasses.add( ( Class<? extends ConstraintValidator<T, ?>> ) validator );
			}
			return;
		}

		List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> constraintDefinitonClasses = new ArrayList<Class<? extends ConstraintValidator<? extends Annotation, ?>>>();
		if ( constraintHelper.isBuiltinConstraint( annotation.annotationType() ) ) {
			constraintDefinitonClasses.addAll( constraintHelper.getBuiltInConstraints( annotation.annotationType() ) );
		}
		else {
			final Class<? extends Annotation> annotationType = annotation.annotationType();
			Class<? extends ConstraintValidator<?, ?>>[] validatedBy = annotationType
					.getAnnotation( Constraint.class )
					.validatedBy();
			constraintDefinitonClasses.addAll( Arrays.asList( validatedBy ) );
		}

		constraintHelper.addConstraintValidatorDefinition(
				annotation.annotationType(), constraintDefinitonClasses
		);

		for ( Class<? extends ConstraintValidator<? extends Annotation, ?>> validator : constraintDefinitonClasses ) {
			@SuppressWarnings("unchecked")
			Class<? extends ConstraintValidator<T, ?>> safeValidator = ( Class<? extends ConstraintValidator<T, ?>> ) validator;
			constraintValidatorDefinitonClasses.add( safeValidator );
		}
	}

	public T getAnnotation() {
		return annotation;
	}

	public Set<Class<?>> getGroups() {
		return Collections.unmodifiableSet( groups );
	}

	public List<Class<? extends ConstraintValidator<T, ?>>> getConstraintValidatorClasses() {
		return Collections.unmodifiableList( constraintValidatorDefinitonClasses );
	}

	public Map<String, Object> getAttributes() {
		return Collections.unmodifiableMap( attributes );
	}

	public Set<ConstraintDescriptor<?>> getComposingConstraints() {
		return Collections.unmodifiableSet( composingConstraints );
	}

	public boolean isReportAsSingleViolation() {
		return isReportAsSingleInvalidConstraint;
	}

	@Override
	public String toString() {
		return "ConstraintDescriptorImpl{" +
				"annotation=" + annotation +
				", constraintValidatorDefinitonClasses=" + constraintValidatorDefinitonClasses.toString() +
				", groups=" + groups +
				", attributes=" + attributes +
				", composingConstraints=" + composingConstraints +
				", isReportAsSingleInvalidConstraint=" + isReportAsSingleInvalidConstraint +
				'}';
	}

	private Map<String, Object> getAnnotationParameters(Annotation annotation) {
		Method[] declaredMethods = annotation.annotationType().getDeclaredMethods();
		Map<String, Object> parameters = new HashMap<String, Object>( declaredMethods.length );
		for ( Method m : declaredMethods ) {
			try {
				parameters.put( m.getName(), m.invoke( annotation ) );
			}
			catch ( IllegalAccessException e ) {
				throw new ValidationException( "Unable to read annotation attributes: " + annotation.getClass(), e );
			}
			catch ( InvocationTargetException e ) {
				throw new ValidationException( "Unable to read annotation attributes: " + annotation.getClass(), e );
			}
		}
		return Collections.unmodifiableMap( parameters );
	}

	private Object getMethodValue(Annotation annotation, Method m) {
		Object value;
		try {
			value = m.invoke( annotation );
		}
		// should never happen
		catch ( IllegalAccessException e ) {
			throw new ValidationException( "Unable to retrieve annotation parameter value." );
		}
		catch ( InvocationTargetException e ) {
			throw new ValidationException( "Unable to retrieve annotation parameter value." );
		}
		return value;
	}

	private Map<ClassIndexWrapper, Map<String, Object>> parseOverrideParameters() {
		Map<ClassIndexWrapper, Map<String, Object>> overrideParameters = new HashMap<ClassIndexWrapper, Map<String, Object>>();
		for ( Method m : annotation.annotationType().getMethods() ) {
			if ( m.getAnnotation( OverridesAttribute.class ) != null ) {
				addOverrideAttributes(
						overrideParameters, m, m.getAnnotation( OverridesAttribute.class )
				);
			}
			else if ( m.getAnnotation( OverridesAttribute.List.class ) != null ) {
				addOverrideAttributes(
						overrideParameters,
						m,
						m.getAnnotation( OverridesAttribute.List.class ).value()
				);
			}
		}
		return overrideParameters;
	}

	private void addOverrideAttributes(Map<ClassIndexWrapper, Map<String, Object>> overrideParameters, Method m, OverridesAttribute... attributes) {

		Object value = getMethodValue( annotation, m );
		for ( OverridesAttribute overridesAttribute : attributes ) {
			ensureAttributeIsOverridable( m, overridesAttribute );

			ClassIndexWrapper wrapper = new ClassIndexWrapper(
					overridesAttribute.constraint(), overridesAttribute.constraintIndex()
			);
			Map<String, Object> map = overrideParameters.get( wrapper );
			if ( map == null ) {
				map = new HashMap<String, Object>();
				overrideParameters.put( wrapper, map );
			}
			map.put( overridesAttribute.name(), value );
		}
	}

	private void ensureAttributeIsOverridable(Method m, OverridesAttribute overridesAttribute) {
		try {
			Class<?> returnTypeOfOverridenConstraint = overridesAttribute.constraint()
					.getMethod( overridesAttribute.name() )
					.getReturnType();
			if ( !returnTypeOfOverridenConstraint.equals( m.getReturnType() ) ) {
				String message = "The overiding type of a composite constraint must be identical to the overwridden one. Expected " + returnTypeOfOverridenConstraint
						.getName() + " found " + m.getReturnType();
				throw new ConstraintDefinitionException( message );
			}
		}
		catch ( NoSuchMethodException nsme ) {
			throw new ConstraintDefinitionException(
					"Overriden constraint does not define an attribute with name " + overridesAttribute.name()
			);
		}
	}

	private void parseComposingConstraints(Map<ClassIndexWrapper, Map<String, Object>> overrideParameters) {
		for ( Annotation declaredAnnotation : annotation.annotationType().getDeclaredAnnotations() ) {
			if ( constraintHelper.isConstraintAnnotation( declaredAnnotation )
					|| constraintHelper.isBuiltinConstraint( declaredAnnotation.annotationType() ) ) {
				ConstraintDescriptorImpl<?> descriptor = createComposingConstraintDescriptor(
						declaredAnnotation, overrideParameters, OVERRIDES_PARAMETER_DEFAULT_INDEX
				);
				composingConstraints.add( descriptor );
				log.debug( "Adding composing constraint: " + descriptor );
			}
			else if ( constraintHelper.isMultiValueConstraint( declaredAnnotation ) ) {
				List<Annotation> multiValueConstraints = constraintHelper.getMultiValueConstraints( declaredAnnotation );
				int index = 1;
				for ( Annotation constraintAnnotation : multiValueConstraints ) {
					ConstraintDescriptorImpl<?> descriptor = createComposingConstraintDescriptor(
							constraintAnnotation, overrideParameters, index
					);
					composingConstraints.add( descriptor );
					log.debug( "Adding composing constraint: " + descriptor );
					index++;
				}
			}
		}
	}

	private <U extends Annotation> ConstraintDescriptorImpl<U> createComposingConstraintDescriptor(U declaredAnnotation, Map<ClassIndexWrapper, Map<String, Object>> overrideParameters, int index) {
		//TODO don't quite understand this warning
		//TODO assuming U.getClass() returns Class<U>
		@SuppressWarnings("unchecked")
		final Class<U> annotationType = ( Class<U> ) declaredAnnotation.annotationType();
		return createComposingConstraintDescriptor(
				overrideParameters,
				index,
				declaredAnnotation,
				annotationType
		);
	}

	private <U extends Annotation> ConstraintDescriptorImpl<U> createComposingConstraintDescriptor(Map<ClassIndexWrapper, Map<String, Object>> overrideParameters, int index, U constraintAnnotation, Class<U> annotationType) {
		// use a annotation proxy
		AnnotationDescriptor<U> annotationDescriptor = new AnnotationDescriptor<U>(
				annotationType, getAnnotationParameters( constraintAnnotation )
		);

		// get the right override parameters
		Map<String, Object> overrides = overrideParameters.get(
				new ClassIndexWrapper(
						annotationType, index
				)
		);
		if ( overrides != null ) {
			for ( Map.Entry<String, Object> entry : overrides.entrySet() ) {
				annotationDescriptor.setValue( entry.getKey(), entry.getValue() );
			}
		}

		// groups get inherited from the parent
		annotationDescriptor.setValue( "groups", groups.toArray( new Class<?>[] { } ) );

		U annotationProxy = AnnotationFactory.create( annotationDescriptor );
		return new ConstraintDescriptorImpl<U>(
				annotationProxy, constraintHelper
		);
	}

	/**
	 * A wrapper class to keep track for which compposing constraints (class and index) a given attribute override applies to.
	 */
	private class ClassIndexWrapper {
		final Class<?> clazz;
		final int index;

		ClassIndexWrapper(Class<?> clazz, int index) {
			this.clazz = clazz;
			this.index = index;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			ClassIndexWrapper that = ( ClassIndexWrapper ) o;

			if ( index != that.index ) {
				return false;
			}
			if ( clazz != null ? !clazz.equals( that.clazz ) : that.clazz != null ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = clazz != null ? clazz.hashCode() : 0;
			result = 31 * result + index;
			return result;
		}
	}
}
