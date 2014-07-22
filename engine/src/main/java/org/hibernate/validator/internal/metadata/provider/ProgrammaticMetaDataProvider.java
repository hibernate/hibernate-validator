/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.metadata.provider;

import java.util.Set;

import javax.validation.ParameterNameProvider;

import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * A {@link MetaDataProvider} based on the programmatic constraint API.
 *
 * @author Gunnar Morling
 */
public class ProgrammaticMetaDataProvider extends MetaDataProviderKeyedByClassName {

	private static final Log log = LoggerFactory.make();

	private final AnnotationProcessingOptions annotationProcessingOptions;
	private final ParameterNameProvider parameterNameProvider;

	public ProgrammaticMetaDataProvider(ConstraintHelper constraintHelper,
										ParameterNameProvider parameterNameProvider,
										Set<DefaultConstraintMapping> constraintMappings) {
		super( constraintHelper );
		Contracts.assertNotNull( constraintMappings );
		this.parameterNameProvider = parameterNameProvider;

		assertUniquenessOfConfiguredTypes( constraintMappings );
		addAllBeanConfigurations( constraintMappings );
		annotationProcessingOptions = mergeAnnotationProcessingOptions( constraintMappings );
	}

	private void assertUniquenessOfConfiguredTypes(Set<DefaultConstraintMapping> mappings) {
		Set<Class<?>> allConfiguredTypes = newHashSet();

		for ( DefaultConstraintMapping constraintMapping : mappings ) {
			for ( Class<?> configuredType : constraintMapping.getConfiguredTypes() ) {
				if ( allConfiguredTypes.contains( configuredType ) ) {
					throw log.getBeanClassHasAlreadyBeConfiguredViaProgrammaticApiException( configuredType.getName() );
				}
			}

			allConfiguredTypes.addAll( constraintMapping.getConfiguredTypes() );
		}
	}

	private void addAllBeanConfigurations(Set<DefaultConstraintMapping> mappings) {
		for ( DefaultConstraintMapping mapping : mappings ) {
			Set<BeanConfiguration<?>> beanConfigurations = mapping.getBeanConfigurations(
					constraintHelper,
					parameterNameProvider
			);

			for ( BeanConfiguration<?> beanConfiguration : beanConfigurations ) {
				addBeanConfiguration( beanConfiguration.getBeanClass(), beanConfiguration );
			}
		}
	}

	/**
	 * Creates a single merged {@code AnnotationProcessingOptions} in case multiple programmatic mappings are provided.
	 * <p>
	 * Note that it is made sure at this point that no element (type, property, method etc.) is configured more than once within
	 * all the given contexts. So the "merge" pulls together the information for all configured elements, but it will never
	 * merge several configurations for one given element.
	 *
	 * @param contexts set of mapping contexts providing annotation processing options to be merged
	 *
	 * @return a single annotation processing options object
	 */
	private AnnotationProcessingOptions mergeAnnotationProcessingOptions(Set<DefaultConstraintMapping> mappings) {
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
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}
}
