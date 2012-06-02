/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.xml;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.validation.ConfigurationSource;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.ResourceLoaderHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 */
public class ValidationBootstrapParameters {
	private static final Log log = LoggerFactory.make();

	private ConstraintValidatorFactory constraintValidatorFactory;
	private MessageInterpolator messageInterpolator;
	private TraversableResolver traversableResolver;
	private ParameterNameProvider parameterNameProvider;
	private ValidationProvider<?> provider;
	private Class<? extends ValidationProvider<?>> providerClass = null;
	private final Map<String, String> configProperties = new HashMap<String, String>();
	private final Set<InputStream> mappings = new HashSet<InputStream>();

	public ValidationBootstrapParameters() {
	}

	public ValidationBootstrapParameters(ConfigurationSource configurationSource) {
		setProviderClass( configurationSource.getDefaultProviderClassName() );
		setMessageInterpolator( configurationSource.getMessageInterpolatorClassName() );
		setTraversableResolver( configurationSource.getTraversableResolverClassName() );
		setConstraintFactory( configurationSource.getConstraintValidatorFactoryClassName() );
		setParameterNameProvider( configurationSource.getParameterNameProviderClassName() );
		setMappingStreams( configurationSource.getConstraintMappingResourcePath() );
		setConfigProperties( configurationSource.getProperties() );
	}

	public final ConstraintValidatorFactory getConstraintValidatorFactory() {
		return constraintValidatorFactory;
	}

	public final void setConstraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
		this.constraintValidatorFactory = constraintValidatorFactory;
	}

	public final MessageInterpolator getMessageInterpolator() {
		return messageInterpolator;
	}

	public final void setMessageInterpolator(MessageInterpolator messageInterpolator) {
		this.messageInterpolator = messageInterpolator;
	}

	public final ValidationProvider<?> getProvider() {
		return provider;
	}

	public final void setProvider(ValidationProvider<?> provider) {
		this.provider = provider;
	}

	public final Class<? extends ValidationProvider<?>> getProviderClass() {
		return providerClass;
	}

	public final void setProviderClass(Class<? extends ValidationProvider<?>> providerClass) {
		this.providerClass = providerClass;
	}

	public final TraversableResolver getTraversableResolver() {
		return traversableResolver;
	}

	public final void setTraversableResolver(TraversableResolver traversableResolver) {
		this.traversableResolver = traversableResolver;
	}

	public final void addConfigProperty(String key, String value) {
		configProperties.put( key, value );
	}

	public final void addMapping(InputStream in) {
		mappings.add( in );
	}

	public final void addAllMappings(Set<InputStream> mappings) {
		this.mappings.addAll( mappings );
	}

	public final Set<InputStream> getMappings() {
		return Collections.unmodifiableSet( mappings );
	}

	public final Map<String, String> getConfigProperties() {
		return Collections.unmodifiableMap( configProperties );
	}

	public ParameterNameProvider getParameterNameProvider() {
		return parameterNameProvider;
	}

	public void setParameterNameProvider(ParameterNameProvider parameterNameProvider) {
		this.parameterNameProvider = parameterNameProvider;
	}

	@SuppressWarnings("unchecked")
	private void setProviderClass(String providerFqcn) {
		if ( providerFqcn != null ) {
			try {
				providerClass = (Class<? extends ValidationProvider<?>>) ReflectionHelper.loadClass(
						providerFqcn,
						this.getClass()
				);
				log.usingValidationProvider( providerFqcn );
			}
			catch ( Exception e ) {
				throw log.getUnableToInstantiateValidationProviderClassException( providerFqcn, e );
			}
		}
	}

	private void setMessageInterpolator(String messageInterpolatorFqcn) {
		if ( messageInterpolatorFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<MessageInterpolator> messageInterpolatorClass = (Class<MessageInterpolator>) ReflectionHelper.loadClass(
						messageInterpolatorFqcn, this.getClass()
				);
				messageInterpolator = ReflectionHelper.newInstance( messageInterpolatorClass, "message interpolator" );
				log.usingMessageInterpolator( messageInterpolatorFqcn );
			}
			catch ( ValidationException e ) {
				throw log.getUnableToInstantiateMessageInterpolatorClassException( messageInterpolatorFqcn, e );
			}
		}
	}

	private void setTraversableResolver(String traversableResolverFqcn) {
		if ( traversableResolverFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<TraversableResolver> clazz = (Class<TraversableResolver>) ReflectionHelper.loadClass(
						traversableResolverFqcn, this.getClass()
				);
				traversableResolver = ReflectionHelper.newInstance( clazz, "traversable resolver" );
				log.usingTraversableResolver( traversableResolverFqcn );
			}
			catch ( ValidationException e ) {
				throw log.getUnableToInstantiateTraversableResolverClassException( traversableResolverFqcn, e );
			}
		}
	}

	private void setConstraintFactory(String constraintFactoryFqcn) {
		if ( constraintFactoryFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<ConstraintValidatorFactory> clazz = (Class<ConstraintValidatorFactory>) ReflectionHelper.loadClass(
						constraintFactoryFqcn, this.getClass()
				);
				constraintValidatorFactory = ReflectionHelper.newInstance( clazz, "constraint factory class" );
				log.usingConstraintFactory( constraintFactoryFqcn );
			}
			catch ( ValidationException e ) {
				throw log.getUnableToInstantiateConstraintFactoryClassException( constraintFactoryFqcn, e );
			}
		}
	}

	private void setParameterNameProvider(String parameterNameProviderFqcn) {
		if ( parameterNameProviderFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<ParameterNameProvider> clazz = (Class<ParameterNameProvider>) ReflectionHelper.loadClass(
						parameterNameProviderFqcn, this.getClass()
				);
				parameterNameProvider = ReflectionHelper.newInstance( clazz, "parameter name provider class" );
				log.usingParameterNameProvider( parameterNameProviderFqcn );
			}
			catch ( ValidationException e ) {
				throw log.getUnableToInstantiateParameterNameProviderClassException( parameterNameProviderFqcn, e );
			}
		}
	}

	private void setMappingStreams(Set<String> mappingFileNames) {
		for ( String mappingFileName : mappingFileNames ) {
			log.debugf( "Trying to open input stream for %s.", mappingFileName );

			InputStream in = ResourceLoaderHelper.getInputStreamForPath( mappingFileName );
			if ( in == null ) {
				throw log.getUnableToOpenInputStreamForMappingFileException( mappingFileName );
			}
			mappings.add( in );
		}
	}

	private void setConfigProperties(Map<String, String> properties) {
		for ( Map.Entry<String, String> entry : properties.entrySet() ) {
			configProperties.put( entry.getKey(), entry.getValue() );
		}
	}
}
