/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.PredefinedScopeHibernateValidatorConfiguration;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;

/**
 * @author Guillaume Smet
 */
public class PredefinedScopeConfigurationImpl extends AbstractConfigurationImpl<PredefinedScopeHibernateValidatorConfiguration>
		implements PredefinedScopeHibernateValidatorConfiguration, ConfigurationState {

	private Set<Class<?>> beanClassesToInitialize;

	private BeanMetaDataClassNormalizer beanMetaDataClassNormalizer;

	/**
	 * Locales to initialize eagerly.
	 * <p>
	 * We will always include the default locale in the final list.
	 */
	private Set<Locale> localesToInitialize = Collections.emptySet();

	public PredefinedScopeConfigurationImpl(BootstrapState state) {
		super( state );
	}

	public PredefinedScopeConfigurationImpl(ValidationProvider<?> validationProvider) {
		super( validationProvider );
	}

	@Override
	public PredefinedScopeHibernateValidatorConfiguration initializeBeanMetaData(Set<Class<?>> beanMetaDataToInitialize) {
		beanClassesToInitialize = CollectionHelper.toImmutableSet( beanMetaDataToInitialize );
		return thisAsT();
	}

	public Set<Class<?>> getBeanClassesToInitialize() {
		return beanClassesToInitialize;
	}

	@Override
	public PredefinedScopeHibernateValidatorConfiguration initializeLocales(Set<Locale> localesToInitialize) {
		Contracts.assertNotNull( localesToInitialize, MESSAGES.parameterMustNotBeNull( "localesToInitialize" ) );
		this.localesToInitialize = localesToInitialize;
		return thisAsT();
	}

	@Override
	public PredefinedScopeHibernateValidatorConfiguration beanMetaDataClassNormalizer(BeanMetaDataClassNormalizer beanMetaDataClassNormalizer) {
		this.beanMetaDataClassNormalizer = beanMetaDataClassNormalizer;
		return thisAsT();
	}

	public BeanMetaDataClassNormalizer getBeanMetaDataClassNormalizer() {
		return beanMetaDataClassNormalizer;
	}

	@Override
	protected Set<Locale> getAllLocalesToInitialize() {
		if ( localesToInitialize.isEmpty() ) {
			return Collections.singleton( getDefaultLocale() );
		}

		Set<Locale> allLocales = CollectionHelper.newHashSet( localesToInitialize.size() + 1 );
		allLocales.addAll( localesToInitialize );
		allLocales.add( getDefaultLocale() );
		return Collections.unmodifiableSet( allLocales );
	}
}
