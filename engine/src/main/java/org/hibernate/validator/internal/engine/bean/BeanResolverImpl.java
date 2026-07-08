/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.bean;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.validation.ValidationException;

import org.hibernate.validator.bean.BeanHolder;
import org.hibernate.validator.bean.BeanReference;
import org.hibernate.validator.bean.BeanResolver;
import org.hibernate.validator.bean.BeanRetrieval;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.actions.LoadClass;
import org.hibernate.validator.internal.util.actions.NewInstance;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.bean.BeanConfigurer;
import org.hibernate.validator.spi.bean.BeanNotFoundException;
import org.hibernate.validator.spi.bean.BeanProvider;

public final class BeanResolverImpl implements BeanResolver {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	public static BeanResolverImpl create(ClassLoader classLoader, List<BeanConfigurer> configurers,
			BeanProvider beanProvider) {
		if ( beanProvider == null ) {
			beanProvider = new NoConfiguredBeanManagerBeanProvider();
		}

		BeanConfigurationContextImpl configurationContext = new BeanConfigurationContextImpl( classLoader );
		for ( BeanConfigurer configurer : configurers ) {
			configurer.configure( configurationContext );
		}

		return new BeanResolverImpl( classLoader, configurationContext.buildRegistry(), beanProvider );
	}

	private final ClassLoader classLoader;
	private final ConfigurationBeanRegistry configurationBeanRegistry;
	private final BeanProvider beanManagerBeanProvider;

	private BeanResolverImpl(ClassLoader classLoader,
			ConfigurationBeanRegistry configurationBeanRegistry,
			BeanProvider beanManagerBeanProvider) {
		this.classLoader = classLoader;
		this.configurationBeanRegistry = configurationBeanRegistry;
		this.beanManagerBeanProvider = beanManagerBeanProvider;
	}

	@Override
	public void close() {
		beanManagerBeanProvider.close();
	}

	@Override
	public <T> BeanHolder<T> resolve(Class<T> typeReference, BeanRetrieval retrieval) {
		Contracts.assertNotNull( typeReference, "typeReference" );
		return resolveFromFirstSuccessfulSource( typeReference, retrieval );
	}

	@Override
	public <T> BeanHolder<T> resolve(Class<T> typeReference, String nameReference, BeanRetrieval retrieval) {
		Contracts.assertNotNull( typeReference, "typeReference" );
		Contracts.assertNotEmpty( nameReference, "nameReference" );
		return resolveFromFirstSuccessfulSource( typeReference, nameReference, retrieval );
	}

	@Override
	public <T> List<BeanReference<T>> allConfiguredForRole(Class<T> role) {
		Contracts.assertNotNull( role, "role" );
		BeanReferenceRegistryForType<T> registry = configurationBeanRegistry.explicitlyConfiguredBeans( role );
		if ( registry == null ) {
			return Collections.emptyList();
		}
		return registry.all();
	}

	@Override
	public <T> Map<String, BeanReference<T>> namedConfiguredForRole(Class<T> role) {
		Contracts.assertNotNull( role, "role" );
		BeanReferenceRegistryForType<T> registry = configurationBeanRegistry.explicitlyConfiguredBeans( role );
		if ( registry == null ) {
			return Collections.emptyMap();
		}
		return registry.named();
	}

	private List<BeanSource> toSources(BeanRetrieval retrieval, boolean hasName) {
		switch ( retrieval ) {
			case BUILTIN:
				return Collections.singletonList( BeanSource.CONFIGURATION );
			case BEAN:
				return Collections.singletonList( BeanSource.BEAN_MANAGER );
			case CLASS:
				return Arrays.asList( BeanSource.BEAN_MANAGER_ASSUME_CLASS_NAME, BeanSource.REFLECTION );
			case CONSTRUCTOR:
				return Collections.singletonList( BeanSource.REFLECTION );
			case ANY:
				return hasName
						? Arrays.asList( BeanSource.CONFIGURATION, BeanSource.BEAN_MANAGER,
								BeanSource.BEAN_MANAGER_ASSUME_CLASS_NAME, BeanSource.REFLECTION )
						: Arrays.asList( BeanSource.CONFIGURATION, BeanSource.BEAN_MANAGER, BeanSource.REFLECTION );
			default:
				throw new IllegalStateException( "Unknown bean retrieval: " + retrieval );
		}
	}

	private <T> BeanHolder<T> resolveFromFirstSuccessfulSource(Class<T> typeReference, BeanRetrieval retrieval) {
		List<BeanSource> sources = toSources( retrieval, false );
		BeanNotFoundException firstFailure = null;
		List<BeanNotFoundException> otherFailures = new ArrayList<>();
		for ( BeanSource source : sources ) {
			try {
				return tryResolve( typeReference, source );
			}
			catch (BeanNotFoundException e) {
				if ( firstFailure == null ) {
					firstFailure = e;
				}
				else {
					otherFailures.add( e );
				}
			}
		}
		throw buildResolutionException( typeReference, null, sources, firstFailure, otherFailures );
	}

