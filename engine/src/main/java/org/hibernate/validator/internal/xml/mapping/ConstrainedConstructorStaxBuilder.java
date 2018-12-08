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
import org.hibernate.validator.internal.properties.javabean.JavaBeanConstructor;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

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
			ClassLoadingHelper classLoadingHelper, ConstraintCreationContext constraintCreationContext,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder, AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		super( classLoadingHelper, constraintCreationContext, defaultPackageStaxBuilder, annotationProcessingOptions );
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

	ConstrainedExecutable build(JavaBeanHelper javaBeanHelper, Class<?> beanClass, List<JavaBeanConstructor> alreadyProcessedConstructors) {
		Class<?>[] parameterTypes = constrainedParameterStaxBuilders.stream()
				.map( builder -> builder.getParameterType( beanClass ) )
				.toArray( Class[]::new );

		final JavaBeanConstructor javaBeanConstructor = findConstructor( javaBeanHelper, beanClass, parameterTypes );

		if ( alreadyProcessedConstructors.contains( javaBeanConstructor ) ) {
			throw LOG.getConstructorIsDefinedTwiceInMappingXmlForBeanException( javaBeanConstructor, beanClass );
		}
		else {
			alreadyProcessedConstructors.add( javaBeanConstructor );
		}

		// ignore annotations
		if ( ignoreAnnotations.isPresent() ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
					javaBeanConstructor,
					ignoreAnnotations.get()
			);
		}

		List<ConstrainedParameter> constrainedParameters = CollectionHelper.newArrayList( constrainedParameterStaxBuilders.size() );
		for ( int index = 0; index < constrainedParameterStaxBuilders.size(); index++ ) {
			ConstrainedParameterStaxBuilder builder = constrainedParameterStaxBuilders.get( index );
			constrainedParameters.add( builder.build( javaBeanConstructor, index ) );
		}

		Set<MetaConstraint<?>> crossParameterConstraints = getCrossParameterStaxBuilder()
				.map( builder -> builder.build( javaBeanConstructor ) ).orElse( Collections.emptySet() );

		// parse the return value
		Set<MetaConstraint<?>> returnValueConstraints = new HashSet<>();
		Set<MetaConstraint<?>> returnValueTypeArgumentConstraints = new HashSet<>();
		CascadingMetaDataBuilder cascadingMetaDataBuilder = getReturnValueStaxBuilder()
				.map( builder -> builder.build( javaBeanConstructor, returnValueConstraints, returnValueTypeArgumentConstraints ) )
				.orElse( CascadingMetaDataBuilder.nonCascading() );

		return new ConstrainedExecutable(
				ConfigurationSource.XML,
				javaBeanConstructor,
				constrainedParameters,
				crossParameterConstraints,
				returnValueConstraints,
				returnValueTypeArgumentConstraints,
				cascadingMetaDataBuilder
		);
	}

	private JavaBeanConstructor findConstructor(JavaBeanHelper javaBeanHelper, Class<?> beanClass, Class<?>[] parameterTypes) {
		return javaBeanHelper.findDeclaredConstructor( beanClass, parameterTypes )
				.orElseThrow( () -> LOG.getBeanDoesNotContainConstructorException( beanClass, parameterTypes ) );
	}
}
