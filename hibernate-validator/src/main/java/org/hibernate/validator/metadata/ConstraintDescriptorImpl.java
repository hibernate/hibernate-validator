/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.metadata;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
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
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.ValidationException;
import javax.validation.groups.Default;
import javax.validation.metadata.ConstraintDescriptor;

import org.slf4j.Logger;

import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.util.LoggerFactory;
import org.hibernate.validator.util.ReflectionHelper;
import org.hibernate.validator.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.util.annotationfactory.AnnotationFactory;

import static org.hibernate.validator.constraints.CompositionType.AND;

/**
 * Describe a single constraint (including it's composing constraints).
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Federico Mancini
 * @author Dag Hovland
 */
public class ConstraintDescriptorImpl<T extends Annotation> implements ConstraintDescriptor<T>, Serializable {

	private static final long serialVersionUID = -2563102960314069246L;
	private static final Logger log = LoggerFactory.make();
	private static final int OVERRIDES_PARAMETER_DEFAULT_INDEX = -1;
	private static final String GROUPS = "groups";
	private static final String PAYLOAD = "payload";

	/**
	 * A list of annotations which can be ignored when investigating for composing constraints.
	 */
	private static final List<String> NON_COMPOSING_CONSTRAINT_ANNOTATIONS = new ArrayList<String>();

	static {
		NON_COMPOSING_CONSTRAINT_ANNOTATIONS.add( Documented.class.getName() );
		NON_COMPOSING_CONSTRAINT_ANNOTATIONS.add( Retention.class.getName() );
		NON_COMPOSING_CONSTRAINT_ANNOTATIONS.add( Target.class.getName() );
		NON_COMPOSING_CONSTRAINT_ANNOTATIONS.add( Constraint.class.getName() );
		NON_COMPOSING_CONSTRAINT_ANNOTATIONS.add( ReportAsSingleViolation.class.getName() );
	}

	/**
	 * The actual constraint annotation.
	 */
	private final T annotation;

	/**
	 * The type of the annotation made instance variable, because {@code annotation.annotationType()} is quite expensive.
	 */
	private final Class<T> annotationType;

	/**
	 * The set of classes implementing the validation for this constraint. See also
	 * {@code ConstraintValidator} resolution algorithm.
	 */
	private final List<Class<? extends ConstraintValidator<T, ?>>> constraintValidatorDefinitionClasses;

	/**
	 * The groups for which to apply this constraint.
	 */
	private final Set<Class<?>> groups;

	/**
	 * The constraint parameters as map. The key is the parameter name and the value the
	 * parameter value as specified in the constraint.
	 */
	private final Map<String, Object> attributes;

	/**
	 * The specified payload of the constraint.
	 */
	private final Set<Class<? extends Payload>> payloads;

	/**
	 * The composing constraints for this constraint.
	 */
	private final Set<ConstraintDescriptor<?>> composingConstraints;

	/**
	 * Flag indicating if in case of a composing constraint a single error or multiple errors should be raised.
	 */
	private final boolean isReportAsSingleInvalidConstraint;

	/**
	 * Describes on which level (<code>TYPE</code>, <code>METHOD</code>, <code>FIELD</code>) the constraint was
	 * defined on.
	 */
	private final ElementType elementType;

	/**
	 * The origin of the constraint. Defined on the actual root class or somehwere in the class hierarchy
	 */
	private final ConstraintOrigin definedOn;

	/**
	 * Type indicating how composing constraints should be combined. By default this is set to {@code ConstraintComposition.CompositionType.AND}.
	 */
	private CompositionType compositionType = AND;


	/**
	 * Handle to the built-in constraint implementations.
	 */
	//TODO Can be made transient since it is only used during object construction. It would be better if we would not have to pass it at all
	private final transient ConstraintHelper constraintHelper;

