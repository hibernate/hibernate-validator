/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.provider;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.engine.HibernateConstrainedType;
import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.engine.constrainedtype.JavaBeanConstrainedType;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;
import org.hibernate.validator.internal.xml.mapping.MappingXmlParser;

/**
 * A {@link MetaDataProvider} providing constraint related meta data based on
 * XML descriptors as defined by the Bean Validation API.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class XmlMetaDataProvider implements MetaDataProvider {

	// cached against the fqcn of a class. not a class instance itself (HV-479)
	@Immutable
	private final Map<HibernateConstrainedType<?>, BeanConfiguration<?>> configuredBeans;

	private final AnnotationProcessingOptions annotationProcessingOptions;

	public XmlMetaDataProvider(ConstraintCreationContext constraintCreationContext,
			JavaBeanHelper javaBeanHelper,
			Set<InputStream> mappingStreams,
			ClassLoader externalClassLoader) {

		MappingXmlParser mappingParser = new MappingXmlParser( constraintCreationContext,
				javaBeanHelper, externalClassLoader );
		mappingParser.parse( mappingStreams );

		configuredBeans = CollectionHelper.toImmutableMap( createBeanConfigurations( mappingParser ) );
		annotationProcessingOptions = mappingParser.getAnnotationProcessingOptions();
	}

	private static Map<HibernateConstrainedType<?>, BeanConfiguration<?>> createBeanConfigurations(MappingXmlParser mappingParser) {
		final Map<HibernateConstrainedType<?>, BeanConfiguration<?>> configuredBeans = new HashMap<>();
		for ( Class<?> clazz : mappingParser.getXmlConfiguredClasses() ) {
			Set<ConstrainedElement> constrainedElements = mappingParser.getConstrainedElementsForClass( clazz );

			JavaBeanConstrainedType<?> constrainedType = new JavaBeanConstrainedType<>( clazz );

			BeanConfiguration<?> beanConfiguration = new BeanConfiguration<>(
					ConfigurationSource.XML,
					constrainedType,
					constrainedElements,
					mappingParser.getDefaultSequenceForClass( clazz ),
					null
			);

			configuredBeans.put( constrainedType, beanConfiguration );
		}
		return configuredBeans;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> BeanConfiguration<T> getBeanConfiguration(HibernateConstrainedType<T> constrainedType) {
		return (BeanConfiguration<T>) configuredBeans.get( constrainedType );
	}

	@Override
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}
}
