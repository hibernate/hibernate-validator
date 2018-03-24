/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.namespace.QName;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredConstructor;

/**
 * Builder for constrained constructors.
 *
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
class ConstrainedConstructorStaxBuilder extends AbstractConstrainedExecutableElementStaxBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final String METHOD_QNAME_LOCAL_PART = "constructor";

	ConstrainedConstructorStaxBuilder(
			ClassLoadingHelper classLoadingHelper, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager, DefaultPackageStaxBuilder defaultPackageStaxBuilder,
			AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		super( classLoadingHelper, constraintHelper, typeResolutionHelper, valueExtractorManager, defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	@Override
	Optional<QName> getMainAttributeValueQname() {
		return Optional.empty();
	}

	@Override
	protected String getAcceptableQName() {
		return METHOD_QNAME_LOCAL_PART;
	}

	public String getMethodName() {
		return mainAttributeValue;
	}

	ConstrainedExecutable build(Class<?> beanClass, List<Constructor<?>> alreadyProcessedConstructors) {
		Class<?>[] parameterTypes = constrainedParameterStaxBuilders.stream()
				.map( builder -> builder.getParameterType( beanClass ) )
				.toArray( Class[]::new );

		final Constructor<?> constructor = run(
				GetDeclaredConstructor.action(
						beanClass,
						parameterTypes
				)
		);

		if ( constructor == null ) {
			throw LOG.getBeanDoesNotContainConstructorException(
					beanClass,
					parameterTypes
			);
		}

		if ( alreadyProcessedConstructors.contains( constructor ) ) {
			throw LOG.getConstructorIsDefinedTwiceInMappingXmlForBeanException( constructor, beanClass );
		}
		else {
			alreadyProcessedConstructors.add( constructor );
		}

		// ignore annotations
		if ( ignoreAnnotations.isPresent() ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
					constructor,
					ignoreAnnotations.get()
			);
		}

		List<ConstrainedParameter> constrainedParameters = CollectionHelper.newArrayList( constrainedParameterStaxBuilders.size() );
		for ( int index = 0; index < constrainedParameterStaxBuilders.size(); index++ ) {
			ConstrainedParameterStaxBuilder builder = constrainedParameterStaxBuilders.get( index );
			constrainedParameters.add( builder.build( constructor, index ) );
		}

		Set<MetaConstraint<?>> crossParameterConstraints = getCrossParameterStaxBuilder()
				.map( builder -> builder.build( constructor ) ).orElse( Collections.emptySet() );

		// parse the return value
		Set<MetaConstraint<?>> returnValueConstraints = new HashSet<>();
		Set<MetaConstraint<?>> returnValueTypeArgumentConstraints = new HashSet<>();
		CascadingMetaDataBuilder cascadingMetaDataBuilder = getReturnValueStaxBuilder().map( builder -> builder.build( constructor, returnValueConstraints, returnValueTypeArgumentConstraints ) )
				.orElse( CascadingMetaDataBuilder.nonCascading() );

		return new ConstrainedExecutable(
				ConfigurationSource.XML,
				constructor,
				constrainedParameters,
				crossParameterConstraints,
				returnValueConstraints,
				returnValueTypeArgumentConstraints,
				cascadingMetaDataBuilder
		);
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
