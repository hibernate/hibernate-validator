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
package org.hibernate.validator.internal.engine;

import java.util.List;
import java.util.Map;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.spi.ConfigurationState;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidatorContext;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.ProgrammaticMetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.XmlMetaDataProvider;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * Factory returning initialized {@code Validator} instances. This is Hibernate Validator default
 * implementation of the {@code ValidatorFactory} interface.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class ValidatorFactoryImpl implements HibernateValidatorFactory {

	private static final Log log = LoggerFactory.make();

	private final MessageInterpolator messageInterpolator;
	private final TraversableResolver traversableResolver;
	private final ConstraintValidatorFactory constraintValidatorFactory;
	private final ParameterNameProvider parameterNameProvider;
	private final BeanMetaDataManager metaDataManager;
	private final boolean failFast;

	public ValidatorFactoryImpl(ConfigurationState configurationState) {
		this.messageInterpolator = configurationState.getMessageInterpolator();
		this.constraintValidatorFactory = configurationState.getConstraintValidatorFactory();
		this.traversableResolver = configurationState.getTraversableResolver();
		this.parameterNameProvider = configurationState.getParameterNameProvider();
		ConstraintHelper constraintHelper = new ConstraintHelper();

		List<MetaDataProvider> metaDataProviders = newArrayList();

		// HV-302; don't load XmlMappingParser if not necessary
		if ( !configurationState.getMappingStreams().isEmpty() ) {
			metaDataProviders.add(
					new XmlMetaDataProvider(
							constraintHelper, configurationState.getMappingStreams()
					)
			);
		}

		Map<String, String> properties = configurationState.getProperties();

		boolean tmpFailFast = false;
		if ( configurationState instanceof ConfigurationImpl ) {
			ConfigurationImpl hibernateSpecificConfig = (ConfigurationImpl) configurationState;

			if ( hibernateSpecificConfig.getProgrammaticMappings().size() > 0 ) {
				metaDataProviders.add(
						new ProgrammaticMetaDataProvider(
								constraintHelper,
								parameterNameProvider,
								hibernateSpecificConfig.getProgrammaticMappings()
						)
				);
			}
			// check whether fail fast is programmatically enabled
			tmpFailFast = hibernateSpecificConfig.getFailFast();
		}
		tmpFailFast = checkPropertiesForFailFast(
				properties, tmpFailFast
		);
		this.failFast = tmpFailFast;
		metaDataManager = new BeanMetaDataManager( constraintHelper, parameterNameProvider, metaDataProviders );
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
		throw log.getTypeNotSupportedException( type );
	}

	public HibernateValidatorContext usingContext() {
		return new ValidatorContextImpl(
				constraintValidatorFactory,
				messageInterpolator,
				traversableResolver,
				parameterNameProvider,
				metaDataManager,
				failFast
		);
	}

	@Override
	public ParameterNameProvider getParameterNameProvider() {
		return parameterNameProvider;
	}

	@Override
	public void close() {
		// TODO HV-571
		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	private boolean checkPropertiesForFailFast(Map<String, String> properties, boolean programmaticConfiguredFailFast) {
		boolean failFast = programmaticConfiguredFailFast;
		String failFastPropValue = properties.get( HibernateValidatorConfiguration.FAIL_FAST );
		if ( failFastPropValue != null ) {
			boolean tmpFailFast = Boolean.valueOf( failFastPropValue );
			if ( programmaticConfiguredFailFast && !tmpFailFast ) {
				throw log.getInconsistentFailFastConfigurationException();
			}
			failFast = tmpFailFast;
		}
		return failFast;
	}
}
