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
package org.hibernate.validation.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintDescriptor;
import javax.validation.Constraint;
import javax.validation.OverridesParameter;
import javax.validation.OverridesParameters;
import javax.validation.ReportAsViolationFromCompositeConstraint;
import javax.validation.ValidationException;
import javax.validation.groups.Default;

import org.slf4j.Logger;

import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.ReflectionHelper;
import org.hibernate.validation.util.annotationfactory.AnnotationFactory;
import org.hibernate.validation.util.annotationfactory.AnnotationDescriptor;

/**
 * Describe a single constraint.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ConstraintDescriptorImpl<U extends Annotation> implements ConstraintDescriptor {
	private static final Logger log = LoggerFactory.make();
	private static final Class<?>[] DEFAULT_GROUP = new Class<?>[] { Default.class };
	private static final int OVERRIDES_PARAMETER_DEFAULT_INDEX = -1;

	private final U annotation;
	private final Class<? extends ConstraintValidator<U,?>>[] constraintClasses;
	private final Set<Class<?>> groups;
	private final Map<String, Object> parameters;
	private final Set<ConstraintDescriptor> composingConstraints = new HashSet<ConstraintDescriptor>();
	private final Map<ClassIndexWrapper, Map<String, Object>> overrideParameters = new HashMap<ClassIndexWrapper, Map<String, Object>>();
	private final boolean isReportAsSingleInvalidConstraint;

	public ConstraintDescriptorImpl(U annotation, Class<?>[] groups) {
		this( annotation, new HashSet<Class<?>>() );
		if ( groups.length == 0 ) {
			groups = DEFAULT_GROUP;
		}
		this.groups.addAll( Arrays.asList( groups ) );
	}

	public ConstraintDescriptorImpl(U annotation, Set<Class<?>> groups) {
		this.annotation = annotation;
		this.groups = groups;
		this.parameters = getAnnotationParameters( annotation );

		this.isReportAsSingleInvalidConstraint = annotation.annotationType().isAnnotationPresent(
				ReportAsViolationFromCompositeConstraint.class
		);


		if ( ReflectionHelper.isBuiltInConstraintAnnotation( annotation ) ) {
			this.constraintClasses = (Class<? extends ConstraintValidator<U,?>>[])
					ReflectionHelper.getBuiltInConstraints( annotation );
		}
		else {
			Constraint constraint = annotation.annotationType()
					.getAnnotation( Constraint.class );
			this.constraintClasses = (Class<? extends ConstraintValidator<U,?>>[])
					constraint.validatedBy();
		}

		parseOverrideParameters();
		parseComposingConstraints();
	}

	/**
	 * {@inheritDoc}
	 */
	public U getAnnotation() {
		return annotation;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<Class<?>> getGroups() {
		return groups;
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<? extends ConstraintValidator<U,?>>[]
			getConstraintValidatorClasses() {
		return constraintClasses;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<ConstraintDescriptor> getComposingConstraints() {
		return composingConstraints;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isReportAsViolationFromCompositeConstraint() {
		return isReportAsSingleInvalidConstraint;
	}

	@Override
	public String toString() {
		return "ConstraintDescriptorImpl{" +
				"annotation=" + annotation +
				", constraintClasses=" + constraintClasses +
				", groups=" + groups +
				", parameters=" + parameters +
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
				throw new ValidationException( "Unable to read annotation parameters: " + annotation.getClass(), e );
			}
			catch ( InvocationTargetException e ) {
				throw new ValidationException( "Unable to read annotation parameters: " + annotation.getClass(), e );
			}
		}
		return Collections.unmodifiableMap( parameters );
	}

	private void addOverrideParameter(Map<ClassIndexWrapper, Map<String, Object>> overrideParameters, Object value, OverridesParameter... parameters) {

		for ( OverridesParameter parameter : parameters ) {
			ClassIndexWrapper wrapper = new ClassIndexWrapper( parameter.constraint(), parameter.index() );
			Map<String, Object> map = overrideParameters.get( wrapper );
			if ( map == null ) {
				map = new HashMap<String, Object>();
				overrideParameters.put( wrapper, map );
			}
			map.put( parameter.parameter(), value );
		}
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

	private void parseOverrideParameters() {
		// check for overrides
		for ( Method m : annotation.annotationType().getMethods() ) {
			if ( m.getAnnotation( OverridesParameter.class ) != null ) {
				addOverrideParameter(
						overrideParameters, getMethodValue( annotation, m ), m.getAnnotation( OverridesParameter.class )
				);
			}
			else if ( m.getAnnotation( OverridesParameters.class ) != null ) {
				addOverrideParameter(
						overrideParameters,
						getMethodValue( annotation, m ),
						m.getAnnotation( OverridesParameters.class ).value()
				);
			}
		}
	}

	private void parseComposingConstraints() {
		for ( Annotation declaredAnnotation : annotation.annotationType().getDeclaredAnnotations() ) {
			if ( ReflectionHelper.isConstraintAnnotation( declaredAnnotation ) || ReflectionHelper.isBuiltInConstraintAnnotation(
					declaredAnnotation
			) ) {
				ConstraintDescriptorImpl descriptor = createComposingConstraintDescriptor(
						OVERRIDES_PARAMETER_DEFAULT_INDEX,
						declaredAnnotation
				);
				composingConstraints.add( descriptor );
				log.debug( "Adding composing constraint: " + descriptor );
			}
			else if ( ReflectionHelper.isMultiValueConstraint( declaredAnnotation ) ) {
				List<Annotation> multiValueConstraints = ReflectionHelper.getMultiValueConstraints( declaredAnnotation );
				int index = 1;
				for ( Annotation constraintAnnotation : multiValueConstraints ) {
					ConstraintDescriptorImpl descriptor = createComposingConstraintDescriptor(
							index, constraintAnnotation
					);
					composingConstraints.add( descriptor );
					log.debug( "Adding composing constraint: " + descriptor );
					index++;
				}
			}
		}
	}

	private ConstraintDescriptorImpl createComposingConstraintDescriptor(int index, Annotation constraintAnnotation) {
		AnnotationDescriptor annotationDescriptor = new AnnotationDescriptor(
				constraintAnnotation.annotationType(), getAnnotationParameters( constraintAnnotation )
		);
		Map<String, Object> overrides = overrideParameters.get(
				new ClassIndexWrapper(
						constraintAnnotation.annotationType(), index
				)
		);
		if ( overrides != null ) {
			for ( Map.Entry<String, Object> entry : overrides.entrySet() ) {
				annotationDescriptor.setValue( entry.getKey(), entry.getValue() );
			}
		}
		Annotation annotationProxy = AnnotationFactory.create( annotationDescriptor );
		return new ConstraintDescriptorImpl( annotationProxy, groups );
	}

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
