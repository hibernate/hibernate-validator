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
package org.hibernate.validator.internal.metadata.descriptor;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintTarget;
import javax.validation.ConstraintValidator;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.ValidationException;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import javax.validation.groups.Default;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.ConstraintOrigin;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.constraints.CompositionType.AND;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Describes a single constraint (including it's composing constraints).
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Federico Mancini
 * @author Dag Hovland
 */
public class ConstraintDescriptorImpl<T extends Annotation> implements ConstraintDescriptor<T>, Serializable {

	private static final long serialVersionUID = -2563102960314069246L;
	private static final Log log = LoggerFactory.make();
	private static final int OVERRIDES_PARAMETER_DEFAULT_INDEX = -1;
	private static final String GROUPS = "groups";
	private static final String PAYLOAD = "payload";
	private static final String MESSAGE = "message";
	private static final String VALIDATION_APPLIES_TO = "validationAppliesTo";

	/**
	 * A list of annotations which can be ignored when investigating for composing constraints.
	 */
	private static final List<String> NON_COMPOSING_CONSTRAINT_ANNOTATIONS = Arrays.asList(
			Documented.class.getName(),
			Retention.class.getName(),
			Target.class.getName(),
			Constraint.class.getName(),
			ReportAsSingleViolation.class.getName()
	);

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
	 * The single cross parameter constraint validator if there is one. {@code null} otherwise.
	 */
	private final Class<? extends ConstraintValidator<T, ?>> crossParameterConstraintValidatorClass;

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
	 * The origin of the constraint. Defined on the actual root class or somewhere in the class hierarchy
	 */
	private final ConstraintOrigin definedOn;

	/**
	 * The type of this constraint.
	 */
	private final ConstraintType constraintType;

	/**
	 * Type indicating how composing constraints should be combined. By default this is set to
	 * {@code ConstraintComposition.CompositionType.AND}.
	 */
	private CompositionType compositionType = AND;

	@SuppressWarnings("unchecked")
	public ConstraintDescriptorImpl(T annotation,
									ConstraintHelper constraintHelper,
									Class<?> implicitGroup,
									ElementType type,
									ConstraintOrigin definedOn,
									Member member) {
		this.annotation = annotation;
		this.annotationType = (Class<T>) this.annotation.annotationType();
		this.elementType = type;
		this.definedOn = definedOn;
		this.isReportAsSingleInvalidConstraint = annotationType.isAnnotationPresent(
				ReportAsSingleViolation.class
		);

		// HV-181 - To avoid any thread visibility issues we are building the different data structures in tmp variables and
		// then assign them to the final variables
		this.attributes = buildAnnotationParameterMap( annotation );
		this.groups = buildGroupSet( implicitGroup );
		this.payloads = buildPayloadSet( annotation );
		this.constraintValidatorDefinitionClasses = constraintHelper.getValidatorClasses( annotationType );
		this.crossParameterConstraintValidatorClass = findCrossParameterValidatorClass(
				constraintValidatorDefinitionClasses
		);

		this.constraintType = determineConstraintType( member );
		this.composingConstraints = parseComposingConstraints( member, constraintHelper );
	}

	public ConstraintDescriptorImpl(Member member,
									T annotation,
									ConstraintHelper constraintHelper,
									ElementType type,
									ConstraintOrigin definedOn) {
		this( annotation, constraintHelper, null, type, definedOn, member );
	}

	@Override
	public T getAnnotation() {
		return annotation;
	}

	@Override
	public String getMessageTemplate() {
		return (String) getAttributes().get( MESSAGE );
	}

	@Override
	public Set<Class<?>> getGroups() {
		return groups;
	}

	@Override
	public Set<Class<? extends Payload>> getPayload() {
		return payloads;
	}

	@Override
	public ConstraintTarget getValidationAppliesTo() {
		return (ConstraintTarget) attributes.get( VALIDATION_APPLIES_TO );
	}

	@Override
	public List<Class<? extends ConstraintValidator<T, ?>>> getConstraintValidatorClasses() {
		return constraintValidatorDefinitionClasses;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Set<ConstraintDescriptor<?>> getComposingConstraints() {
		return composingConstraints;
	}

	@Override
	public boolean isReportAsSingleViolation() {
		return isReportAsSingleInvalidConstraint;
	}

	public ElementType getElementType() {
		return elementType;
	}

	public ConstraintOrigin getDefinedOn() {
		return definedOn;
	}

	public ConstraintType getConstraintType() {
		return constraintType;
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
		sb.append( ", constraintType=" ).append( constraintType );
		sb.append( '}' );
		return sb.toString();
	}

	private ConstraintType determineConstraintType(Member member) {
		if ( crossParameterConstraintValidatorClass == null ) {
			return ConstraintType.GENERIC;
		}

		// check parameter count and return value
		int parameterCount = getParameterCount( member );
		boolean hasReturnValue = hasReturnValue( member );

		if ( parameterCount == 0 && crossParameterValidatorOnly() ) {
			if ( member == null ) {
				throw log.getCrossParameterConstraintOnClassException( annotationType.getName() );
			}
			else if ( member instanceof Field ) {
				throw log.getCrossParameterConstraintOnFieldException(
						annotationType.getName(),
						member.toString()
				);
			}
			else {
				throw log.getCrossParameterConstraintOnMethodWithoutParametersException(
						annotationType.getName(),
						member.toString()
				);
			}
		}

		if ( member == null || member instanceof Field ) {
			return ConstraintType.GENERIC;
		}

		if ( parameterCount > 0 && !hasReturnValue ) {
			return ConstraintType.CROSS_PARAMETER;
		}

		// we have parameters and return value but only a single cross parameter validator
		if ( parameterCount > 0 && hasReturnValue && crossParameterValidatorOnly() ) {
			return ConstraintType.CROSS_PARAMETER;
		}

		// Now we are out of luck. We have cross parameter and generic validators.
		if ( !attributes.containsKey( VALIDATION_APPLIES_TO ) ) {
			throw log.getGenericAndCrossParameterValidatorWithoutConstraintTargetException( annotationType.getName() );
		}

		ConstraintTarget constraintTarget = (ConstraintTarget) attributes.get( VALIDATION_APPLIES_TO );
		switch ( constraintTarget ) {
			case IMPLICIT: {
				throw log.getImplicitConstraintTargetInAmbiguousConfigurationException( annotationType.getName() );
			}
			case RETURN_VALUE: {
				return ConstraintType.GENERIC;
			}
			case PARAMETERS: {
				return ConstraintType.CROSS_PARAMETER;
			}
		}

		throw log.getUnableToDetermineConstraintType( annotationType.getName() );
	}

	private boolean crossParameterValidatorOnly() {
		return crossParameterConstraintValidatorClass != null && constraintValidatorDefinitionClasses.size() == 1 && !supportsValidationTarget(
				crossParameterConstraintValidatorClass,
				ValidationTarget.ANNOTATED_ELEMENT
		);
	}

	private int getParameterCount(Member member) {
		int parameterCount;
		if ( member instanceof Constructor ) {
			Constructor<?> constructor = (Constructor<?>) member;
			parameterCount = constructor.getParameterTypes().length;
		}
		else if ( member instanceof Method ) {
			Method method = (Method) member;
			parameterCount = method.getParameterTypes().length;
		}
		else {
			// field or type
			parameterCount = 0;
		}
		return parameterCount;
	}

	private boolean hasReturnValue(Member member) {
		boolean hasReturnValue;
		if ( member instanceof Constructor ) {
			hasReturnValue = true;
		}
		else if ( member instanceof Method ) {
			Method method = (Method) member;
			hasReturnValue = method.getGenericReturnType() != void.class;
		}
		else {
			// field or type
			hasReturnValue = false;
		}
		return hasReturnValue;
	}

	@SuppressWarnings("unchecked")
	private Set<Class<? extends Payload>> buildPayloadSet(T annotation) {
		Set<Class<? extends Payload>> payloadSet = newHashSet();
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
		Set<Class<?>> groupSet = newHashSet();
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

	private Class<? extends ConstraintValidator<T, ?>> findCrossParameterValidatorClass(List<Class<? extends ConstraintValidator<T, ?>>> constraintValidatorDefinitionClasses) {
		Class<? extends ConstraintValidator<T, ?>> crossParameterValidatorClass = null;
		boolean crossParameterValidatorFound = false;
		for ( Class<? extends ConstraintValidator<T, ?>> validatorClass : constraintValidatorDefinitionClasses ) {
			if ( crossParameterValidatorFound ) {
				throw log.getMultipleCrossParameterValidatorClassesException( annotationType.getName() );
			}

			crossParameterValidatorFound = supportsValidationTarget( validatorClass, ValidationTarget.PARAMETERS );
			if ( crossParameterValidatorFound ) {
				crossParameterValidatorClass = validatorClass;
				crossParameterValidatorFound = true;
			}
		}
		return crossParameterValidatorClass;
	}

	private boolean supportsValidationTarget(Class<?> validatorClass, ValidationTarget target) {
		SupportedValidationTarget supportedTargetAnnotation = validatorClass.getAnnotation(
				SupportedValidationTarget.class
		);
		if ( supportedTargetAnnotation == null ) {
			return false;
		}
		ValidationTarget[] targets = supportedTargetAnnotation.value();
		for ( ValidationTarget configuredTarget : targets ) {
			if ( configuredTarget.equals( target ) ) {
				return true;
			}
		}
		return false;
	}

	private Map<String, Object> buildAnnotationParameterMap(Annotation annotation) {
		final Method[] declaredMethods = ReflectionHelper.getDeclaredMethods( annotation.annotationType() );
		Map<String, Object> parameters = newHashMap( declaredMethods.length );
		for ( Method m : declaredMethods ) {
			try {
				parameters.put( m.getName(), m.invoke( annotation ) );
			}
			catch ( IllegalAccessException e ) {
				throw log.getUnableToReadAnnotationAttributesException( annotation.getClass(), e );
			}
			catch ( InvocationTargetException e ) {
				throw log.getUnableToReadAnnotationAttributesException( annotation.getClass(), e );
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
			throw log.getUnableToRetrieveAnnotationParameterValueException( e );
		}
		catch ( InvocationTargetException e ) {
			throw log.getUnableToRetrieveAnnotationParameterValueException( e );
		}
		return value;
	}

	private Map<ClassIndexWrapper, Map<String, Object>> parseOverrideParameters() {
		Map<ClassIndexWrapper, Map<String, Object>> overrideParameters = newHashMap();
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
				map = newHashMap();
				overrideParameters.put( wrapper, map );
			}
			map.put( overridesAttribute.name(), value );
		}
	}

	private void ensureAttributeIsOverridable(Method m, OverridesAttribute overridesAttribute) {
		final Method method = ReflectionHelper.getMethod( overridesAttribute.constraint(), overridesAttribute.name() );
		if ( method == null ) {
			throw log.getOverriddenConstraintAttributeNotFoundException( overridesAttribute.name() );
		}
		Class<?> returnTypeOfOverriddenConstraint = method.getReturnType();
		if ( !returnTypeOfOverriddenConstraint.equals( m.getReturnType() ) ) {
			throw log.getWrongAttributeTypeForOverriddenConstraintException(
					returnTypeOfOverriddenConstraint.getName(),
					m.getReturnType()
			);
		}
	}

	private Set<ConstraintDescriptor<?>> parseComposingConstraints(Member member, ConstraintHelper constraintHelper) {
		Set<ConstraintDescriptor<?>> composingConstraintsSet = newHashSet();
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
					log.debugf( "Adding Bool %s.", declaredAnnotationType.getName() );
				}
				continue;
			}

			if ( constraintHelper.isConstraintAnnotation( declaredAnnotationType ) ) {
				ConstraintDescriptorImpl<?> descriptor = createComposingConstraintDescriptor(
						member,
						declaredAnnotation,
						overrideParameters,
						OVERRIDES_PARAMETER_DEFAULT_INDEX,
						constraintHelper
				);
				composingConstraintsSet.add( descriptor );
				log.debugf( "Adding composing constraint: %s.", descriptor );
			}
			else if ( constraintHelper.isMultiValueConstraint( declaredAnnotationType ) ) {
				List<Annotation> multiValueConstraints = constraintHelper.getMultiValueConstraints( declaredAnnotation );
				int index = 0;
				for ( Annotation constraintAnnotation : multiValueConstraints ) {
					ConstraintDescriptorImpl<?> descriptor = createComposingConstraintDescriptor(
							member, constraintAnnotation, overrideParameters, index, constraintHelper
					);
					composingConstraintsSet.add( descriptor );
					log.debugf( "Adding composing constraint: %s.", descriptor );
					index++;
				}
			}
		}
		return Collections.unmodifiableSet( composingConstraintsSet );
	}

	private <U extends Annotation> ConstraintDescriptorImpl<U> createComposingConstraintDescriptor(
			Member member,
			U declaredAnnotation,
			Map<ClassIndexWrapper, Map<String, Object>> overrideParameters,
			int index,
			ConstraintHelper constraintHelper) {
		@SuppressWarnings("unchecked")
		final Class<U> annotationType = (Class<U>) declaredAnnotation.annotationType();
		return createComposingConstraintDescriptor(
				member,
				overrideParameters,
				index,
				declaredAnnotation,
				annotationType,
				constraintHelper
		);
	}

	private <U extends Annotation> ConstraintDescriptorImpl<U> createComposingConstraintDescriptor(
			Member member,
			Map<ClassIndexWrapper, Map<String, Object>> overrideParameters,
			int index,
			U constraintAnnotation,
			Class<U> annotationType,
			ConstraintHelper constraintHelper) {
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

		//propagate inherited attributes to composing constraints
		annotationDescriptor.setValue( GROUPS, groups.toArray( new Class<?>[groups.size()] ) );
		annotationDescriptor.setValue( PAYLOAD, payloads.toArray( new Class<?>[payloads.size()] ) );
		if ( annotationDescriptor.getElements().containsKey( VALIDATION_APPLIES_TO ) ) {
			annotationDescriptor.setValue( VALIDATION_APPLIES_TO, getValidationAppliesTo() );
		}

		U annotationProxy = AnnotationFactory.create( annotationDescriptor );
		return new ConstraintDescriptorImpl<U>(
				member, annotationProxy, constraintHelper, elementType, definedOn
		);
	}

	/**
	 * @param compositionType the compositionType to set
	 */
	private void setCompositionType(CompositionType compositionType) {
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

	/**
	 * The type of a constraint.
	 */
	public enum ConstraintType {
		/**
		 * A non cross parameter constraint.
		 */
		GENERIC,

		/**
		 * A cross parameter constraint.
		 */
		CROSS_PARAMETER
	}
}
