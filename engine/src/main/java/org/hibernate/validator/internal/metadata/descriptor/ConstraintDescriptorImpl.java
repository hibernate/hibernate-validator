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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintTarget;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.OverridesAttribute;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import jakarta.validation.groups.Default;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ValidateUnwrappedValue;
import jakarta.validation.valueextraction.Unwrapping;

import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorDescriptor;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.ConstraintOrigin;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationAttributes;
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
 * @author Guillaume Smet
 */
public class ConstraintDescriptorImpl<T extends Annotation> implements ConstraintDescriptor<T>, Serializable {

	private static final long serialVersionUID = -2563102960314069246L;
	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );
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
			Repeatable.class.getName(),
			Deprecated.class.getName()
	);

	/**
	 * The annotation descriptor - accessing the annotation information has a cost so we do it only once.
	 */
	private final ConstraintAnnotationDescriptor<T> annotationDescriptor;

	/**
	 * The set of classes implementing the validation for this constraint. See also
	 * {@code ConstraintValidator} resolution algorithm.
	 */
	@Immutable
	private final List<Class<? extends ConstraintValidator<T, ?>>> constraintValidatorClasses;

	/**
	 * This field is transient as the implementations of  {@link ConstraintValidatorDescriptor} might not be serializable.
	 * Typically {@code ClassBasedValidatorDescriptor} might contain a {@code ParameterizedTypeImpl}, which is not serializable.
	 */
	@Immutable
	private final transient List<ConstraintValidatorDescriptor<T>> matchingConstraintValidatorDescriptors;

	/**
	 * The groups for which to apply this constraint.
	 */
	@Immutable
	private final Set<Class<?>> groups;

	/**
	 * The specified payload of the constraint.
	 */
	@Immutable
	private final Set<Class<? extends Payload>> payloads;

	/**
	 * The composing constraints for this constraint.
	 */
	@Immutable
	private final Set<ConstraintDescriptorImpl<?>> composingConstraints;

	/**
	 * Flag indicating if in case of a composing constraint a single error or multiple errors should be raised.
	 */
	private final boolean isReportAsSingleInvalidConstraint;

	/**
	 * Describes on which level ({@code TYPE}, {@code METHOD}, {@code FIELD}...) the constraint was
	 * defined on.
	 */
	private final ConstraintLocationKind constraintLocationKind;

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
	private final ValidateUnwrappedValue valueUnwrapping;

	/**
	 * The target of the constraint.
	 */
	private final ConstraintTarget validationAppliesTo;

	/**
	 * Type indicating how composing constraints should be combined. By default this is set to
	 * {@code ConstraintComposition.CompositionType.AND}.
	 */
	private final CompositionType compositionType;

	private final int hashCode;

	public ConstraintDescriptorImpl(ConstraintHelper constraintHelper,
			Constrainable constrainable,
			ConstraintAnnotationDescriptor<T> annotationDescriptor,
			ConstraintLocationKind constraintLocationKind,
			Class<?> implicitGroup,
			ConstraintOrigin definedOn,
			ConstraintType externalConstraintType) {
		this.annotationDescriptor = annotationDescriptor;
		this.constraintLocationKind = constraintLocationKind;
		this.definedOn = definedOn;
		this.isReportAsSingleInvalidConstraint = annotationDescriptor.getType().isAnnotationPresent(
				ReportAsSingleViolation.class
		);

		// HV-181 - To avoid any thread visibility issues we are building the different data structures in tmp variables and
		// then assign them to the final variables
		this.groups = buildGroupSet( annotationDescriptor, implicitGroup );
		this.payloads = buildPayloadSet( annotationDescriptor );

		this.valueUnwrapping = determineValueUnwrapping( this.payloads, constrainable, annotationDescriptor.getType() );

		this.validationAppliesTo = determineValidationAppliesTo( annotationDescriptor );

		this.constraintValidatorClasses = constraintHelper.getAllValidatorDescriptors( annotationDescriptor.getType() )
				.stream()
				.map( ConstraintValidatorDescriptor::getValidatorClass )
				.collect( Collectors.collectingAndThen( Collectors.toList(), CollectionHelper::toImmutableList ) );

		List<ConstraintValidatorDescriptor<T>> crossParameterValidatorDescriptors = CollectionHelper.toImmutableList( constraintHelper.findValidatorDescriptors(
				annotationDescriptor.getType(),
				ValidationTarget.PARAMETERS
		) );
		List<ConstraintValidatorDescriptor<T>> genericValidatorDescriptors = CollectionHelper.toImmutableList( constraintHelper.findValidatorDescriptors(
				annotationDescriptor.getType(),
				ValidationTarget.ANNOTATED_ELEMENT
		) );

		if ( crossParameterValidatorDescriptors.size() > 1 ) {
			throw LOG.getMultipleCrossParameterValidatorClassesException( annotationDescriptor.getType() );
		}

		this.constraintType = determineConstraintType(
				annotationDescriptor.getType(),
				constrainable,
				!genericValidatorDescriptors.isEmpty(),
				!crossParameterValidatorDescriptors.isEmpty(),
				externalConstraintType
		);
		this.composingConstraints = parseComposingConstraints( constraintHelper, constrainable, constraintType );
		this.compositionType = parseCompositionType( constraintHelper );
		validateComposingConstraintTypes();

		if ( constraintType == ConstraintType.GENERIC ) {
			this.matchingConstraintValidatorDescriptors = CollectionHelper.toImmutableList( genericValidatorDescriptors );
		}
		else {
			this.matchingConstraintValidatorDescriptors = CollectionHelper.toImmutableList( crossParameterValidatorDescriptors );
		}

		this.hashCode = annotationDescriptor.hashCode();
	}

	public ConstraintDescriptorImpl(ConstraintHelper constraintHelper,
			Constrainable constrainable,
			ConstraintAnnotationDescriptor<T> annotationDescriptor,
			ConstraintLocationKind constraintLocationKind) {
		this( constraintHelper, constrainable, annotationDescriptor, constraintLocationKind, null, ConstraintOrigin.DEFINED_LOCALLY, null );
	}

	public ConstraintDescriptorImpl(ConstraintHelper constraintHelper,
			Constrainable constrainable,
			ConstraintAnnotationDescriptor<T> annotationDescriptor,
			ConstraintLocationKind constraintLocationKind,
			ConstraintType constraintType) {
		this( constraintHelper, constrainable, annotationDescriptor, constraintLocationKind, null, ConstraintOrigin.DEFINED_LOCALLY, constraintType );
	}

	public ConstraintAnnotationDescriptor<T> getAnnotationDescriptor() {
		return annotationDescriptor;
	}

	@Override
	public T getAnnotation() {
		return annotationDescriptor.getAnnotation();
	}

	public Class<T> getAnnotationType() {
		return annotationDescriptor.getType();
	}

	@Override
	public String getMessageTemplate() {
		return annotationDescriptor.getMessage();
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
		return validationAppliesTo;
	}

	@Override
	public ValidateUnwrappedValue getValueUnwrapping() {
		return valueUnwrapping;
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
		return annotationDescriptor.getAttributes();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<ConstraintDescriptor<?>> getComposingConstraints() {
		return (Set<ConstraintDescriptor<?>>) (Object) composingConstraints;
	}

	public Set<ConstraintDescriptorImpl<?>> getComposingConstraintImpls() {
		return composingConstraints;
	}

	@Override
	public boolean isReportAsSingleViolation() {
		return isReportAsSingleInvalidConstraint;
	}

	public ConstraintLocationKind getConstraintLocationKind() {
		return constraintLocationKind;
	}

	public ConstraintOrigin getDefinedOn() {
		return definedOn;
	}

	public ConstraintType getConstraintType() {
		return constraintType;
	}

	@Override
	public <U> U unwrap(Class<U> type) {
		throw LOG.getUnwrappingOfConstraintDescriptorNotSupportedYetException();
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

		if ( annotationDescriptor != null ? !annotationDescriptor.equals( that.annotationDescriptor ) : that.annotationDescriptor != null ) {
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
		sb.append( "{annotation=" ).append( StringHelper.toShortString( annotationDescriptor.getType() ) );
		sb.append( ", payloads=" ).append( payloads );
		sb.append( ", hasComposingConstraints=" ).append( composingConstraints.isEmpty() );
		sb.append( ", isReportAsSingleInvalidConstraint=" ).append( isReportAsSingleInvalidConstraint );
		sb.append( ", constraintLocationKind=" ).append( constraintLocationKind );
		sb.append( ", definedOn=" ).append( definedOn );
		sb.append( ", groups=" ).append( groups );
		sb.append( ", attributes=" ).append( annotationDescriptor.getAttributes() );
		sb.append( ", constraintType=" ).append( constraintType );
		sb.append( ", valueUnwrapping=" ).append( valueUnwrapping );
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
	 * @param constrainable The annotated member
	 * @param hasGenericValidators Whether the constraint has at least one generic validator or
	 * not
	 * @param hasCrossParameterValidator Whether the constraint has a cross-parameter validator
	 * @param externalConstraintType constraint type as derived from external context, e.g. for
	 * constraints declared in XML via {@code &lt;return-value/gt;}
	 *
	 * @return The type of this constraint
	 */
	private ConstraintType determineConstraintType(Class<? extends Annotation> constraintAnnotationType,
			Constrainable constrainable,
			boolean hasGenericValidators,
			boolean hasCrossParameterValidator,
			ConstraintType externalConstraintType) {
		ConstraintTarget constraintTarget = validationAppliesTo;
		ConstraintType constraintType = null;
		boolean isExecutable = constraintLocationKind.isExecutable();

		//target explicitly set to RETURN_VALUE
		if ( constraintTarget == ConstraintTarget.RETURN_VALUE ) {
			if ( !isExecutable ) {
				throw LOG.getParametersOrReturnValueConstraintTargetGivenAtNonExecutableException(
						annotationDescriptor.getType(),
						ConstraintTarget.RETURN_VALUE
				);
			}
			constraintType = ConstraintType.GENERIC;
		}
		//target explicitly set to PARAMETERS
		else if ( constraintTarget == ConstraintTarget.PARAMETERS ) {
			if ( !isExecutable ) {
				throw LOG.getParametersOrReturnValueConstraintTargetGivenAtNonExecutableException(
						annotationDescriptor.getType(),
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
			//hence look only if it is a callable
			else if ( constrainable instanceof Callable ) {
				boolean hasParameters = constrainable.as( Callable.class ).hasParameters();
				boolean hasReturnValue = constrainable.as( Callable.class ).hasReturnValue();

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
			throw LOG.getImplicitConstraintTargetInAmbiguousConfigurationException( annotationDescriptor.getType() );
		}

		if ( constraintType == ConstraintType.CROSS_PARAMETER ) {
			validateCrossParameterConstraintType( constrainable, hasCrossParameterValidator );
		}

		return constraintType;
	}

	private static ValidateUnwrappedValue determineValueUnwrapping(Set<Class<? extends Payload>> payloads, Constrainable constrainable, Class<? extends Annotation> annotationType) {
		if ( payloads.contains( Unwrapping.Unwrap.class ) ) {
			if ( payloads.contains( Unwrapping.Skip.class ) ) {
				throw LOG.getInvalidUnwrappingConfigurationForConstraintException( constrainable, annotationType );
			}

			return ValidateUnwrappedValue.UNWRAP;
		}

		if ( payloads.contains( Unwrapping.Skip.class ) ) {
			return ValidateUnwrappedValue.SKIP;
		}

		return ValidateUnwrappedValue.DEFAULT;
	}

	private static ConstraintTarget determineValidationAppliesTo(ConstraintAnnotationDescriptor<?> annotationDescriptor) {
		return annotationDescriptor.getValidationAppliesTo();
	}

	private void validateCrossParameterConstraintType(Constrainable constrainable, boolean hasCrossParameterValidator) {
		if ( !hasCrossParameterValidator ) {
			throw LOG.getCrossParameterConstraintHasNoValidatorException( annotationDescriptor.getType() );
		}
		else if ( constrainable == null ) {
			throw LOG.getCrossParameterConstraintOnClassException( annotationDescriptor.getType() );
		}
		else if ( constrainable instanceof Property ) {
			throw LOG.getCrossParameterConstraintOnFieldException( annotationDescriptor.getType(), constrainable );
		}
		else if ( !constrainable.as( Callable.class ).hasParameters() ) {
			throw LOG.getCrossParameterConstraintOnMethodWithoutParametersException(
					annotationDescriptor.getType(),
					constrainable
			);
		}
	}

	/**
	 * Asserts that this constraint and all its composing constraints share the
	 * same constraint type (generic or cross-parameter).
	 */
	private void validateComposingConstraintTypes() {
		for ( ConstraintDescriptorImpl<?> composingConstraint : getComposingConstraintImpls() ) {
			if ( composingConstraint.constraintType != constraintType ) {
				throw LOG.getComposedAndComposingConstraintsHaveDifferentTypesException(
						annotationDescriptor.getType(),
						composingConstraint.annotationDescriptor.getType(),
						constraintType,
						composingConstraint.constraintType
				);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static Set<Class<? extends Payload>> buildPayloadSet(ConstraintAnnotationDescriptor<?> annotationDescriptor) {
		Set<Class<? extends Payload>> payloadSet = newHashSet();

		Class<? extends Payload>[] payloadFromAnnotation = annotationDescriptor.getPayload();

		if ( payloadFromAnnotation != null ) {
			payloadSet.addAll( Arrays.asList( payloadFromAnnotation ) );
		}
		return CollectionHelper.toImmutableSet( payloadSet );
	}

	private static Set<Class<?>> buildGroupSet(ConstraintAnnotationDescriptor<?> annotationDescriptor, Class<?> implicitGroup) {
		Set<Class<?>> groupSet = newHashSet();
		final Class<?>[] groupsFromAnnotation = annotationDescriptor.getGroups();
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
		return CollectionHelper.toImmutableSet( groupSet );
	}

	private Map<ClassIndexWrapper, Map<String, Object>> parseOverrideParameters() {
		Map<ClassIndexWrapper, Map<String, Object>> overrideParameters = newHashMap();
		final Method[] methods = run( GetDeclaredMethods.action( annotationDescriptor.getType() ) );
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
		Object value = annotationDescriptor.getAttribute( m.getName() );
		for ( OverridesAttribute overridesAttribute : attributes ) {
			String overridesAttributeName = overridesAttribute.name().length() > 0 ? overridesAttribute.name() : m.getName();

			ensureAttributeIsOverridable( m, overridesAttribute, overridesAttributeName );

			ClassIndexWrapper wrapper = new ClassIndexWrapper(
					overridesAttribute.constraint(), overridesAttribute.constraintIndex()
			);
			Map<String, Object> map = overrideParameters.get( wrapper );
			if ( map == null ) {
				map = newHashMap();
				overrideParameters.put( wrapper, map );
			}
			map.put( overridesAttributeName, value );
		}
	}

	private void ensureAttributeIsOverridable(Method m, OverridesAttribute overridesAttribute, String overridesAttributeName) {
		final Method method = run( GetMethod.action( overridesAttribute.constraint(), overridesAttributeName ) );
		if ( method == null ) {
			throw LOG.getOverriddenConstraintAttributeNotFoundException( overridesAttributeName );
		}
		Class<?> returnTypeOfOverriddenConstraint = method.getReturnType();
		if ( !returnTypeOfOverriddenConstraint.equals( m.getReturnType() ) ) {
			throw LOG.getWrongAttributeTypeForOverriddenConstraintException(
					returnTypeOfOverriddenConstraint,
					m.getReturnType()
			);
		}
	}

	private Set<ConstraintDescriptorImpl<?>> parseComposingConstraints(ConstraintHelper constraintHelper, Constrainable constrainable,
			ConstraintType constraintType) {
		Set<ConstraintDescriptorImpl<?>> composingConstraintsSet = newHashSet();
		Map<ClassIndexWrapper, Map<String, Object>> overrideParameters = parseOverrideParameters();
		Map<Class<? extends Annotation>, ComposingConstraintAnnotationLocation> composingConstraintLocations = new HashMap<>();

		for ( Annotation declaredAnnotation : annotationDescriptor.getType().getDeclaredAnnotations() ) {
			Class<? extends Annotation> declaredAnnotationType = declaredAnnotation.annotationType();
			if ( NON_COMPOSING_CONSTRAINT_ANNOTATIONS.contains( declaredAnnotationType.getName() ) ) {
				// ignore the usual suspects which will be in almost any constraint, but are no composing constraint
				continue;
			}

			if ( constraintHelper.isConstraintAnnotation( declaredAnnotationType ) ) {
				if ( composingConstraintLocations.containsKey( declaredAnnotationType )
						&& !ComposingConstraintAnnotationLocation.DIRECT.equals( composingConstraintLocations.get( declaredAnnotationType ) ) ) {
					throw LOG.getCannotMixDirectAnnotationAndListContainerOnComposedConstraintException( annotationDescriptor.getType(), declaredAnnotationType );
				}

				ConstraintDescriptorImpl<?> descriptor = createComposingConstraintDescriptor(
						constraintHelper,
						constrainable,
						overrideParameters,
						OVERRIDES_PARAMETER_DEFAULT_INDEX,
						declaredAnnotation,
						constraintType
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
						throw LOG.getCannotMixDirectAnnotationAndListContainerOnComposedConstraintException( annotationDescriptor.getType(),
								constraintAnnotation.annotationType() );
					}

					ConstraintDescriptorImpl<?> descriptor = createComposingConstraintDescriptor(
							constraintHelper,
							constrainable,
							overrideParameters,
							index,
							constraintAnnotation,
							constraintType
					);
					composingConstraintsSet.add( descriptor );
					composingConstraintLocations.put( constraintAnnotation.annotationType(), ComposingConstraintAnnotationLocation.IN_CONTAINER );
					LOG.debugf( "Adding composing constraint: %s.", descriptor );
					index++;
				}
			}
		}
		return CollectionHelper.toImmutableSet( composingConstraintsSet );
	}

	private CompositionType parseCompositionType(ConstraintHelper constraintHelper) {
		for ( Annotation declaredAnnotation : annotationDescriptor.getType().getDeclaredAnnotations() ) {
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
			ConstraintHelper constraintHelper,
			Constrainable constrainable,
			Map<ClassIndexWrapper, Map<String, Object>> overrideParameters,
			int index,
			U constraintAnnotation,
			ConstraintType constraintType) {

		@SuppressWarnings("unchecked")
		final Class<U> annotationType = (Class<U>) constraintAnnotation.annotationType();

		// use a annotation proxy
		ConstraintAnnotationDescriptor.Builder<U> annotationDescriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>(
				annotationType, run( GetAnnotationAttributes.action( constraintAnnotation ) )
		);

		// get the right override parameters
		Map<String, Object> overrides = overrideParameters.get(
				new ClassIndexWrapper(
						annotationType, index
				)
		);
		if ( overrides != null ) {
			for ( Map.Entry<String, Object> entry : overrides.entrySet() ) {
				annotationDescriptorBuilder.setAttribute( entry.getKey(), entry.getValue() );
			}
		}

		//propagate inherited attributes to composing constraints
		annotationDescriptorBuilder.setGroups( groups.toArray( new Class<?>[groups.size()] ) );
		annotationDescriptorBuilder.setPayload( payloads.toArray( new Class<?>[payloads.size()] ) );
		if ( annotationDescriptorBuilder.hasAttribute( ConstraintHelper.VALIDATION_APPLIES_TO ) ) {
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

			annotationDescriptorBuilder.setAttribute( ConstraintHelper.VALIDATION_APPLIES_TO, validationAppliesTo );
		}

		return new ConstraintDescriptorImpl<>(
				constraintHelper, constrainable, annotationDescriptorBuilder.build(), constraintLocationKind, null, definedOn, constraintType
		);
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <P> P run(PrivilegedAction<P> action) {
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
	private static class ClassIndexWrapper {
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
