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

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.spi.ConfigurationState;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidatorContext;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.ProgrammaticMetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.XmlMetaDataProvider;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Factory returning initialized {@code Validator} instances. This is Hibernate Validator default
 * implementation of the {@code ValidatorFactory} interface.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 * @author Chris Beckey <cbeckey@paypal.com> (C) 2014 ebay, Inc.
 */
public class ValidatorFactoryImpl implements HibernateValidatorFactory {

	private static final Log log = LoggerFactory.make();

	/**
	 * The default message interpolator for this factory.
	 */
	private final MessageInterpolator messageInterpolator;

	/**
	 * The default traversable resolver for this factory.
	 */
	private final TraversableResolver traversableResolver;

	/**
	 * The default parameter name provider for this factory.
	 */
	private final ParameterNameProvider parameterNameProvider;

	/**
	 * The default constraint validator factory for this factory.
	 */
	private final ConstraintValidatorManager constraintValidatorManager;

	/**
	 * Programmatic constraints passed via the Hibernate Validator specific API. Empty if there are
	 * no programmatic constraints
	 */
	private final Set<ConstraintMapping> constraintMappings;

	/**
	 * Helper for dealing with built-in validators and determining custom constraint annotations.
	 */
	private final ConstraintHelper constraintHelper;

	/**
	 * Used for resolving type parameters. Thread-safe.
	 */
	private final ExecutableHelper executableHelper;

	/**
	 * Hibernate Validator specific flag to abort validation on first constraint violation.
	 */
	private final boolean failFast;
	
	/**
	 * Hibernate validator specific flags to relax constraints on parameters.
	 */
	private final boolean allowOverridingMethodAlterParameterConstraint;
	private final boolean allowParallelMethodsDefineGroupConversion;
	private final boolean allowParallelMethodsDefineParameterConstraints;
	
	/**
	 * Metadata provider for XML configuration.
	 */
	private final XmlMetaDataProvider xmlMetaDataProvider;

	/**
	 * Prior to the introduction of {@code ParameterNameProvider} all the bean meta data was static and could be
	 * cached for all created {@code Validator}s. {@code ParameterNameProvider} makes parts of the meta data and
	 * Bean Validation element descriptors dynamic, since depending of the used provider different parameter names
	 * could be used. To still have the metadata static we create a {@code BeanMetaDataManager} per parameter name
	 * provider. See also HV-659.
	 */
	private final Map<ParameterNameProvider, BeanMetaDataManager> beanMetaDataManagerMap;

	public ValidatorFactoryImpl(ConfigurationState configurationState) {
		this.messageInterpolator = configurationState.getMessageInterpolator();
		this.traversableResolver = configurationState.getTraversableResolver();
		this.parameterNameProvider = configurationState.getParameterNameProvider();
		this.beanMetaDataManagerMap = Collections.synchronizedMap( new IdentityHashMap<ParameterNameProvider, BeanMetaDataManager>() );
		this.constraintHelper = new ConstraintHelper();
		this.executableHelper = new ExecutableHelper();
		this.constraintMappings = newHashSet();

		// HV-302; don't load XmlMappingParser if not necessary
		if ( configurationState.getMappingStreams().isEmpty() ) {
			this.xmlMetaDataProvider = null;
		}
		else {
			this.xmlMetaDataProvider = new XmlMetaDataProvider(
					constraintHelper, parameterNameProvider, configurationState.getMappingStreams()
			);
		}

		Map<String, String> properties = configurationState.getProperties();

		boolean tmpFailFast = false;
		boolean tmpAllowOverridingMethodAlterParameterConstraint = false;
		boolean tmpAllowParallelMethodsDefineGroupConversion = false;
		boolean tmpAllowParallelMethodsDefineParameterConstraints = false;
		
		if ( configurationState instanceof ConfigurationImpl ) {
			ConfigurationImpl hibernateSpecificConfig = (ConfigurationImpl) configurationState;

			if ( hibernateSpecificConfig.getProgrammaticMappings().size() > 0 ) {
				constraintMappings.addAll( hibernateSpecificConfig.getProgrammaticMappings() );
			}
			// check whether fail fast is programmatically enabled
			tmpFailFast = hibernateSpecificConfig.getFailFast();

			tmpAllowOverridingMethodAlterParameterConstraint = hibernateSpecificConfig.isAllowOverridingMethodAlterParameterConstraint();
			tmpAllowParallelMethodsDefineGroupConversion = hibernateSpecificConfig.isAllowParallelMethodsDefineGroupConversion();
			tmpAllowParallelMethodsDefineParameterConstraints = hibernateSpecificConfig.isAllowParallelMethodsDefineParameterConstraints();
		}
		
		tmpFailFast = checkPropertiesForBoolean(properties, HibernateValidatorConfiguration.FAIL_FAST, tmpFailFast);
		this.failFast = tmpFailFast;
		
		tmpAllowOverridingMethodAlterParameterConstraint = checkPropertiesForBoolean(properties, HibernateValidatorConfiguration.ALLOW_PARAMETER_CONSTRAINT_OVERRIDE, tmpAllowOverridingMethodAlterParameterConstraint);
		this.allowOverridingMethodAlterParameterConstraint = tmpAllowOverridingMethodAlterParameterConstraint;
		
		tmpAllowParallelMethodsDefineGroupConversion = checkPropertiesForBoolean(properties, HibernateValidatorConfiguration.ALLOW_PARALLEL_METHODS_DEFINE_GROUPS, tmpAllowParallelMethodsDefineGroupConversion);
		this.allowParallelMethodsDefineGroupConversion = tmpAllowParallelMethodsDefineGroupConversion;
		
		tmpAllowParallelMethodsDefineParameterConstraints = checkPropertiesForBoolean(properties, HibernateValidatorConfiguration.ALLOW_PARALLEL_METHODS_DEFINE_PARAMETER_CONSTRAINTS, tmpAllowParallelMethodsDefineParameterConstraints);
		this.allowParallelMethodsDefineParameterConstraints = tmpAllowParallelMethodsDefineParameterConstraints;
		
		this.constraintValidatorManager = new ConstraintValidatorManager( configurationState.getConstraintValidatorFactory() );
	}

