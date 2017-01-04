/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import org.hibernate.validator.internal.engine.cascading.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.cascading.ValueExtractors;
import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
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

	public static <A extends Annotation> MetaConstraint<A> create(TypeResolutionHelper typeResolutionHelper, ValueExtractors valueExtractors,
			ConstraintDescriptorImpl<A> constraintDescriptor, ConstraintLocation location) {
		ValueExtractorDescriptor valueExtractorDescriptor = valueExtractors.forLocation( location );

		Type typeOfValidatedElement;
		if ( !( location instanceof TypeArgumentConstraintLocation ) && location.getUnwrapMode() == UnwrapMode.UNWRAP ) {
			typeOfValidatedElement = getSingleTypeParameterBind( typeResolutionHelper,
					location.getTypeForValidatorResolution(),
					valueExtractorDescriptor.getExtractedType() );
		}
		else {
			typeOfValidatedElement = location.getTypeForValidatorResolution();
		}

		return new MetaConstraint<A>( constraintDescriptor, location, valueExtractorDescriptor, typeOfValidatedElement );
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
