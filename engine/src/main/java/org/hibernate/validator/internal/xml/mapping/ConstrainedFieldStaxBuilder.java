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

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.properties.javabean.JavaBeanField;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
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

	ConstrainedFieldStaxBuilder(ClassLoadingHelper classLoadingHelper, ConstraintCreationContext constraintCreationContext,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder, AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		super( classLoadingHelper, constraintCreationContext, defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	@Override
	Optional<QName> getMainAttributeValueQname() {
		return Optional.of( NAME_QNAME );
	}

	@Override
	protected String getAcceptableQName() {
		return FIELD_QNAME_LOCAL_PART;
	}

	ConstrainedField build(JavaBeanHelper javaBeanHelper, Class<?> beanClass, List<String> alreadyProcessedFieldNames) {
		if ( alreadyProcessedFieldNames.contains( mainAttributeValue ) ) {
			throw LOG.getIsDefinedTwiceInMappingXmlForBeanException( mainAttributeValue, beanClass );
		}
		else {
			alreadyProcessedFieldNames.add( mainAttributeValue );
		}
		JavaBeanField javaBeanField = findField( javaBeanHelper, beanClass, mainAttributeValue );
		ConstraintLocation constraintLocation = ConstraintLocation.forField( javaBeanField );

		Set<MetaConstraint<?>> metaConstraints = constraintTypeStaxBuilders.stream()
				.map( builder -> builder.build( constraintLocation, ConstraintLocationKind.FIELD, null ) )
				.collect( Collectors.toSet() );

		ContainerElementTypeConfiguration containerElementTypeConfiguration = getContainerElementTypeConfiguration(
				javaBeanField.getType(), constraintLocation );

		ConstrainedField constrainedField = new ConstrainedField(
				ConfigurationSource.XML,
				javaBeanField,
				metaConstraints,
				containerElementTypeConfiguration.getMetaConstraints(),
				getCascadingMetaData( containerElementTypeConfiguration.getTypeParametersCascadingMetaData(), javaBeanField.getType() )
		);

		// ignore annotations
		if ( ignoreAnnotations.isPresent() ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
					javaBeanField,
					ignoreAnnotations.get()
			);
		}

		return constrainedField;
	}

	private static JavaBeanField findField(JavaBeanHelper javaBeanHelper, Class<?> beanClass, String fieldName) {
		return javaBeanHelper.findDeclaredField( beanClass, fieldName )
				.orElseThrow( () -> LOG.getBeanDoesNotContainTheFieldException( beanClass, fieldName ) );
	}
}
