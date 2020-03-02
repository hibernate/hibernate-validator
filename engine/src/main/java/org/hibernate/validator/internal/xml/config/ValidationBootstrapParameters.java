/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.config;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.validation.BootstrapConfiguration;
import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;
import jakarta.validation.ValidationException;
import jakarta.validation.spi.ValidationProvider;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor.Key;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

/**
 * @author Hardy Ferentschik
 */
public class ValidationBootstrapParameters {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private ConstraintValidatorFactory constraintValidatorFactory;
	private MessageInterpolator messageInterpolator;
	private TraversableResolver traversableResolver;
	private ParameterNameProvider parameterNameProvider;
	private ClockProvider clockProvider;
	private ValidationProvider<?> provider;
	private Class<? extends ValidationProvider<?>> providerClass = null;
	private final Map<String, String> configProperties = new HashMap<>();
	private final Set<InputStream> mappings = new HashSet<>();
	private final Map<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> valueExtractorDescriptors = new HashMap<>();
	private PropertyNodeNameProvider propertyNodeNameProvider;

	public ValidationBootstrapParameters() {
	}

	public ValidationBootstrapParameters(BootstrapConfiguration bootstrapConfiguration, ClassLoader externalClassLoader) {
		setProviderClass( bootstrapConfiguration.getDefaultProviderClassName(), externalClassLoader );
		setMessageInterpolator( bootstrapConfiguration.getMessageInterpolatorClassName(), externalClassLoader );
		setTraversableResolver( bootstrapConfiguration.getTraversableResolverClassName(), externalClassLoader );
		setConstraintValidatorFactory( bootstrapConfiguration.getConstraintValidatorFactoryClassName(), externalClassLoader );
		setParameterNameProvider( bootstrapConfiguration.getParameterNameProviderClassName(), externalClassLoader );
		setClockProvider( bootstrapConfiguration.getClockProviderClassName(), externalClassLoader );
		setValueExtractors( bootstrapConfiguration.getValueExtractorClassNames(), externalClassLoader );
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
		return CollectionHelper.toImmutableSet( mappings );
	}

	public final Map<String, String> getConfigProperties() {
		return CollectionHelper.toImmutableMap( configProperties );
	}

	public ParameterNameProvider getParameterNameProvider() {
		return parameterNameProvider;
	}

	public void setParameterNameProvider(ParameterNameProvider parameterNameProvider) {
		this.parameterNameProvider = parameterNameProvider;
	}

	public ClockProvider getClockProvider() {
		return clockProvider;
	}

	public void setClockProvider(ClockProvider clockProvider) {
		this.clockProvider = clockProvider;
	}

	public Map<Key, ValueExtractorDescriptor> getValueExtractorDescriptors() {
		return valueExtractorDescriptors;
	}

	public void addValueExtractorDescriptor(ValueExtractorDescriptor descriptor) {
		valueExtractorDescriptors.put( descriptor.getKey(), descriptor );
	}

	public PropertyNodeNameProvider getPropertyNodeNameProvider() {
		return propertyNodeNameProvider;
	}

	public void setPropertyNodeNameProvider(PropertyNodeNameProvider propertyNodeNameProvider) {
		this.propertyNodeNameProvider = propertyNodeNameProvider;
	}

	@SuppressWarnings("unchecked")
	private void setProviderClass(String providerFqcn, ClassLoader externalClassLoader) {
		if ( providerFqcn != null ) {
			try {
				providerClass = (Class<? extends ValidationProvider<?>>) run(
						LoadClass.action( providerFqcn, externalClassLoader )
				);
				LOG.usingValidationProvider( providerClass );
			}
			catch (Exception e) {
				throw LOG.getUnableToInstantiateValidationProviderClassException( providerFqcn, e );
			}
		}
	}

