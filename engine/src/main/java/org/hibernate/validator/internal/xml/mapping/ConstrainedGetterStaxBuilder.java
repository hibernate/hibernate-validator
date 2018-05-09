/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
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
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetMethodFromPropertyName;
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

	ConstrainedGetterStaxBuilder(ClassLoadingHelper classLoadingHelper, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager, DefaultPackageStaxBuilder defaultPackageStaxBuilder,
			AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		super( classLoadingHelper, constraintHelper, typeResolutionHelper, valueExtractorManager, defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	@Override
	Optional<QName> getMainAttributeValueQname() {
		return Optional.of( NAME_QNAME );
	}

	@Override
	protected String getAcceptableQName() {
		return GETTER_QNAME_LOCAL_PART;
	}

	ConstrainedExecutable build(Class<?> beanClass, List<String> alreadyProcessedGetterNames) {
		if ( alreadyProcessedGetterNames.contains( mainAttributeValue ) ) {
			throw LOG.getIsDefinedTwiceInMappingXmlForBeanException( mainAttributeValue, beanClass );
		}
		else {
			alreadyProcessedGetterNames.add( mainAttributeValue );
		}
		Method getter = findGetter( beanClass, mainAttributeValue );
		ConstraintLocation constraintLocation = ConstraintLocation.forGetter( beanClass, getter );

		Set<MetaConstraint<?>> metaConstraints = constraintTypeStaxBuilders.stream()
				.map( builder -> builder.build( constraintLocation, java.lang.annotation.ElementType.METHOD, null ) )
				.collect( Collectors.toSet() );

		ContainerElementTypeConfiguration containerElementTypeConfiguration = getContainerElementTypeConfiguration(
				ReflectionHelper.typeOf( getter ), constraintLocation );

		ConstrainedExecutable constrainedGetter = new ConstrainedExecutable(
				ConfigurationSource.XML,
				getter,
				Collections.<ConstrainedParameter>emptyList(),
				Collections.<MetaConstraint<?>>emptySet(),
				metaConstraints,
				containerElementTypeConfiguration.getMetaConstraints(),
				getCascadingMetaData( containerElementTypeConfiguration.getTypeParametersCascadingMetaData(), ReflectionHelper.typeOf( getter ) )
		);

		// ignore annotations
		if ( ignoreAnnotations.isPresent() ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
					getter,
					ignoreAnnotations.get()
			);
		}

		return constrainedGetter;
	}

	private static Method findGetter(Class<?> beanClass, String getterName) {
		final Method method = run( GetMethodFromPropertyName.action( beanClass, getterName ) );
		if ( method == null ) {
			throw LOG.getBeanDoesNotContainThePropertyException( beanClass, getterName );
		}

		return method;
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
