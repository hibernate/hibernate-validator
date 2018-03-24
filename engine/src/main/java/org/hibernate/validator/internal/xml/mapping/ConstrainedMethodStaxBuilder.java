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
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;

/**
 * Builder for constrained methods.
 *
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
class ConstrainedMethodStaxBuilder extends AbstractConstrainedExecutableElementStaxBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final String METHOD_QNAME_LOCAL_PART = "method";
	private static final QName NAME_QNAME = new QName( "name" );

	ConstrainedMethodStaxBuilder(
			ClassLoadingHelper classLoadingHelper, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
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
		return METHOD_QNAME_LOCAL_PART;
	}

	public String getMethodName() {
		return mainAttributeValue;
	}

	ConstrainedExecutable build(Class<?> beanClass, List<Method> alreadyProcessedMethods) {
		Class<?>[] parameterTypes = constrainedParameterStaxBuilders.stream()
				.map( builder -> builder.getParameterType( beanClass ) )
				.toArray( Class[]::new );

		String methodName = getMethodName();

		final Method method = run(
				GetDeclaredMethod.action(
						beanClass,
						methodName,
						parameterTypes
				)
		);

		if ( method == null ) {
			throw LOG.getBeanDoesNotContainMethodException(
					beanClass,
					methodName,
					parameterTypes
			);
		}

		if ( alreadyProcessedMethods.contains( method ) ) {
			throw LOG.getMethodIsDefinedTwiceInMappingXmlForBeanException( method, beanClass );
		}
		else {
			alreadyProcessedMethods.add( method );
		}

		// ignore annotations
		if ( ignoreAnnotations.isPresent() ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
					method,
					ignoreAnnotations.get()
			);
		}

		List<ConstrainedParameter> constrainedParameters = CollectionHelper.newArrayList( constrainedParameterStaxBuilders.size() );
		for ( int index = 0; index < constrainedParameterStaxBuilders.size(); index++ ) {
			ConstrainedParameterStaxBuilder builder = constrainedParameterStaxBuilders.get( index );
			constrainedParameters.add( builder.build( method, index ) );
		}

		Set<MetaConstraint<?>> crossParameterConstraints = getCrossParameterStaxBuilder()
				.map( builder -> builder.build( method ) ).orElse( Collections.emptySet() );

		// parse the return value
		Set<MetaConstraint<?>> returnValueConstraints = new HashSet<>();
		Set<MetaConstraint<?>> returnValueTypeArgumentConstraints = new HashSet<>();
		CascadingMetaDataBuilder cascadingMetaDataBuilder = getReturnValueStaxBuilder().map( builder -> builder.build( method, returnValueConstraints, returnValueTypeArgumentConstraints ) )
				.orElse( CascadingMetaDataBuilder.nonCascading() );

		return new ConstrainedExecutable(
				ConfigurationSource.XML,
				method,
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
