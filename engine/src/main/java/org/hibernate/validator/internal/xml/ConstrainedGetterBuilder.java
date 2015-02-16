/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetMethodFromPropertyName;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Builder for constraint getters.
 *
 * @author Hardy Ferentschik
 */
class ConstrainedGetterBuilder {
	private static final Log log = LoggerFactory.make();

	private final GroupConversionBuilder groupConversionBuilder;
	private final MetaConstraintBuilder metaConstraintBuilder;
	private final AnnotationProcessingOptionsImpl annotationProcessingOptions;

	ConstrainedGetterBuilder(MetaConstraintBuilder metaConstraintBuilder, GroupConversionBuilder groupConversionBuilder,
			AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		this.metaConstraintBuilder = metaConstraintBuilder;
		this.groupConversionBuilder = groupConversionBuilder;
		this.annotationProcessingOptions = annotationProcessingOptions;
	}

	Set<ConstrainedExecutable> buildConstrainedGetters(List<GetterType> getterList,
																	 Class<?> beanClass,
																	 String defaultPackage) {
		Set<ConstrainedExecutable> constrainedExecutables = newHashSet();
		List<String> alreadyProcessedGetterNames = newArrayList();
		for ( GetterType getterType : getterList ) {
			String getterName = getterType.getName();
			Method getter = findGetter( beanClass, getterName, alreadyProcessedGetterNames );
			ConstraintLocation constraintLocation = ConstraintLocation.forProperty( getter );

			Set<MetaConstraint<?>> metaConstraints = newHashSet();
			for ( ConstraintType constraint : getterType.getConstraint() ) {
				MetaConstraint<?> metaConstraint = metaConstraintBuilder.buildMetaConstraint(
						constraintLocation,
						constraint,
						java.lang.annotation.ElementType.METHOD,
						defaultPackage,
						null
				);
				metaConstraints.add( metaConstraint );
			}
			Map<Class<?>, Class<?>> groupConversions = groupConversionBuilder.buildGroupConversionMap(
					getterType.getConvertGroup(),
					defaultPackage
			);

			// TODO HV-919 Support specification of type parameter constraints via XML and API
			ConstrainedExecutable constrainedGetter = new ConstrainedExecutable(
					ConfigurationSource.XML,
					constraintLocation,
					Collections.<ConstrainedParameter>emptyList(),
					Collections.<MetaConstraint<?>>emptySet(),
					metaConstraints,
					Collections.<MetaConstraint<?>>emptySet(),
					groupConversions,
					getterType.getValid() != null,
					UnwrapMode.AUTOMATIC
			);
			constrainedExecutables.add( constrainedGetter );

			// ignore annotations
			if ( getterType.getIgnoreAnnotations() != null ) {
				annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
						getter,
						getterType.getIgnoreAnnotations()
				);
			}
		}

		return constrainedExecutables;
	}

	private static Method findGetter(Class<?> beanClass, String getterName, List<String> alreadyProcessedGetterNames) {
		if ( alreadyProcessedGetterNames.contains( getterName ) ) {
			throw log.getIsDefinedTwiceInMappingXmlForBeanException( getterName, beanClass.getName() );
		}
		else {
			alreadyProcessedGetterNames.add( getterName );
		}

		final Method method = run( GetMethodFromPropertyName.action( beanClass, getterName ) );
		if ( method == null ) {
			throw log.getBeanDoesNotContainThePropertyException( beanClass.getName(), getterName );
		}

		return method;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