	public ConstraintDescriptorImpl(T annotation, ConstraintHelper constraintHelper, Class<?> implicitGroup, ElementType type, ConstraintOrigin definedOn) {
		this.annotation = annotation;
		this.annotationType = (Class<T>) this.annotation.annotationType();
		this.constraintHelper = constraintHelper;
		this.elementType = type;
		this.definedOn = definedOn;
		this.isReportAsSingleInvalidConstraint = annotationType.isAnnotationPresent(
				ReportAsSingleViolation.class
		);

		// HV-181 - To avoid and thread visibility issues we are building the different data structures in tmp variables and
		// then assign them to the final variables
		this.attributes = buildAnnotationParameterMap( annotation );
		this.groups = buildGroupSet( implicitGroup );
		this.payloads = buildPayloadSet( annotation );
		this.constraintValidatorDefinitionClasses = findConstraintValidatorClasses();
		this.composingConstraints = parseComposingConstraints();
	}

	public ConstraintDescriptorImpl(T annotation, ConstraintHelper constraintHelper, ElementType type, ConstraintOrigin definedOn) {
		this( annotation, constraintHelper, null, type, definedOn );
	}

	private Set<Class<? extends Payload>> buildPayloadSet(T annotation) {
		Set<Class<? extends Payload>> payloadSet = new HashSet<Class<? extends Payload>>();
		Class<Payload>[] payloadFromAnnotation;
		try {
			//TODO be extra safe and make sure this is an array of Payload
			payloadFromAnnotation = ReflectionHelper.getAnnotationParameter( annotation, PAYLOAD, Class[].class );
		}
		catch ( ValidationException e ) {
			//ignore people not defining payloads
			payloadFromAnnotation = null;
		}
		if ( payloadFromAnnotation != null ) {
			payloadSet.addAll( Arrays.asList( payloadFromAnnotation ) );
		}
		return Collections.unmodifiableSet( payloadSet );
	}

	private Set<Class<?>> buildGroupSet(Class<?> implicitGroup) {
		Set<Class<?>> groupSet = new HashSet<Class<?>>();
		final Class<?>[] groupsFromAnnotation = ReflectionHelper.getAnnotationParameter(
				annotation, GROUPS, Class[].class
		);
		if ( groupsFromAnnotation.length == 0 ) {
			groupSet.add( Default.class );
		}
		else {
			groupSet.addAll( Arrays.asList( groupsFromAnnotation ) );
		}

		// if the constraint is part of the Default group it is automatically part of the implicit group as well
		if ( implicitGroup != null && groupSet.contains( Default.class ) ) {
			groupSet.add( implicitGroup );
		}
		return Collections.unmodifiableSet( groupSet );
	}

	private List<Class<? extends ConstraintValidator<T, ?>>> findConstraintValidatorClasses() {
		final List<Class<? extends ConstraintValidator<T, ?>>> constraintValidatorClasses = new ArrayList<Class<? extends ConstraintValidator<T, ?>>>();
		if ( constraintHelper.containsConstraintValidatorDefinition( annotationType ) ) {
			for ( Class<? extends ConstraintValidator<T, ?>> validator : constraintHelper
					.getConstraintValidatorDefinition( annotationType ) ) {
				constraintValidatorClasses.add( validator );
			}
			return Collections.unmodifiableList( constraintValidatorClasses );
		}

		List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> constraintDefinitionClasses = new ArrayList<Class<? extends ConstraintValidator<? extends Annotation, ?>>>();
		if ( constraintHelper.isBuiltinConstraint( annotationType ) ) {
			constraintDefinitionClasses.addAll( constraintHelper.getBuiltInConstraints( annotationType ) );
		}
		else {
			Class<? extends ConstraintValidator<?, ?>>[] validatedBy = annotationType
					.getAnnotation( Constraint.class )
					.validatedBy();
			constraintDefinitionClasses.addAll( Arrays.asList( validatedBy ) );
		}

		constraintHelper.addConstraintValidatorDefinition(
				annotationType, constraintDefinitionClasses
		);

		for ( Class<? extends ConstraintValidator<? extends Annotation, ?>> validator : constraintDefinitionClasses ) {
			@SuppressWarnings("unchecked")
			Class<? extends ConstraintValidator<T, ?>> safeValidator = (Class<? extends ConstraintValidator<T, ?>>) validator;
			constraintValidatorClasses.add( safeValidator );
		}
		return Collections.unmodifiableList( constraintValidatorClasses );
	}

