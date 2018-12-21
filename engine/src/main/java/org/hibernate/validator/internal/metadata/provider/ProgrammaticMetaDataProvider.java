/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.provider;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * A {@link MetaDataProvider} based on the programmatic constraint API.
 *
 * @author Gunnar Morling
 */
public class ProgrammaticMetaDataProvider implements MetaDataProvider {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	// cached against the fqcn of a class. not a class instance itself (HV-479)
	@Immutable
	private final Map<String, BeanConfiguration<?>> configuredBeans;
	private final AnnotationProcessingOptions annotationProcessingOptions;

	public ProgrammaticMetaDataProvider(ConstraintCreationContext constraintCreationContext,
										Set<DefaultConstraintMapping> constraintMappings) {
		Contracts.assertNotNull( constraintMappings );

		configuredBeans = CollectionHelper.toImmutableMap(
				createBeanConfigurations( constraintMappings, constraintCreationContext )
		);

		assertUniquenessOfConfiguredTypes( constraintMappings );
		annotationProcessingOptions = mergeAnnotationProcessingOptions( constraintMappings );
	}

	private static void assertUniquenessOfConfiguredTypes(Set<DefaultConstraintMapping> mappings) {
		Set<Class<?>> allConfiguredTypes = newHashSet();

		for ( DefaultConstraintMapping constraintMapping : mappings ) {
			for ( Class<?> configuredType : constraintMapping.getConfiguredTypes() ) {
				if ( allConfiguredTypes.contains( configuredType ) ) {
					throw LOG.getBeanClassHasAlreadyBeConfiguredViaProgrammaticApiException( configuredType );
				}
			}

			allConfiguredTypes.addAll( constraintMapping.getConfiguredTypes() );
		}
	}

	private static Map<String, BeanConfiguration<?>> createBeanConfigurations(Set<DefaultConstraintMapping> mappings,
			ConstraintCreationContext constraintCreationContext) {
		final Map<String, BeanConfiguration<?>> configuredBeans = new HashMap<>();
		for ( DefaultConstraintMapping mapping : mappings ) {
			Set<BeanConfiguration<?>> beanConfigurations = mapping.getBeanConfigurations( constraintCreationContext );

			for ( BeanConfiguration<?> beanConfiguration : beanConfigurations ) {
				configuredBeans.put( beanConfiguration.getBeanClass().getName(), beanConfiguration );
			}
		}
		return configuredBeans;
	}

	/**
	 * Creates a single merged {@code AnnotationProcessingOptions} in case multiple programmatic mappings are provided.
	 * <p>
	 * Note that it is made sure at this point that no element (type, property, method etc.) is configured more than once within
	 * all the given contexts. So the "merge" pulls together the information for all configured elements, but it will never
	 * merge several configurations for one given element.
	 *
	 * @param mappings set of mapping contexts providing annotation processing options to be merged
	 *
	 * @return a single annotation processing options object
	 */
	private static AnnotationProcessingOptions mergeAnnotationProcessingOptions(Set<DefaultConstraintMapping> mappings) {
		// if we only have one mapping we can return the context of just this mapping
		if ( mappings.size() == 1 ) {
			return mappings.iterator().next().getAnnotationProcessingOptions();
		}

		AnnotationProcessingOptions options = new AnnotationProcessingOptionsImpl();

		for ( DefaultConstraintMapping mapping : mappings ) {
			options.merge( mapping.getAnnotationProcessingOptions() );
		}

		return options;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> BeanConfiguration<T> getBeanConfiguration(Class<T> beanClass) {
		return (BeanConfiguration<T>) configuredBeans.get( beanClass.getName() );
	}

	@Override
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}
}
