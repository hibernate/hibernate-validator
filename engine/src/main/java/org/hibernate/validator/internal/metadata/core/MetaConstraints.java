/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.core;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.metadata.ValidateUnwrappedValue;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorHelper;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.MetaConstraint.ContainerClassTypeParameterAndExtractor;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.TypeArgumentConstraintLocation;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.TypeVariableBindings;
import org.hibernate.validator.internal.util.TypeVariables;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import com.fasterxml.classmate.ResolvedType;

/**
 * Helper used to create {@link MetaConstraint}s.
 *
 * @author Guillaume Smet
 */
public class MetaConstraints {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private MetaConstraints() {
	}

	public static <A extends Annotation> MetaConstraint<A> create(TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager,
			ConstraintValidatorManager constraintValidatorManager,
			ConstraintDescriptorImpl<A> constraintDescriptor, ConstraintLocation location) {
		List<ContainerClassTypeParameterAndExtractor> valueExtractionPath = new ArrayList<>();

		Type typeOfValidatedElement = addValueExtractorDescriptorForWrappedValue( typeResolutionHelper, valueExtractorManager, constraintDescriptor,
				valueExtractionPath, location );

		ConstraintLocation current = location;
		do {
			if ( current instanceof TypeArgumentConstraintLocation ) {
				addValueExtractorDescriptorForTypeArgumentLocation( valueExtractorManager, valueExtractionPath, (TypeArgumentConstraintLocation) current );
				current = ( (TypeArgumentConstraintLocation) current ).getDelegate();
			}
			else {
				current = null;
			}
		}
		while ( current != null );

		Collections.reverse( valueExtractionPath );

		return new MetaConstraint<>( constraintValidatorManager, constraintDescriptor, location, valueExtractionPath, typeOfValidatedElement );
	}

	private static <A extends Annotation> Type addValueExtractorDescriptorForWrappedValue(TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager, ConstraintDescriptorImpl<A> constraintDescriptor,
			List<ContainerClassTypeParameterAndExtractor> valueExtractionPath, ConstraintLocation location) {
		if ( ValidateUnwrappedValue.SKIP.equals( constraintDescriptor.getValueUnwrapping() ) ) {
			return location.getTypeForValidatorResolution();
		}

		Class<?> declaredType = TypeHelper.getErasedReferenceType( location.getTypeForValidatorResolution() );
		Set<ValueExtractorDescriptor> valueExtractorDescriptorCandidates = valueExtractorManager.getResolver().getMaximallySpecificValueExtractors( declaredType );
		ValueExtractorDescriptor selectedValueExtractorDescriptor;

		// we want to force the unwrapping so we require one and only one maximally specific value extractors
		if ( ValidateUnwrappedValue.UNWRAP.equals( constraintDescriptor.getValueUnwrapping() ) ) {
			switch ( valueExtractorDescriptorCandidates.size() ) {
				case 0:
					throw LOG.getNoValueExtractorFoundForUnwrapException( declaredType );
				case 1:
					selectedValueExtractorDescriptor = valueExtractorDescriptorCandidates.iterator().next();
					break;
				default:
					throw LOG.getUnableToGetMostSpecificValueExtractorDueToSeveralMaximallySpecificValueExtractorsDeclaredException(
							declaredType,
							ValueExtractorHelper.toValueExtractorClasses( valueExtractorDescriptorCandidates ) );
			}
		}
		// we are in the implicit (DEFAULT) case so:
		// - if we don't have a maximally specific value extractor marked with @UnwrapByDefault, we don't unwrap
		// - if we have one maximally specific value extractors that is marked with @UnwrapByDefault, we unwrap
		// - otherwise, we throw an exception as we can't choose between the value extractors
		else {
			Set<ValueExtractorDescriptor> unwrapByDefaultValueExtractorDescriptorCandidates = valueExtractorDescriptorCandidates.stream()
					.filter( ved -> ved.isUnwrapByDefault() )
					.collect( Collectors.toSet() );

			switch ( unwrapByDefaultValueExtractorDescriptorCandidates.size() ) {
				case 0:
					return location.getTypeForValidatorResolution();
				case 1:
					selectedValueExtractorDescriptor = unwrapByDefaultValueExtractorDescriptorCandidates.iterator().next();
					break;
				default:
					throw LOG.getImplicitUnwrappingNotAllowedWhenSeveralMaximallySpecificValueExtractorsMarkedWithUnwrapByDefaultDeclaredException(
							declaredType,
							ValueExtractorHelper.toValueExtractorClasses( unwrapByDefaultValueExtractorDescriptorCandidates ) );
			}
		}

		if ( selectedValueExtractorDescriptor.getExtractedType().isPresent() ) {
			valueExtractionPath.add( new ContainerClassTypeParameterAndExtractor( declaredType, null, null, selectedValueExtractorDescriptor ) );
			return selectedValueExtractorDescriptor.getExtractedType().get();
		}
		else {
			Class<?> wrappedValueType = getWrappedValueType( typeResolutionHelper, location.getTypeForValidatorResolution(), selectedValueExtractorDescriptor );
			TypeVariable<?> typeParameter = getContainerClassTypeParameter( declaredType, selectedValueExtractorDescriptor );

			valueExtractionPath.add( new ContainerClassTypeParameterAndExtractor( declaredType, typeParameter, TypeVariables.getTypeParameterIndex( typeParameter ), selectedValueExtractorDescriptor ) );

			return wrappedValueType;
		}
	}