	public T getAnnotation() {
		return annotation;
	}

	public Set<Class<?>> getGroups() {
		return groups;
	}

	public Set<Class<? extends Payload>> getPayload() {
		return payloads;
	}

	public List<Class<? extends ConstraintValidator<T, ?>>> getConstraintValidatorClasses() {
		return constraintValidatorDefinitionClasses;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public Set<ConstraintDescriptor<?>> getComposingConstraints() {
		return composingConstraints;
	}

	public boolean isReportAsSingleViolation() {
		return isReportAsSingleInvalidConstraint;
	}

	public ElementType getElementType() {
		return elementType;
	}

	public ConstraintOrigin getDefinedOn() {
		return definedOn;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ConstraintDescriptorImpl<?> that = (ConstraintDescriptorImpl<?>) o;

		if ( annotation != null ? !annotation.equals( that.annotation ) : that.annotation != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return annotation != null ? annotation.hashCode() : 0;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ConstraintDescriptorImpl" );
		sb.append( "{annotation=" ).append( annotationType.getName() );
		sb.append( ", payloads=" ).append( payloads );
		sb.append( ", hasComposingConstraints=" ).append( composingConstraints.isEmpty() );
		sb.append( ", isReportAsSingleInvalidConstraint=" ).append( isReportAsSingleInvalidConstraint );
		sb.append( ", elementType=" ).append( elementType );
		sb.append( ", definedOn=" ).append( definedOn );
		sb.append( ", groups=" ).append( groups );
		sb.append( ", attributes=" ).append( attributes );
		sb.append( '}' );
		return sb.toString();
	}

	private Map<String, Object> buildAnnotationParameterMap(Annotation annotation) {
		final Method[] declaredMethods = ReflectionHelper.getDeclaredMethods( annotation.annotationType() );
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
			throw new ValidationException( "Unable to retrieve annotation parameter value.", e );
		}
		catch ( InvocationTargetException e ) {
			throw new ValidationException( "Unable to retrieve annotation parameter value.", e );
		}
		return value;
	}

	private Map<ClassIndexWrapper, Map<String, Object>> parseOverrideParameters() {
		Map<ClassIndexWrapper, Map<String, Object>> overrideParameters = new HashMap<ClassIndexWrapper, Map<String, Object>>();
		final Method[] methods = ReflectionHelper.getDeclaredMethods( annotationType );
		for ( Method m : methods ) {
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
		final Method method = ReflectionHelper.getMethod( overridesAttribute.constraint(), overridesAttribute.name() );
		if ( method == null ) {
			throw new ConstraintDefinitionException(
					"Overridden constraint does not define an attribute with name " + overridesAttribute.name()
			);
		}
		Class<?> returnTypeOfOverriddenConstraint = method.getReturnType();
		if ( !returnTypeOfOverriddenConstraint.equals( m.getReturnType() ) ) {
			String message = "The overriding type of a composite constraint must be identical to the overridden one. Expected " + returnTypeOfOverriddenConstraint
					.getName() + " found " + m.getReturnType();
			throw new ConstraintDefinitionException( message );
		}
	}

	private Set<ConstraintDescriptor<?>> parseComposingConstraints() {
		Set<ConstraintDescriptor<?>> composingConstraintsSet = new HashSet<ConstraintDescriptor<?>>();
		Map<ClassIndexWrapper, Map<String, Object>> overrideParameters = parseOverrideParameters();

		for ( Annotation declaredAnnotation : annotationType.getDeclaredAnnotations() ) {
			Class<? extends Annotation> declaredAnnotationType = declaredAnnotation.annotationType();
			if ( NON_COMPOSING_CONSTRAINT_ANNOTATIONS.contains( declaredAnnotationType.getName() ) ) {
				// ignore the usual suspects which will be in almost any constraint, but are no composing constraint
				continue;
			}

			//If there is a @ConstraintCompositionType annotation, set its value as the local compositionType field
			if ( constraintHelper.isConstraintComposition( declaredAnnotationType ) ) {
				this.setCompositionType( ( (ConstraintComposition) declaredAnnotation ).value() );
				if ( log.isDebugEnabled() ) {
					log.debug( "Adding Bool" + declaredAnnotationType.getName() );
				}
				continue;
			}

			if ( constraintHelper.isConstraintAnnotation( declaredAnnotationType )
					|| constraintHelper.isBuiltinConstraint( declaredAnnotationType ) ) {
				ConstraintDescriptorImpl<?> descriptor = createComposingConstraintDescriptor(
						declaredAnnotation, overrideParameters, OVERRIDES_PARAMETER_DEFAULT_INDEX
				);
				composingConstraintsSet.add( descriptor );
				if ( log.isDebugEnabled() ) {
					log.debug( "Adding composing constraint: " + descriptor );
				}
			}
			else if ( constraintHelper.isMultiValueConstraint( declaredAnnotationType ) ) {
				List<Annotation> multiValueConstraints = constraintHelper.getMultiValueConstraints( declaredAnnotation );
				int index = 0;
				for ( Annotation constraintAnnotation : multiValueConstraints ) {
					ConstraintDescriptorImpl<?> descriptor = createComposingConstraintDescriptor(
							constraintAnnotation, overrideParameters, index
					);
					composingConstraintsSet.add( descriptor );
					if ( log.isDebugEnabled() ) {
						log.debug( "Adding composing constraint: " + descriptor );
					}
					index++;
				}
			}
		}
		return Collections.unmodifiableSet( composingConstraintsSet );
	}

	private <U extends Annotation> ConstraintDescriptorImpl<U> createComposingConstraintDescriptor(U declaredAnnotation, Map<ClassIndexWrapper, Map<String, Object>> overrideParameters, int index) {
		@SuppressWarnings("unchecked")
		final Class<U> annotationType = (Class<U>) declaredAnnotation.annotationType();
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
				annotationType, buildAnnotationParameterMap( constraintAnnotation )
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
		annotationDescriptor.setValue( GROUPS, groups.toArray( new Class<?>[groups.size()] ) );

		// HV-183 - payloads are propagated to composing constraints
		annotationDescriptor.setValue( PAYLOAD, payloads.toArray( new Class<?>[payloads.size()] ) );

		U annotationProxy = AnnotationFactory.create( annotationDescriptor );
		return new ConstraintDescriptorImpl<U>(
				annotationProxy, constraintHelper, elementType, definedOn
		);
	}

	/**
	 * @param compositionType the compositionType to set
	 */
	public void setCompositionType(CompositionType compositionType) {
		this.compositionType = compositionType;
	}

	/**
	 * @return the compositionType
	 */
	public CompositionType getCompositionType() {
		return compositionType;
	}

	/**
	 * A wrapper class to keep track for which composing constraints (class and index) a given attribute override applies to.
	 */
	private class ClassIndexWrapper {
		final Class<?> clazz;
		final int index;

		ClassIndexWrapper(Class<?> clazz, int index) {
			this.clazz = clazz;
			this.index = index;
		}

		@Override
		@SuppressWarnings("SimplifiableIfStatement")
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			@SuppressWarnings("unchecked") // safe due to the check above
					ClassIndexWrapper that = (ClassIndexWrapper) o;

			if ( index != that.index ) {
				return false;
			}
			if ( clazz != null && !clazz.equals( that.clazz ) ) {
				return false;
			}
			if ( clazz == null && that.clazz != null ) {
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