	private <T> BeanHolder<T> resolveFromFirstSuccessfulSource(Class<T> typeReference, String nameReference,
			BeanRetrieval retrieval) {
		List<BeanSource> sources = toSources( retrieval, true );
		BeanNotFoundException firstFailure = null;
		List<BeanNotFoundException> otherFailures = new ArrayList<>();
		for ( BeanSource source : sources ) {
			try {
				return tryResolve( typeReference, nameReference, source );
			}
			catch (BeanNotFoundException e) {
				if ( firstFailure == null ) {
					firstFailure = e;
				}
				else {
					otherFailures.add( e );
				}
			}
		}
		throw buildResolutionException( typeReference, nameReference, sources, firstFailure, otherFailures );
	}

	private <T> BeanHolder<T> tryResolve(Class<T> typeReference, BeanSource source) {
		switch ( source ) {
			case CONFIGURATION:
				return configurationBeanRegistry.resolve( typeReference, this );
			case BEAN_MANAGER:
			case BEAN_MANAGER_ASSUME_CLASS_NAME:
				return beanManagerBeanProvider.forType( typeReference );
			case REFLECTION:
				return retrieveUsingConstructor( typeReference );
			default:
				throw new IllegalStateException( "Unknown bean source: " + source );
		}
	}

	private <T> BeanHolder<T> tryResolve(Class<T> typeReference, String nameReference, BeanSource source) {
		switch ( source ) {
			case CONFIGURATION:
				return configurationBeanRegistry.resolve( typeReference, nameReference, this );
			case BEAN_MANAGER:
				return beanManagerBeanProvider.forTypeAndName( typeReference, nameReference );
			case BEAN_MANAGER_ASSUME_CLASS_NAME:
				return beanManagerBeanProvider.forType( toClass( typeReference, nameReference ) );
			case REFLECTION:
				return retrieveUsingConstructor( toClass( typeReference, nameReference ) );
			default:
				throw new IllegalStateException( "Unknown bean source: " + source );
		}
	}

	private <T> Class<? extends T> toClass(Class<T> typeReference, String nameReference) {
		try {
			Class<?> loaded = LoadClass.action( nameReference, classLoader );
			return loaded.asSubclass( typeReference );
		}
		catch (RuntimeException e) {
			throw LOG.getUnableToResolveClassNameException( nameReference, typeReference.getName(), e );
		}
	}

	private <T> BeanHolder<T> retrieveUsingConstructor(Class<T> typeReference) {
		try {
			T instance = NewInstance.action( typeReference, "bean" );
			return BeanHolder.of( instance );
		}
		catch (RuntimeException e) {
			throw LOG.getUnableToCreateBeanUsingReflectionException( typeReference.getName(), e );
		}
	}

	private ValidationException buildResolutionException(Class<?> typeReference, String nameReference,
			List<BeanSource> sources, BeanNotFoundException firstFailure,
			List<BeanNotFoundException> otherFailures) {
		StringBuilder message = new StringBuilder();
		if ( nameReference != null ) {
			message.append( String.format( Locale.ROOT, "Unable to resolve bean of type '%s' with name '%s'. ",
					typeReference.getName(), nameReference ) );
		}
		else {
			message.append( String.format( Locale.ROOT, "Unable to resolve bean of type '%s'. ",
					typeReference.getName() ) );
		}
		message.append( "Attempted sources: " );
		message.append( renderFailure( sources.get( 0 ), firstFailure ) );
		for ( int i = 0; i < otherFailures.size(); i++ ) {
			message.append( " " );
			message.append( renderFailure( sources.get( i + 1 ), otherFailures.get( i ) ) );
		}
		ValidationException exception = new ValidationException( message.toString(), firstFailure );
		for ( BeanNotFoundException otherFailure : otherFailures ) {
			exception.addSuppressed( otherFailure );
		}
		return exception;
	}

	private String renderFailure(BeanSource source, RuntimeException failure) {
		switch ( source ) {
			case CONFIGURATION:
				return "Internal registry: " + failure.getMessage() + ".";
			case BEAN_MANAGER:
			case BEAN_MANAGER_ASSUME_CLASS_NAME:
				return "Bean manager: " + failure.getMessage() + ".";
			case REFLECTION:
				return "Reflection: " + failure.getMessage() + ".";
			default:
				throw new IllegalStateException( "Unknown bean source: " + source );
		}
	}
}
