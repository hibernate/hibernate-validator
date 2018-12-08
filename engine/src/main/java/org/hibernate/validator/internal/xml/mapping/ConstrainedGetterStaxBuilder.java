/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
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
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.properties.javabean.JavaBeanGetter;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.xml.mapping.ContainerElementTypeConfigurationBuilder.ContainerElementTypeConfiguration;

/**
 * Builder for constrained getters.
 *
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
class ConstrainedGetterStaxBuilder extends AbstractConstrainedElementStaxBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );
	private static final QName NAME_QNAME = new QName( "name" );

	private static final String GETTER_QNAME_LOCAL_PART = "getter";

	ConstrainedGetterStaxBuilder(ClassLoadingHelper classLoadingHelper, ConstraintCreationContext constraintCreationContext,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder, AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		super( classLoadingHelper, constraintCreationContext, defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	@Override
	Optional<QName> getMainAttributeValueQname() {
		return Optional.of( NAME_QNAME );
	}

	@Override
	protected String getAcceptableQName() {
		return GETTER_QNAME_LOCAL_PART;
	}

	ConstrainedExecutable build(JavaBeanHelper javaBeanHelper, Class<?> beanClass, List<String> alreadyProcessedGetterNames) {
		if ( alreadyProcessedGetterNames.contains( mainAttributeValue ) ) {
			throw LOG.getIsDefinedTwiceInMappingXmlForBeanException( mainAttributeValue, beanClass );
		}
		else {
			alreadyProcessedGetterNames.add( mainAttributeValue );
		}
		JavaBeanGetter javaBeanGetter = findGetter( javaBeanHelper, beanClass, mainAttributeValue );
		ConstraintLocation constraintLocation = ConstraintLocation.forGetter( javaBeanGetter );

		Set<MetaConstraint<?>> metaConstraints = constraintTypeStaxBuilders.stream()
				.map( builder -> builder.build( constraintLocation, ConstraintLocationKind.GETTER, null ) )
				.collect( Collectors.toSet() );

		ContainerElementTypeConfiguration containerElementTypeConfiguration = getContainerElementTypeConfiguration(
				javaBeanGetter.getType(), constraintLocation );

		ConstrainedExecutable constrainedGetter = new ConstrainedExecutable(
				ConfigurationSource.XML,
				javaBeanGetter,
				Collections.<ConstrainedParameter>emptyList(),
				Collections.<MetaConstraint<?>>emptySet(),
				metaConstraints,
				containerElementTypeConfiguration.getMetaConstraints(),
				getCascadingMetaData( containerElementTypeConfiguration.getTypeParametersCascadingMetaData(), javaBeanGetter.getType() )
		);

		// ignore annotations
		if ( ignoreAnnotations.isPresent() ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
					javaBeanGetter,
					ignoreAnnotations.get()
			);
		}

		return constrainedGetter;
	}

	private static JavaBeanGetter findGetter(JavaBeanHelper javaBeanHelper, Class<?> beanClass, String getterName) {
		Optional<JavaBeanGetter> property = javaBeanHelper.findGetter( beanClass, getterName );

		return property.orElseThrow( () -> LOG.getBeanDoesNotContainThePropertyException( beanClass, getterName ) );
	}
}
