/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.descriptor;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import javax.validation.valueextraction.Unwrapping;
import javax.validation.valueextraction.ValidateUnwrappedValue;

import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorDescriptor;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.ConstraintOrigin;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationParameter;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethods;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Describes a single constraint (including its composing constraints).
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Federico Mancini
 * @author Dag Hovland
 */
public class ConstraintDescriptorImpl<T extends Annotation> implements ConstraintDescriptor<T>, Serializable {

	private static final long serialVersionUID = -2563102960314069246L;
	private static final Log LOG = LoggerFactory.make();
	private static final int OVERRIDES_PARAMETER_DEFAULT_INDEX = -1;

	/**
	 * A list of annotations which can be ignored when investigating for composing constraints.
	 */
	private static final List<String> NON_COMPOSING_CONSTRAINT_ANNOTATIONS = Arrays.asList(
			Documented.class.getName(),
			Retention.class.getName(),
			Target.class.getName(),
			Constraint.class.getName(),
			ReportAsSingleViolation.class.getName(),
			Repeatable.class.getName()
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
	@Immutable
	private final List<Class<? extends ConstraintValidator<T, ?>>> constraintValidatorClasses;

	private final List<ConstraintValidatorDescriptor<T>> matchingConstraintValidatorDescriptors;

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
	private final Set<ConstraintDescriptorImpl<?>> composingConstraints;

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
	 * The unwrapping behavior defined on the constraint.
	 */
	private final ValidateUnwrappedValue validateUnwrappedValue;

	/**
	 * Type indicating how composing constraints should be combined. By default this is set to
	 * {@code ConstraintComposition.CompositionType.AND}.
	 */
	private final CompositionType compositionType;

	private final int hashCode;

	@SuppressWarnings("unchecked")
	public ConstraintDescriptorImpl(ConstraintHelper constraintHelper,
			Member member,
			T annotation,
			ElementType type,
			Class<?> implicitGroup,
			ConstraintOrigin definedOn,
			ConstraintType externalConstraintType) {
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

		this.validateUnwrappedValue = determineValidateUnwrappedValue( this.payloads, member, annotationType );

		this.constraintValidatorClasses = constraintHelper.getAllValidatorDescriptors( annotationType )
				.stream()
				.map( ConstraintValidatorDescriptor::getValidatorClass )
				.collect( Collectors.collectingAndThen( Collectors.toList(), Collections::unmodifiableList ) );

		List<ConstraintValidatorDescriptor<T>> crossParameterValidatorDescriptors = constraintHelper.findValidatorDescriptors(
				annotationType,
				ValidationTarget.PARAMETERS
		);
		List<ConstraintValidatorDescriptor<T>> genericValidatorDescriptors = constraintHelper.findValidatorDescriptors(
				annotationType,
				ValidationTarget.ANNOTATED_ELEMENT
		);

		if ( crossParameterValidatorDescriptors.size() > 1 ) {
			throw LOG.getMultipleCrossParameterValidatorClassesException( annotationType );
		}

		this.constraintType = determineConstraintType(
				annotation.annotationType(),
				member,
				type,
				!genericValidatorDescriptors.isEmpty(),
				!crossParameterValidatorDescriptors.isEmpty(),
				externalConstraintType
		);
		this.composingConstraints = parseComposingConstraints( member, constraintHelper, constraintType );
		this.compositionType = parseCompositionType( constraintHelper );
		validateComposingConstraintTypes();

		if ( constraintType == ConstraintType.GENERIC ) {
			this.matchingConstraintValidatorDescriptors = Collections.unmodifiableList( genericValidatorDescriptors );
		}
		else {
			this.matchingConstraintValidatorDescriptors = Collections.unmodifiableList( crossParameterValidatorDescriptors );
		}

		this.hashCode = annotation.hashCode();
	}

	public ConstraintDescriptorImpl(ConstraintHelper constraintHelper,
			Member member,
			T annotation,
			ElementType type) {
		this( constraintHelper, member, annotation, type, null, ConstraintOrigin.DEFINED_LOCALLY, null );
	}

	public ConstraintDescriptorImpl(ConstraintHelper constraintHelper, Member member,
			T annotation,
			ElementType type,
			ConstraintType constraintType) {
		this( constraintHelper, member, annotation, type, null, ConstraintOrigin.DEFINED_LOCALLY, constraintType );
	}

	@Override
	public T getAnnotation() {
		return annotation;
	}

	public Class<T> getAnnotationType() {
		return annotationType;
	}

	@Override
	public String getMessageTemplate() {
		return (String) getAttributes().get( ConstraintHelper.MESSAGE );
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
		return (ConstraintTarget) attributes.get( ConstraintHelper.VALIDATION_APPLIES_TO );
	}

	@Override
	public ValidateUnwrappedValue validateUnwrappedValue() {
		return validateUnwrappedValue;
	}

	@Override
	public List<Class<? extends ConstraintValidator<T, ?>>> getConstraintValidatorClasses() {
		return constraintValidatorClasses;
	}

	/**
	 * Return all constraint validator descriptors (either generic or cross-parameter) which are registered for the
	 * constraint of this descriptor.
	 *
	 * @return The constraint validator descriptors applying to type of this constraint.
	 */
	public List<ConstraintValidatorDescriptor<T>> getMatchingConstraintValidatorDescriptors() {
		return matchingConstraintValidatorDescriptors;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Set<ConstraintDescriptor<?>> getComposingConstraints() {
		return Collections.<ConstraintDescriptor<?>>unmodifiableSet( composingConstraints );
	}

	public Set<ConstraintDescriptorImpl<?>> getComposingConstraintImpls() {
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
		return hashCode;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ConstraintDescriptorImpl" );
		sb.append( "{annotation=" ).append( StringHelper.toShortString( annotationType ) );
		sb.append( ", payloads=" ).append( payloads );
		sb.append( ", hasComposingConstraints=" ).append( composingConstraints.isEmpty() );
		sb.append( ", isReportAsSingleInvalidConstraint=" ).append( isReportAsSingleInvalidConstraint );
		sb.append( ", elementType=" ).append( elementType );
		sb.append( ", definedOn=" ).append( definedOn );
		sb.append( ", groups=" ).append( groups );
		sb.append( ", attributes=" ).append( attributes );
		sb.append( ", constraintType=" ).append( constraintType );
		sb.append( ", validateUnwrappedValue=" ).append( validateUnwrappedValue );
		sb.append( '}' );
		return sb.toString();
	}

	/**
	 * Determines the type of this constraint. The following rules apply in
	 * descending order:
	 * <ul>
	 * <li>If {@code validationAppliesTo()} is set to either
	 * {@link ConstraintTarget#RETURN_VALUE} or
	 * {@link ConstraintTarget#PARAMETERS}, this value will be considered.</li>
	 * <li>Otherwise, if the constraint is either purely generic or purely
	 * cross-parameter as per its validators, that value will be considered.</li>
	 * <li>Otherwise, if the constraint is not on an executable, it is
	 * considered generic.</li>
	 * <li>Otherwise, the type will be determined based on exclusive existence
	 * of parameters and return value.</li>
	 * <li>If that also is not possible, determination fails (i.e. the user must
	 * specify the target explicitly).</li>
	 * </ul>
	 *
	 * @param member The annotated member
	 * @param elementType The type of the annotated element
	 * @param hasGenericValidators Whether the constraint has at least one generic validator or
	 * not
	 * @param hasCrossParameterValidator Whether the constraint has a cross-parameter validator
	 * @param externalConstraintType constraint type as derived from external context, e.g. for
	 * constraints declared in XML via {@code &lt;return-value/gt;}
	 *
	 * @return The type of this constraint
	 */
	private ConstraintType determineConstraintType(Class<? extends Annotation> constraintAnnotationType,
			Member member,
			ElementType elementType,
			boolean hasGenericValidators,
			boolean hasCrossParameterValidator,
			ConstraintType externalConstraintType) {
		ConstraintTarget constraintTarget = (ConstraintTarget) attributes.get( ConstraintHelper.VALIDATION_APPLIES_TO );
		ConstraintType constraintType = null;
		boolean isExecutable = isExecutable( elementType );

		//target explicitly set to RETURN_VALUE
		if ( constraintTarget == ConstraintTarget.RETURN_VALUE ) {
			if ( !isExecutable ) {
				throw LOG.getParametersOrReturnValueConstraintTargetGivenAtNonExecutableException(
						annotationType,
						ConstraintTarget.RETURN_VALUE
				);
			}
			constraintType = ConstraintType.GENERIC;
		}
		//target explicitly set to PARAMETERS
		else if ( constraintTarget == ConstraintTarget.PARAMETERS ) {
			if ( !isExecutable ) {
				throw LOG.getParametersOrReturnValueConstraintTargetGivenAtNonExecutableException(
						annotationType,
						ConstraintTarget.PARAMETERS
				);
			}
			constraintType = ConstraintType.CROSS_PARAMETER;
		}
		//target set by external context (e.g. <return-value> element in XML or returnValue() method in prog. API)
		else if ( externalConstraintType != null ) {
			constraintType = externalConstraintType;
		}
		//target set to IMPLICIT or not set at all
		else {
			//try to derive the type from the existing validators
			if ( hasGenericValidators && !hasCrossParameterValidator ) {
				constraintType = ConstraintType.GENERIC;
			}
			else if ( !hasGenericValidators && hasCrossParameterValidator ) {
				constraintType = ConstraintType.CROSS_PARAMETER;
			}
			else if ( !isExecutable ) {
				constraintType = ConstraintType.GENERIC;
			}
			else if ( constraintAnnotationType.isAnnotationPresent( SupportedValidationTarget.class ) ) {
				SupportedValidationTarget supportedValidationTarget = constraintAnnotationType.getAnnotation( SupportedValidationTarget.class );
				if ( supportedValidationTarget.value().length == 1 ) {
					constraintType = supportedValidationTarget.value()[0] == ValidationTarget.ANNOTATED_ELEMENT ? ConstraintType.GENERIC : ConstraintType.CROSS_PARAMETER;
				}
			}

			//try to derive from existence of parameters/return value
			else {
				boolean hasParameters = hasParameters( member );
				boolean hasReturnValue = hasReturnValue( member );

				if ( !hasParameters && hasReturnValue ) {
					constraintType = ConstraintType.GENERIC;
				}
				else if ( hasParameters && !hasReturnValue ) {
					constraintType = ConstraintType.CROSS_PARAMETER;
				}
			}
		}

		// Now we are out of luck
		if ( constraintType == null ) {
			throw LOG.getImplicitConstraintTargetInAmbiguousConfigurationException( annotationType );
		}

		if ( constraintType == ConstraintType.CROSS_PARAMETER ) {
			validateCrossParameterConstraintType( member, hasCrossParameterValidator );
		}

		return constraintType;
	}

	private static ValidateUnwrappedValue determineValidateUnwrappedValue(Set<Class<? extends Payload>> payloads, Member member, Class<? extends Annotation> annotationType) {
		if ( payloads.contains( Unwrapping.Unwrap.class ) ) {
			if ( payloads.contains( Unwrapping.Skip.class ) ) {
				throw LOG.getInvalidUnwrappingConfigurationForConstraintException( member, annotationType );
			}

			return ValidateUnwrappedValue.YES;
		}

		if ( payloads.contains( Unwrapping.Skip.class ) ) {
			return ValidateUnwrappedValue.NO;
		}

		return ValidateUnwrappedValue.DEFAULT;
	}

	private void validateCrossParameterConstraintType(Member member, boolean hasCrossParameterValidator) {
		if ( !hasCrossParameterValidator ) {
			throw LOG.getCrossParameterConstraintHasNoValidatorException( annotationType );
		}
		else if ( member == null ) {
			throw LOG.getCrossParameterConstraintOnClassException( annotationType );
		}
		else if ( member instanceof Field ) {
			throw LOG.getCrossParameterConstraintOnFieldException( annotationType, member );
		}
		else if ( !hasParameters( member ) ) {
			throw LOG.getCrossParameterConstraintOnMethodWithoutParametersException(
					annotationType,
					(Executable) member
			);
		}
	}

	/**
	 * Asserts that this constraint and all its composing constraints share the
	 * same constraint type (generic or cross-parameter).
	 */
	private void validateComposingConstraintTypes() {
		for ( ConstraintDescriptorImpl<?> composingConstraint : composingConstraints ) {
			if ( composingConstraint.constraintType != constraintType ) {
				throw LOG.getComposedAndComposingConstraintsHaveDifferentTypesException(
						annotationType,
						composingConstraint.annotationType,
						constraintType,
						composingConstraint.constraintType
				);
			}
		}
	}

	private boolean hasParameters(Member member) {
		boolean hasParameters = false;
		if ( member instanceof Constructor ) {
			Constructor<?> constructor = (Constructor<?>) member;
			hasParameters = constructor.getParameterTypes().length > 0;
		}
		else if ( member instanceof Method ) {
			Method method = (Method) member;
			hasParameters = method.getParameterTypes().length > 0;
		}
		return hasParameters;
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

	private boolean isExecutable(ElementType elementType) {
		return elementType == ElementType.METHOD || elementType == ElementType.CONSTRUCTOR;
	}

	@SuppressWarnings("unchecked")
	private Set<Class<? extends Payload>> buildPayloadSet(T annotation) {
		Set<Class<? extends Payload>> payloadSet = newHashSet();
		Class<Payload>[] payloadFromAnnotation;
		try {
			//TODO be extra safe and make sure this is an array of Payload
			payloadFromAnnotation = run(
					GetAnnotationParameter.action( annotation, ConstraintHelper.PAYLOAD, Class[].class )
			);
		}
		catch (ValidationException e) {
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
		final Class<?>[] groupsFromAnnotation = run(
				GetAnnotationParameter.action( annotation, ConstraintHelper.GROUPS, Class[].class )
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

	private Map<String, Object> buildAnnotationParameterMap(Annotation annotation) {
		final Method[] declaredMethods = run( GetDeclaredMethods.action( annotation.annotationType() ) );
		Map<String, Object> parameters = newHashMap( declaredMethods.length );
		for ( Method m : declaredMethods ) {
			Object value = run( GetAnnotationParameter.action( annotation, m.getName(), Object.class ) );
			parameters.put( m.getName(), value );
		}
		return Collections.unmodifiableMap( parameters );
	}

	private Map<ClassIndexWrapper, Map<String, Object>> parseOverrideParameters() {
		Map<ClassIndexWrapper, Map<String, Object>> overrideParameters = newHashMap();
		final Method[] methods = run( GetDeclaredMethods.action( annotationType ) );
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
		Object value = run( GetAnnotationParameter.action( annotation, m.getName(), Object.class ) );
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
		final Method method = run( GetMethod.action( overridesAttribute.constraint(), overridesAttribute.name() ) );
		if ( method == null ) {
			throw LOG.getOverriddenConstraintAttributeNotFoundException( overridesAttribute.name() );
		}
		Class<?> returnTypeOfOverriddenConstraint = method.getReturnType();
		if ( !returnTypeOfOverriddenConstraint.equals( m.getReturnType() ) ) {
			throw LOG.getWrongAttributeTypeForOverriddenConstraintException(
					returnTypeOfOverriddenConstraint,
					m.getReturnType()
			);
		}
	}

	private Set<ConstraintDescriptorImpl<?>> parseComposingConstraints(Member member, ConstraintHelper constraintHelper, ConstraintType constraintType) {
		Set<ConstraintDescriptorImpl<?>> composingConstraintsSet = newHashSet();
		Map<ClassIndexWrapper, Map<String, Object>> overrideParameters = parseOverrideParameters();
		Map<Class<? extends Annotation>, ComposingConstraintAnnotationLocation> composingConstraintLocations = new HashMap<>();

		for ( Annotation declaredAnnotation : annotationType.getDeclaredAnnotations() ) {
			Class<? extends Annotation> declaredAnnotationType = declaredAnnotation.annotationType();
			if ( NON_COMPOSING_CONSTRAINT_ANNOTATIONS.contains( declaredAnnotationType.getName() ) ) {
				// ignore the usual suspects which will be in almost any constraint, but are no composing constraint
				continue;
			}

			if ( constraintHelper.isConstraintAnnotation( declaredAnnotationType ) ) {
				if ( composingConstraintLocations.containsKey( declaredAnnotationType )
						&& !ComposingConstraintAnnotationLocation.DIRECT.equals( composingConstraintLocations.get( declaredAnnotationType ) ) ) {
					throw LOG.getCannotMixDirectAnnotationAndListContainerOnComposedConstraintException( annotationType, declaredAnnotationType );
				}

				ConstraintDescriptorImpl<?> descriptor = createComposingConstraintDescriptor(
						member,
						overrideParameters,
						OVERRIDES_PARAMETER_DEFAULT_INDEX,
						declaredAnnotation,
						constraintType,
						constraintHelper
				);
				composingConstraintsSet.add( descriptor );
				composingConstraintLocations.put( declaredAnnotationType, ComposingConstraintAnnotationLocation.DIRECT );
				LOG.debugf( "Adding composing constraint: %s.", descriptor );
			}
			else if ( constraintHelper.isMultiValueConstraint( declaredAnnotationType ) ) {
				List<Annotation> multiValueConstraints = constraintHelper.getConstraintsFromMultiValueConstraint( declaredAnnotation );
				int index = 0;
				for ( Annotation constraintAnnotation : multiValueConstraints ) {
					if ( composingConstraintLocations.containsKey( constraintAnnotation.annotationType() )
							&& !ComposingConstraintAnnotationLocation.IN_CONTAINER.equals( composingConstraintLocations.get( constraintAnnotation.annotationType() ) ) ) {
						throw LOG.getCannotMixDirectAnnotationAndListContainerOnComposedConstraintException( annotationType, constraintAnnotation.annotationType() );
					}

					ConstraintDescriptorImpl<?> descriptor = createComposingConstraintDescriptor(
							member,
							overrideParameters,
							index,
							constraintAnnotation,
							constraintType,
							constraintHelper
					);
					composingConstraintsSet.add( descriptor );
					composingConstraintLocations.put( constraintAnnotation.annotationType(), ComposingConstraintAnnotationLocation.IN_CONTAINER );
					LOG.debugf( "Adding composing constraint: %s.", descriptor );
					index++;
				}
			}
		}
		return Collections.unmodifiableSet( composingConstraintsSet );
	}

	private CompositionType parseCompositionType(ConstraintHelper constraintHelper) {
		for ( Annotation declaredAnnotation : annotationType.getDeclaredAnnotations() ) {
			Class<? extends Annotation> declaredAnnotationType = declaredAnnotation.annotationType();
			if ( NON_COMPOSING_CONSTRAINT_ANNOTATIONS.contains( declaredAnnotationType.getName() ) ) {
				// ignore the usual suspects which will be in almost any constraint, but are no composing constraint
				continue;
			}

			if ( constraintHelper.isConstraintComposition( declaredAnnotationType ) ) {
				if ( LOG.isDebugEnabled() ) {
					LOG.debugf( "Adding Bool %s.", declaredAnnotationType.getName() );
				}
				return ( (ConstraintComposition) declaredAnnotation ).value();
			}
		}
		return CompositionType.AND;
	}

	private <U extends Annotation> ConstraintDescriptorImpl<U> createComposingConstraintDescriptor(
			Member member,
			Map<ClassIndexWrapper, Map<String, Object>> overrideParameters,
			int index,
			U constraintAnnotation,
			ConstraintType constraintType,
			ConstraintHelper constraintHelper) {

		@SuppressWarnings("unchecked")
		final Class<U> annotationType = (Class<U>) constraintAnnotation.annotationType();

		// use a annotation proxy
		AnnotationDescriptor<U> annotationDescriptor = new AnnotationDescriptor<>(
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
		annotationDescriptor.setValue( ConstraintHelper.GROUPS, groups.toArray( new Class<?>[groups.size()] ) );
		annotationDescriptor.setValue( ConstraintHelper.PAYLOAD, payloads.toArray( new Class<?>[payloads.size()] ) );
		if ( annotationDescriptor.getElements().containsKey( ConstraintHelper.VALIDATION_APPLIES_TO ) ) {
			ConstraintTarget validationAppliesTo = getValidationAppliesTo();

			// composed constraint does not declare validationAppliesTo() itself
			if ( validationAppliesTo == null ) {
				if ( constraintType == ConstraintType.CROSS_PARAMETER ) {
					validationAppliesTo = ConstraintTarget.PARAMETERS;
				}
				else {
					 validationAppliesTo = ConstraintTarget.IMPLICIT;
				}
			}

			annotationDescriptor.setValue( ConstraintHelper.VALIDATION_APPLIES_TO, validationAppliesTo );
		}

		U annotationProxy = AnnotationFactory.create( annotationDescriptor );
		return new ConstraintDescriptorImpl<>(
				constraintHelper, member, annotationProxy, elementType, null, definedOn, constraintType
		);
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <P> P run(PrivilegedAction<P> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
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

			return clazz.equals( that.clazz );
		}

		@Override
		public int hashCode() {
			int result = clazz != null ? clazz.hashCode() : 0;
			result = 31 * result + index;
			return result;
		}

		@Override
		public String toString() {
			return "ClassIndexWrapper [clazz=" + StringHelper.toShortString( clazz ) + ", index=" + index + "]";
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

	/**
	 * The location of a composing constraint.
	 */
	private enum ComposingConstraintAnnotationLocation {
		/**
		 * The annotation is located directly on the class.
		 */
		DIRECT,

		/**
		 * The annotation is defined in a container, typically a List container.
		 */
		IN_CONTAINER
	}
}