	@Override
	public Validator getValidator() {
		return createValidator(
				constraintValidatorManager.getDefaultConstraintValidatorFactory(),
				messageInterpolator,
				traversableResolver,
				parameterNameProvider,
				failFast,
				allowOverridingMethodAlterParameterConstraint,
				allowParallelMethodsDefineGroupConversion,
				allowParallelMethodsDefineParameterConstraints
		);
	}

	@Override
	public MessageInterpolator getMessageInterpolator() {
		return messageInterpolator;
	}

	@Override
	public TraversableResolver getTraversableResolver() {
		return traversableResolver;
	}

	@Override
	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return constraintValidatorManager.getDefaultConstraintValidatorFactory();
	}

	@Override
	public ParameterNameProvider getParameterNameProvider() {
		return parameterNameProvider;
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		//allow unwrapping into public super types
		if ( type.isAssignableFrom( HibernateValidatorFactory.class ) ) {
			return type.cast( this );
		}
		throw log.getTypeNotSupportedForUnwrappingException( type );
	}

	@Override
	public HibernateValidatorContext usingContext() {
		return new ValidatorContextImpl( this );
	}

	@Override
	public void close() {
		constraintValidatorManager.clear();
		for ( BeanMetaDataManager beanMetaDataManager : beanMetaDataManagerMap.values() ) {
			beanMetaDataManager.clear();
		}
	}

	Validator createValidator(ConstraintValidatorFactory constraintValidatorFactory,
							  MessageInterpolator messageInterpolator,
							  TraversableResolver traversableResolver,
							  ParameterNameProvider parameterNameProvider,
							  boolean failFast, 
							  boolean allowOverridingMethodAlterParameterConstraint, 
							  boolean allowParallelMethodsDefineGroupConversion, 
							  boolean allowParallelMethodsDefineParameterConstraints) {
		BeanMetaDataManager beanMetaDataManager;
		if ( !beanMetaDataManagerMap.containsKey( parameterNameProvider ) ) {
			beanMetaDataManager = new BeanMetaDataManager(
					constraintHelper,
					executableHelper,
					parameterNameProvider,
					buildDataProviders( parameterNameProvider ),
					allowOverridingMethodAlterParameterConstraint, 
					allowParallelMethodsDefineGroupConversion, 
					allowParallelMethodsDefineParameterConstraints
			);
			beanMetaDataManagerMap.put( parameterNameProvider, beanMetaDataManager );
		}
		else {
			beanMetaDataManager = beanMetaDataManagerMap.get( parameterNameProvider );
		}

		return new ValidatorImpl(
				constraintValidatorFactory,
				messageInterpolator,
				traversableResolver,
				beanMetaDataManager,
				parameterNameProvider,
				constraintValidatorManager,
				failFast
				
		);
	}

	private List<MetaDataProvider> buildDataProviders(ParameterNameProvider parameterNameProvider) {
		List<MetaDataProvider> metaDataProviders = newArrayList();
		if ( xmlMetaDataProvider != null ) {
			metaDataProviders.add( xmlMetaDataProvider );
		}

		if ( !constraintMappings.isEmpty() ) {
			metaDataProviders.add(
					new ProgrammaticMetaDataProvider(
							constraintHelper,
							parameterNameProvider,
							constraintMappings
					)
			);
		}
		return metaDataProviders;
	}

	private boolean checkPropertiesForBoolean(Map<String, String> properties, String propertyKey, boolean programmaticValue) {
		boolean value = programmaticValue;
		String propertyStringValue = properties.get( propertyKey );
		if ( propertyStringValue != null ) {
			boolean configurationValue = Boolean.valueOf( propertyStringValue );
			// throw an exception if the programmatic value is true and it overrides a false configured value
			if ( programmaticValue && !configurationValue ) {
				throw log.getInconsistentFailFastConfigurationException();
			}
			value = configurationValue;
		}
		return value;
	}
}
