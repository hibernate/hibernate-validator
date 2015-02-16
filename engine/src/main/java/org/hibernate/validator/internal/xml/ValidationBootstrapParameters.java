/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.validation.BootstrapConfiguration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

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
	private final Map<String, String> configProperties = newHashMap();
	private final Set<InputStream> mappings = newHashSet();

	public ValidationBootstrapParameters() {
	}

	public ValidationBootstrapParameters(BootstrapConfiguration bootstrapConfiguration, ClassLoader externalClassLoader) {
		setProviderClass( bootstrapConfiguration.getDefaultProviderClassName(), externalClassLoader );
		setMessageInterpolator( bootstrapConfiguration.getMessageInterpolatorClassName(), externalClassLoader );
		setTraversableResolver( bootstrapConfiguration.getTraversableResolverClassName(), externalClassLoader );
		setConstraintFactory( bootstrapConfiguration.getConstraintValidatorFactoryClassName(), externalClassLoader );
		setParameterNameProvider( bootstrapConfiguration.getParameterNameProviderClassName(), externalClassLoader );
		setMappingStreams( bootstrapConfiguration.getConstraintMappingResourcePaths(), externalClassLoader );
		setConfigProperties( bootstrapConfiguration.getProperties() );
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
	private void setProviderClass(String providerFqcn, ClassLoader externalClassLoader) {
		if ( providerFqcn != null ) {
			try {
				providerClass = (Class<? extends ValidationProvider<?>>) run(
						LoadClass.action( providerFqcn, externalClassLoader )
				);
				log.usingValidationProvider( providerFqcn );
			}
			catch ( Exception e ) {
				throw log.getUnableToInstantiateValidationProviderClassException( providerFqcn, e );
			}
		}
	}

	private void setMessageInterpolator(String messageInterpolatorFqcn, ClassLoader externalClassLoader) {
		if ( messageInterpolatorFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<MessageInterpolator> messageInterpolatorClass = (Class<MessageInterpolator>) run(
						LoadClass.action( messageInterpolatorFqcn, externalClassLoader )
				);
				messageInterpolator = run( NewInstance.action( messageInterpolatorClass, "message interpolator" ) );
				log.usingMessageInterpolator( messageInterpolatorFqcn );
			}
			catch ( ValidationException e ) {
				throw log.getUnableToInstantiateMessageInterpolatorClassException( messageInterpolatorFqcn, e );
			}
		}
	}

	private void setTraversableResolver(String traversableResolverFqcn, ClassLoader externalClassLoader) {
		if ( traversableResolverFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<TraversableResolver> clazz = (Class<TraversableResolver>) run(
						LoadClass.action( traversableResolverFqcn, externalClassLoader )
				);
				traversableResolver = run( NewInstance.action( clazz, "traversable resolver" ) );
				log.usingTraversableResolver( traversableResolverFqcn );
			}
			catch ( ValidationException e ) {
				throw log.getUnableToInstantiateTraversableResolverClassException( traversableResolverFqcn, e );
			}
		}
	}

	private void setConstraintFactory(String constraintFactoryFqcn, ClassLoader externalClassLoader) {
		if ( constraintFactoryFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<ConstraintValidatorFactory> clazz = (Class<ConstraintValidatorFactory>) run(
						LoadClass.action( constraintFactoryFqcn, externalClassLoader )
				);
				constraintValidatorFactory = run( NewInstance.action( clazz, "constraint factory class" ) );
				log.usingConstraintFactory( constraintFactoryFqcn );
			}
			catch ( ValidationException e ) {
				throw log.getUnableToInstantiateConstraintFactoryClassException( constraintFactoryFqcn, e );
			}
		}
	}

	private void setParameterNameProvider(String parameterNameProviderFqcn, ClassLoader externalClassLoader) {
		if ( parameterNameProviderFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<ParameterNameProvider> clazz = (Class<ParameterNameProvider>) run(
						LoadClass.action( parameterNameProviderFqcn, externalClassLoader )
				);
				parameterNameProvider = run( NewInstance.action( clazz, "parameter name provider class" ) );
				log.usingParameterNameProvider( parameterNameProviderFqcn );
			}
			catch ( ValidationException e ) {
				throw log.getUnableToInstantiateParameterNameProviderClassException( parameterNameProviderFqcn, e );
			}
		}
	}

	private void setMappingStreams(Set<String> mappingFileNames, ClassLoader externalClassLoader) {
		for ( String mappingFileName : mappingFileNames ) {
			log.debugf( "Trying to open input stream for %s.", mappingFileName );

			InputStream in = ResourceLoaderHelper.getResettableInputStreamForPath( mappingFileName, externalClassLoader );
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

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