	private void setMessageInterpolator(String messageInterpolatorFqcn, ClassLoader externalClassLoader) {
		if ( messageInterpolatorFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends MessageInterpolator> messageInterpolatorClass = (Class<? extends MessageInterpolator>) run(
						LoadClass.action( messageInterpolatorFqcn, externalClassLoader )
				);
				messageInterpolator = run( NewInstance.action( messageInterpolatorClass, "message interpolator" ) );
				LOG.usingMessageInterpolator( messageInterpolatorClass );
			}
			catch (ValidationException e) {
				throw LOG.getUnableToInstantiateMessageInterpolatorClassException( messageInterpolatorFqcn, e );
			}
		}
	}

	private void setTraversableResolver(String traversableResolverFqcn, ClassLoader externalClassLoader) {
		if ( traversableResolverFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends TraversableResolver> clazz = (Class<? extends TraversableResolver>) run(
						LoadClass.action( traversableResolverFqcn, externalClassLoader )
				);
				traversableResolver = run( NewInstance.action( clazz, "traversable resolver" ) );
				LOG.usingTraversableResolver( clazz );
			}
			catch (ValidationException e) {
				throw LOG.getUnableToInstantiateTraversableResolverClassException( traversableResolverFqcn, e );
			}
		}
	}

	private void setConstraintValidatorFactory(String constraintValidatorFactoryFqcn, ClassLoader externalClassLoader) {
		if ( constraintValidatorFactoryFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends ConstraintValidatorFactory> clazz = (Class<? extends ConstraintValidatorFactory>) run(
						LoadClass.action( constraintValidatorFactoryFqcn, externalClassLoader )
				);
				constraintValidatorFactory = run( NewInstance.action( clazz, "constraint validator factory class" ) );
				LOG.usingConstraintValidatorFactory( clazz );
			}
			catch (ValidationException e) {
				throw LOG.getUnableToInstantiateConstraintValidatorFactoryClassException( constraintValidatorFactoryFqcn, e );
			}
		}
	}

	private void setParameterNameProvider(String parameterNameProviderFqcn, ClassLoader externalClassLoader) {
		if ( parameterNameProviderFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends ParameterNameProvider> clazz = (Class<? extends ParameterNameProvider>) run(
						LoadClass.action( parameterNameProviderFqcn, externalClassLoader )
				);
				parameterNameProvider = run( NewInstance.action( clazz, "parameter name provider class" ) );
				LOG.usingParameterNameProvider( clazz );
			}
			catch (ValidationException e) {
				throw LOG.getUnableToInstantiateParameterNameProviderClassException( parameterNameProviderFqcn, e );
			}
		}
	}

	private void setClockProvider(String clockProviderFqcn, ClassLoader externalClassLoader) {
		if ( clockProviderFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends ClockProvider> clazz = (Class<? extends ClockProvider>) run(
						LoadClass.action( clockProviderFqcn, externalClassLoader )
				);
				clockProvider = run( NewInstance.action( clazz, "clock provider class" ) );
				LOG.usingClockProvider( clazz );
			}
			catch (ValidationException e) {
				throw LOG.getUnableToInstantiateClockProviderClassException( clockProviderFqcn, e );
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void setValueExtractors(Set<String> valueExtractorFqcns, ClassLoader externalClassLoader) {
		for ( String valueExtractorFqcn : valueExtractorFqcns ) {
			ValueExtractor<?> valueExtractor;

			try {
				Class<? extends ValueExtractor<?>> clazz = (Class<? extends ValueExtractor<?>>) run(
						LoadClass.action( valueExtractorFqcn, externalClassLoader )
				);
				valueExtractor = run( NewInstance.action( clazz, "value extractor class" ) );
			}
			catch (ValidationException e) {
				throw LOG.getUnableToInstantiateValueExtractorClassException( valueExtractorFqcn, e );
			}


			ValueExtractorDescriptor descriptor = new ValueExtractorDescriptor( valueExtractor );
			ValueExtractorDescriptor previous = valueExtractorDescriptors.put( descriptor.getKey(), descriptor );

			if ( previous != null ) {
				throw LOG.getValueExtractorForTypeAndTypeUseAlreadyPresentException( valueExtractor, previous.getValueExtractor() );
			}

			LOG.addingValueExtractor( (Class<? extends ValueExtractor<?>>) valueExtractor.getClass() );
		}
	}

	private void setMappingStreams(Set<String> mappingFileNames, ClassLoader externalClassLoader) {
		for ( String mappingFileName : mappingFileNames ) {
			LOG.debugf( "Trying to open input stream for %s.", mappingFileName );

			InputStream in = ResourceLoaderHelper.getResettableInputStreamForPath( mappingFileName, externalClassLoader );
			if ( in == null ) {
				throw LOG.getUnableToOpenInputStreamForMappingFileException( mappingFileName );
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
