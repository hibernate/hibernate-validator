/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.xml.mapping.ContainerElementTypeConfigurationBuilder.ContainerElementTypeConfiguration;

/**
 * Builder for constraints on return value.
 *
 * @author Marko Bekhta
 */
class ReturnValueStaxBuilder extends AbstractConstrainedElementStaxBuilder {

	private static final String RETURN_VALUE_QNAME_LOCAL_PART = "return-value";

	ReturnValueStaxBuilder(ClassLoadingHelper classLoadingHelper, ConstraintCreationContext constraintCreationContext,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder, AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		super( classLoadingHelper, constraintCreationContext, defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	@Override
	Optional<QName> getMainAttributeValueQname() {
		return Optional.empty();
	}

	@Override
	protected String getAcceptableQName() {
		return RETURN_VALUE_QNAME_LOCAL_PART;
	}

	CascadingMetaDataBuilder build(
			Callable callable,
			Set<MetaConstraint<?>> returnValueConstraints,
			Set<MetaConstraint<?>> returnValueTypeArgumentConstraints) {

		ConstraintLocation constraintLocation = ConstraintLocation.forReturnValue( callable );
		returnValueConstraints.addAll( constraintTypeStaxBuilders.stream()
				.map( builder -> builder.build( constraintLocation, ConstraintLocationKind.of( callable.getConstrainedElementKind() ),
						ConstraintDescriptorImpl.ConstraintType.GENERIC ) )
				.collect( Collectors.toSet() ) );

		ContainerElementTypeConfiguration containerElementTypeConfiguration = getContainerElementTypeConfiguration( callable.getType(), constraintLocation );

		returnValueTypeArgumentConstraints.addAll( containerElementTypeConfiguration.getMetaConstraints() );

		// ignore annotations
		if ( ignoreAnnotations.isPresent() ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsForReturnValue(
					callable,
					ignoreAnnotations.get()
			);
		}

		return getCascadingMetaData( containerElementTypeConfiguration.getTypeParametersCascadingMetaData(), callable.getType() );
	}
}
