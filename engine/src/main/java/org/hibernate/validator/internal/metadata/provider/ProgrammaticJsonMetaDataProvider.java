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

import org.hibernate.validator.internal.cfg.json.JsonConstraintMappingImpl;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.JsonConfiguration;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * A Json {@link MetaDataProvider} based on the programmatic constraint API.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
public class ProgrammaticJsonMetaDataProvider {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	//	private static final AnnotationProcessingOptions EMPTY_ANNOTATION_PROCESSING_OPTIONS = new AnnotationProcessingOptionsImpl();

	// cached against the fqcn of a class. not a class instance itself (HV-479)
	@Immutable
	private final Map<String, JsonConfiguration<?>> configuredBeans;

	public ProgrammaticJsonMetaDataProvider(ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager,
			Set<JsonConstraintMappingImpl> constraintMappings) {
		Contracts.assertNotNull( constraintMappings );

		configuredBeans = CollectionHelper.toImmutableMap(
				createBeanConfigurations( constraintMappings, constraintHelper, typeResolutionHelper, valueExtractorManager )
		);

		assertUniquenessOfConfiguredTypes( constraintMappings );
	}

	private static void assertUniquenessOfConfiguredTypes(Set<JsonConstraintMappingImpl> mappings) {
		Set<Class<?>> allConfiguredTypes = newHashSet();

		for ( JsonConstraintMappingImpl constraintMapping : mappings ) {
			for ( Class<?> configuredType : constraintMapping.getConfiguredTypes() ) {
				if ( allConfiguredTypes.contains( configuredType ) ) {
					throw LOG.getBeanClassHasAlreadyBeConfiguredViaProgrammaticApiException( configuredType );
				}
			}

			allConfiguredTypes.addAll( constraintMapping.getConfiguredTypes() );
		}
	}

	private static Map<String, JsonConfiguration<?>> createBeanConfigurations(Set<JsonConstraintMappingImpl> mappings, ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager) {
		final Map<String, JsonConfiguration<?>> configuredBeans = new HashMap<>();
		for ( JsonConstraintMappingImpl mapping : mappings ) {
			Set<JsonConfiguration<?>> beanConfigurations = mapping.getBeanConfigurations( constraintHelper, typeResolutionHelper,
					valueExtractorManager
			);

			for ( JsonConfiguration<?> beanConfiguration : beanConfigurations ) {
				configuredBeans.put( beanConfiguration.getBeanClass().getName(), beanConfiguration );
			}
		}
		return configuredBeans;
	}

	@SuppressWarnings("unchecked")
	public JsonConfiguration<?> getBeanConfiguration(Class<?> beanClass) {
		return configuredBeans.get( beanClass.getName() );
	}

}