	private static void addValueExtractorDescriptorForTypeArgumentLocation( ValueExtractorManager valueExtractorManager,
			List<ContainerClassTypeParameterAndExtractor> valueExtractionPath, TypeArgumentConstraintLocation typeArgumentConstraintLocation ) {
		Class<?> declaredType = typeArgumentConstraintLocation.getContainerClass();
		TypeVariable<?> typeParameter = typeArgumentConstraintLocation.getTypeParameter();

		ValueExtractorDescriptor valueExtractorDescriptor = valueExtractorManager.getResolver()
				.getMaximallySpecificAndContainerElementCompliantValueExtractor( declaredType, typeParameter );

		if ( valueExtractorDescriptor == null ) {
			throw LOG.getNoValueExtractorFoundForTypeException( declaredType, typeParameter );
		}

		TypeVariable<?> actualTypeParameter = TypeVariables.getActualTypeParameter( typeParameter );
		valueExtractionPath.add( new ContainerClassTypeParameterAndExtractor(
				TypeVariables.getContainerClass( typeParameter ),
				actualTypeParameter,
				TypeVariables.getTypeParameterIndex( actualTypeParameter ),
				valueExtractorDescriptor ) );
	}

	/**
	 * Returns the sub-types binding for the single type parameter of the super-type. E.g. for {@code IntegerProperty}
	 * and {@code Property<T>}, {@code Integer} would be returned.
	 */
	private static Class<?> getWrappedValueType(TypeResolutionHelper typeResolutionHelper, Type declaredType, ValueExtractorDescriptor valueExtractorDescriptor) {
		ResolvedType resolvedType = typeResolutionHelper.getTypeResolver().resolve( declaredType );

		List<ResolvedType> resolvedTypeParameters = resolvedType.typeParametersFor( valueExtractorDescriptor.getContainerType() );

		if ( resolvedTypeParameters == null || resolvedTypeParameters.isEmpty() ) {
			throw LOG.getNoValueExtractorFoundForUnwrapException( declaredType );
		}

		return resolvedTypeParameters.get( TypeVariables.getTypeParameterIndex( valueExtractorDescriptor.getExtractedTypeParameter() ) ).getErasedType();
	}

	private static TypeVariable<?> getContainerClassTypeParameter(Class<?> declaredType, ValueExtractorDescriptor selectedValueExtractorDescriptor) {
		if ( selectedValueExtractorDescriptor.getExtractedTypeParameter() == null ) {
			return null;
		}

		Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> allBindings = TypeVariableBindings.getTypeVariableBindings( declaredType );
		Map<TypeVariable<?>, TypeVariable<?>> extractorTypeBindings = allBindings.get( selectedValueExtractorDescriptor.getContainerType() );
		if ( extractorTypeBindings == null ) {
			return null;
		}
		return extractorTypeBindings.entrySet().stream()
				.filter( e -> Objects.equals( e.getKey().getGenericDeclaration(), declaredType ) )
				.collect( Collectors.toMap( Map.Entry::getValue, Map.Entry::getKey ) )
				.get( selectedValueExtractorDescriptor.getExtractedTypeParameter() );
	}
}
