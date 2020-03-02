/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ValidationException;
import javax.xml.namespace.QName;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.xml.mapping.ContainerElementTypeConfigurationBuilder.ContainerElementTypeConfiguration;

/**
 * Builder for constrained parameters.
 *
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
class ConstrainedParameterStaxBuilder extends AbstractConstrainedElementStaxBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final String PARAMETER_QNAME_LOCAL_PART = "parameter";
	private static final QName TYPE_QNAME = new QName( "type" );

	ConstrainedParameterStaxBuilder(ClassLoadingHelper classLoadingHelper, ConstraintCreationContext constraintCreationContext,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder, AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		super( classLoadingHelper, constraintCreationContext, defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	@Override
	Optional<QName> getMainAttributeValueQname() {
		return Optional.of( TYPE_QNAME );
	}

	@Override
	protected String getAcceptableQName() {
		return PARAMETER_QNAME_LOCAL_PART;
	}

	public Class<?> getParameterType(Class<?> beanClass) {
		try {
			return classLoadingHelper.loadClass( mainAttributeValue, defaultPackageStaxBuilder.build().orElse( "" ) );
		}
		catch (ValidationException e) {
			throw LOG.getInvalidParameterTypeException( mainAttributeValue, beanClass );
		}
	}

	ConstrainedParameter build(Callable callable, int index) {

		ConstraintLocation constraintLocation = ConstraintLocation.forParameter( callable, index );
		Type type = callable.getParameterGenericType( index );

		Set<MetaConstraint<?>> metaConstraints = constraintTypeStaxBuilders.stream()
				.map( builder -> builder.build( constraintLocation, ConstraintLocationKind.PARAMETER, null ) )
				.collect( Collectors.toSet() );

		ContainerElementTypeConfiguration containerElementTypeConfiguration = getContainerElementTypeConfiguration( type, constraintLocation );

		// ignore annotations
		if ( ignoreAnnotations.isPresent() ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsOnParameter(
					callable,
					index,
					ignoreAnnotations.get()
			);
		}

		ConstrainedParameter constrainedParameter = new ConstrainedParameter(
				ConfigurationSource.XML,
				callable,
				type,
				index,
				metaConstraints,
				containerElementTypeConfiguration.getMetaConstraints(),
				getCascadingMetaData( containerElementTypeConfiguration.getTypeParametersCascadingMetaData(), type )
		);
		return constrainedParameter;
	}
}
