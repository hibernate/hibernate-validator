/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.namespace.QName;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.properties.javabean.JavaBeanMethod;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

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
			ClassLoadingHelper classLoadingHelper, ConstraintCreationContext constraintCreationContext,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder, AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		super( classLoadingHelper, constraintCreationContext, defaultPackageStaxBuilder, annotationProcessingOptions );
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

	ConstrainedExecutable build(JavaBeanHelper javaBeanHelper, Class<?> beanClass, List<JavaBeanMethod> alreadyProcessedMethods) {
		Class<?>[] parameterTypes = constrainedParameterStaxBuilders.stream()
				.map( builder -> builder.getParameterType( beanClass ) )
				.toArray( Class[]::new );

		String methodName = getMethodName();

		JavaBeanMethod javaBeanMethod = findMethod( javaBeanHelper, beanClass, methodName, parameterTypes );

		if ( alreadyProcessedMethods.contains( javaBeanMethod ) ) {
			throw LOG.getMethodIsDefinedTwiceInMappingXmlForBeanException( javaBeanMethod, beanClass );
		}
		else {
			alreadyProcessedMethods.add( javaBeanMethod );
		}

		// ignore annotations
		if ( ignoreAnnotations.isPresent() ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
					javaBeanMethod,
					ignoreAnnotations.get()
			);
		}

		List<ConstrainedParameter> constrainedParameters = CollectionHelper.newArrayList( constrainedParameterStaxBuilders.size() );
		for ( int index = 0; index < constrainedParameterStaxBuilders.size(); index++ ) {
			ConstrainedParameterStaxBuilder builder = constrainedParameterStaxBuilders.get( index );
			constrainedParameters.add( builder.build( javaBeanMethod, index ) );
		}

		Set<MetaConstraint<?>> crossParameterConstraints = getCrossParameterStaxBuilder()
				.map( builder -> builder.build( javaBeanMethod ) ).orElse( Collections.emptySet() );

		// parse the return value
		Set<MetaConstraint<?>> returnValueConstraints = new HashSet<>();
		Set<MetaConstraint<?>> returnValueTypeArgumentConstraints = new HashSet<>();
		CascadingMetaDataBuilder cascadingMetaDataBuilder = getReturnValueStaxBuilder().map( builder -> builder.build( javaBeanMethod, returnValueConstraints, returnValueTypeArgumentConstraints ) )
				.orElse( CascadingMetaDataBuilder.nonCascading() );

		return new ConstrainedExecutable(
				ConfigurationSource.XML,
				javaBeanMethod,
				constrainedParameters,
				crossParameterConstraints,
				returnValueConstraints,
				returnValueTypeArgumentConstraints,
				cascadingMetaDataBuilder
		);
	}

	private JavaBeanMethod findMethod(JavaBeanHelper javaBeanHelper, Class<?> beanClass, String methodName, Class<?>[] parameterTypes) {
		return javaBeanHelper.findDeclaredMethod( beanClass, methodName, parameterTypes )
				.orElseThrow( () -> LOG.getBeanDoesNotContainMethodException( beanClass, methodName, parameterTypes ) );
	}
}
