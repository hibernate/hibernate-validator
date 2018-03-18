/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedProperty;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.properties.javabean.JavaBean;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.xml.mapping.ContainerElementTypeConfigurationBuilder.ContainerElementTypeConfiguration;

/**
 * Builder for constrained fields.
 *
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
class ConstrainedFieldStaxBuilder extends AbstractConstrainedElementStaxBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final String FIELD_QNAME_LOCAL_PART = "field";
	private static final QName NAME_QNAME = new QName( "name" );

	ConstrainedFieldStaxBuilder(ClassLoadingHelper classLoadingHelper, ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder, AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		super( classLoadingHelper, constraintHelper, typeResolutionHelper, valueExtractorManager, defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	@Override
	Optional<QName> getMainAttributeValueQname() {
		return Optional.of( NAME_QNAME );
	}

	@Override
	protected String getAcceptableQName() {
		return FIELD_QNAME_LOCAL_PART;
	}

	ConstrainedProperty build(JavaBean javaBean, List<String> alreadyProcessedFieldNames) {
		if ( alreadyProcessedFieldNames.contains( mainAttributeValue ) ) {
			throw LOG.getIsDefinedTwiceInMappingXmlForBeanException( mainAttributeValue, javaBean );
		}
		else {
			alreadyProcessedFieldNames.add( mainAttributeValue );
		}
		Property property = findField( javaBean, mainAttributeValue );
		ConstraintLocation constraintLocation = ConstraintLocation.forProperty( property );

		Set<MetaConstraint<?>> metaConstraints = constraintTypeStaxBuilders.stream()
				.map( builder -> builder.build( constraintLocation, java.lang.annotation.ElementType.FIELD, null ) )
				.collect( Collectors.toSet() );

		ContainerElementTypeConfiguration containerElementTypeConfiguration = getContainerElementTypeConfiguration(
				property.getType(), constraintLocation );

		ConstrainedProperty constrainedField = ConstrainedProperty.forField(
				ConfigurationSource.XML,
				property,
				metaConstraints,
				containerElementTypeConfiguration.getMetaConstraints(),
				getCascadingMetaData( containerElementTypeConfiguration.getTypeParametersCascadingMetaData(), property.getType() )
		);

		// ignore annotations
		if ( ignoreAnnotations.isPresent() ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
					property,
					ignoreAnnotations.get()
			);
		}

		return constrainedField;
	}

	private static Property findField(JavaBean javaBean, String fieldName) {
		return javaBean.getFieldPropertyByName( fieldName )
				.orElseThrow( () -> LOG.getBeanDoesNotContainTheFieldException( javaBean, fieldName ) );
	}
}
