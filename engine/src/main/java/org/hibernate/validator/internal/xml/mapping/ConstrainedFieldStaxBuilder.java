/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
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
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;
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

	ConstrainedField build(Class<?> beanClass, List<String> alreadyProcessedFieldNames) {
		if ( alreadyProcessedFieldNames.contains( mainAttributeValue ) ) {
			throw LOG.getIsDefinedTwiceInMappingXmlForBeanException( mainAttributeValue, beanClass );
		}
		else {
			alreadyProcessedFieldNames.add( mainAttributeValue );
		}
		Field field = findField( beanClass, mainAttributeValue );
		ConstraintLocation constraintLocation = ConstraintLocation.forField( field );
		Set<MetaConstraint<?>> metaConstraints = constraintTypeStaxBuilders.stream()
				.map( builder -> builder.build( constraintLocation, java.lang.annotation.ElementType.FIELD, null ) )
				.collect( Collectors.toSet() );

		ContainerElementTypeConfiguration containerElementTypeConfiguration = getContainerElementTypeConfiguration(
				ReflectionHelper.typeOf( field ), constraintLocation );

		ConstrainedField constrainedField = new ConstrainedField(
				ConfigurationSource.XML,
				field,
				metaConstraints,
				containerElementTypeConfiguration.getMetaConstraints(),
				getCascadingMetaData( containerElementTypeConfiguration.getTypeParametersCascadingMetaData(), ReflectionHelper.typeOf( field ) )
		);

		// ignore annotations
		if ( ignoreAnnotations.isPresent() ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
					field,
					ignoreAnnotations.get()
			);
		}

		return constrainedField;
	}

	private static Field findField(Class<?> beanClass, String fieldName) {
		final Field field = run( GetDeclaredField.action( beanClass, fieldName ) );
		if ( field == null ) {
			throw LOG.getBeanDoesNotContainTheFieldException( beanClass, fieldName );
		}
		return field;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 *
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
