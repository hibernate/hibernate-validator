/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.engine;

import java.util.List;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.spi.ConfigurationState;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidatorContext;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.metadata.BeanMetaDataManager;
import org.hibernate.validator.metadata.core.ConstraintHelper;
import org.hibernate.validator.metadata.provider.MetaDataProvider;
import org.hibernate.validator.metadata.provider.ProgrammaticMappingMetaDataProvider;
import org.hibernate.validator.metadata.provider.XmlConfigurationMetaDataProvider;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;

/**
 * Factory returning initialized {@code Validator} instances. This is Hibernate Validator default
 * implementation of the {@code ValidatorFactory} interface.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class ValidatorFactoryImpl implements HibernateValidatorFactory {

	private final MessageInterpolator messageInterpolator;
	private final TraversableResolver traversableResolver;
	private final ConstraintValidatorFactory constraintValidatorFactory;
	private final ConstraintHelper constraintHelper;
	private final BeanMetaDataManager metaDataManager;
	private final boolean failFast;

	public ValidatorFactoryImpl(ConfigurationState configurationState) {

		this.messageInterpolator = configurationState.getMessageInterpolator();
		this.constraintValidatorFactory = configurationState.getConstraintValidatorFactory();
		this.traversableResolver = configurationState.getTraversableResolver();
		this.constraintHelper = new ConstraintHelper();

		boolean tmpFailFast = false;

		List<MetaDataProvider> metaDataProviders = newArrayList();

		// HV-302; don't load XmlMappingParser if not necessary
		if ( !configurationState.getMappingStreams().isEmpty() ) {
			metaDataProviders.add(
					new XmlConfigurationMetaDataProvider(
							constraintHelper, configurationState.getMappingStreams()
					)
			);
		}

		if ( configurationState instanceof ConfigurationImpl ) {
			ConfigurationImpl hibernateSpecificConfig = (ConfigurationImpl) configurationState;

			if ( hibernateSpecificConfig.getMapping() != null ) {
				metaDataProviders.add(
						new ProgrammaticMappingMetaDataProvider(
								constraintHelper,
								hibernateSpecificConfig.getMapping()
						)
				);
			}
			// check whether fail fast is programmatically enabled
			tmpFailFast = hibernateSpecificConfig.getFailFast();
		}
		tmpFailFast = checkPropertiesForFailFast(
				configurationState, tmpFailFast
		);

		this.failFast = tmpFailFast;

		metaDataManager = new BeanMetaDataManager( constraintHelper, metaDataProviders );
	}

	public Validator getValidator() {
		return usingContext().getValidator();
	}

	public MessageInterpolator getMessageInterpolator() {
		return messageInterpolator;
	}

	public TraversableResolver getTraversableResolver() {
		return traversableResolver;
	}

	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return constraintValidatorFactory;
	}

	public <T> T unwrap(Class<T> type) {
		if ( HibernateValidatorFactory.class.equals( type ) ) {
			return type.cast( this );
		}
		throw new ValidationException( "Type " + type + " not supported" );
	}

	public HibernateValidatorContext usingContext() {
		return new ValidatorContextImpl(
				constraintValidatorFactory,
				messageInterpolator,
				traversableResolver,
				constraintHelper,
				metaDataManager,
				failFast
		);
	}

	private boolean checkPropertiesForFailFast(ConfigurationState configurationState, boolean programmaticConfiguredFailFast) {
		boolean failFast = programmaticConfiguredFailFast;
		String failFastPropValue = configurationState.getProperties().get( HibernateValidatorConfiguration.FAIL_FAST );
		if ( failFastPropValue != null ) {
			boolean tmpFailFast = Boolean.valueOf( failFastPropValue );
			if ( programmaticConfiguredFailFast && !tmpFailFast ) {
				throw new ValidationException(
						"Inconsistent fail fast configuration. Fail fast enabled via programmatic API, " +
								"but explicitly disabled via properties"
				);
			}
			failFast = tmpFailFast;
		}
		return failFast;
	}
}
