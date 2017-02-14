/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.valueextraction.ValidateUnwrappedValue;

import org.hibernate.validator.internal.engine.cascading.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.cascading.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.TypeArgumentConstraintLocation;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import com.fasterxml.classmate.ResolvedType;

/**
 * Helper used to create {@link MetaConstraint}s.
 *
 * @author Guillaume Smet
 */
public class MetaConstraints {

	private static final Log LOG = LoggerFactory.make();

	private MetaConstraints() {
	}

	public static <A extends Annotation> MetaConstraint<A> create(TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager,
			ConstraintDescriptorImpl<A> constraintDescriptor, ConstraintLocation location) {
		List<ValueExtractorDescriptor> valueExtractorDescriptors = new ArrayList<>();

		Type typeOfValidatedElement = addValueExtractorDescriptorForWrappedValue( typeResolutionHelper, valueExtractorManager, constraintDescriptor,
				valueExtractorDescriptors, location );

		if ( location instanceof TypeArgumentConstraintLocation ) {
			TypeArgumentConstraintLocation typeArgumentConstraintLocation = (TypeArgumentConstraintLocation) location;

			addValueExtractorDescriptorForTypeArgumentLocation( valueExtractorManager, valueExtractorDescriptors, typeArgumentConstraintLocation );

			ConstraintLocation delegateLocation = typeArgumentConstraintLocation.getDelegate();
			while ( delegateLocation instanceof TypeArgumentConstraintLocation ) {
				typeArgumentConstraintLocation = (TypeArgumentConstraintLocation) delegateLocation;
				addValueExtractorDescriptorForTypeArgumentLocation( valueExtractorManager, valueExtractorDescriptors, (TypeArgumentConstraintLocation) delegateLocation );

				delegateLocation = typeArgumentConstraintLocation.getDelegate();
			}
		}

		Collections.reverse( valueExtractorDescriptors );

		return new MetaConstraint<A>( constraintDescriptor, location, valueExtractorDescriptors, typeOfValidatedElement );
	}

	private static <A extends Annotation> Type addValueExtractorDescriptorForWrappedValue(TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager, ConstraintDescriptorImpl<A> constraintDescriptor,
			List<ValueExtractorDescriptor> valueExtractorDescriptors, ConstraintLocation location) {
		if ( ValidateUnwrappedValue.NO.equals( constraintDescriptor.validateUnwrappedValue() ) ) {
			return location.getTypeForValidatorResolution();
		}

		Class<?> declaredType = TypeHelper.getErasedReferenceType( location.getTypeForValidatorResolution() );
		ValueExtractorDescriptor valueExtractorDescriptorCandidate = valueExtractorManager.getValueExtractor( declaredType );

		if ( ValidateUnwrappedValue.DEFAULT.equals( constraintDescriptor.validateUnwrappedValue() )
				&& ( valueExtractorDescriptorCandidate == null
						|| !valueExtractorDescriptorCandidate.isUnwrapByDefault() ) ) {
			return location.getTypeForValidatorResolution();
		}
		else {
			if ( valueExtractorDescriptorCandidate == null ) {
				throw LOG.getNoValueExtractorFoundForTypeException( declaredType, null );
			}

			valueExtractorDescriptors.add( valueExtractorDescriptorCandidate );

			return getSingleTypeParameterBind( typeResolutionHelper,
					location.getTypeForValidatorResolution(),
					valueExtractorDescriptorCandidate.getExtractedType() );
		}
	}

	private static void addValueExtractorDescriptorForTypeArgumentLocation( ValueExtractorManager valueExtractorManager,
			List<ValueExtractorDescriptor> valueExtractorDescriptors, TypeArgumentConstraintLocation typeArgumentConstraintLocation ) {
		Class<?> declaredType = TypeHelper.getErasedReferenceType( typeArgumentConstraintLocation.getContainerType() );
		TypeVariable<?> typeParameter = typeArgumentConstraintLocation.getTypeParameter();

		ValueExtractorDescriptor valueExtractorDescriptor = valueExtractorManager.getValueExtractor( declaredType, typeParameter );

		if ( valueExtractorDescriptor == null ) {
			throw LOG.getNoValueExtractorFoundForTypeException( declaredType, typeParameter );
		}

		valueExtractorDescriptors.add( valueExtractorDescriptor );
	}

	/**
	 * Returns the sub-types binding for the single type parameter of the super-type. E.g. for {@code IntegerProperty}
	 * and {@code Property<T>}, {@code Integer} would be returned.
	 */
	static Class<?> getSingleTypeParameterBind(TypeResolutionHelper typeResolutionHelper, Type subType, Type superType) {
		ResolvedType resolvedType = typeResolutionHelper.getTypeResolver().resolve( subType );
		List<ResolvedType> resolvedTypeParameters = resolvedType.typeParametersFor( TypeHelper.getErasedReferenceType( superType ) );

		if ( resolvedTypeParameters.isEmpty() ) {
			throw LOG.getNoValueExtractorFoundForUnwrapException( subType );
		}
		else if ( resolvedTypeParameters.size() > 1 ) {
			throw LOG.getUnableToExtractValueForTypeWithMultipleTypeParametersException(  subType );
		}
		else {
			return resolvedTypeParameters.iterator().next().getErasedType();
		}
	}

}
