/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.json;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.invoke.MethodHandles;
import java.util.Set;

import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.json.JsonConstraintMapping;
import org.hibernate.validator.cfg.json.TypeConstraintMappingContext;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.JsonConfiguration;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Default implementation of {@link ConstraintMapping}.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Marko Bekhta
 */
public class JsonConstraintMappingImpl implements JsonConstraintMapping {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Set<Class<?>> configuredTypes;
	private final Set<JsonTypeConstraintMappingContextImpl<?>> typeContexts;

	public JsonConstraintMappingImpl() {
		this.configuredTypes = newHashSet();
		this.typeContexts = newHashSet();
	}

	@Override
	public final <C> TypeConstraintMappingContext<C> type(Class<C> type) {
		Contracts.assertNotNull( type, MESSAGES.beanTypeMustNotBeNull() );

		if ( configuredTypes.contains( type ) ) {
			throw LOG.getBeanClassHasAlreadyBeConfiguredViaProgrammaticApiException( type );
		}

		JsonTypeConstraintMappingContextImpl<C> typeContext = new JsonTypeConstraintMappingContextImpl<>( this, type );
		typeContexts.add( typeContext );
		configuredTypes.add( type );

		return typeContext;
	}

	public Set<Class<?>> getConfiguredTypes() {
		return configuredTypes;
	}

	/**
	 * Returns all bean configurations configured through this constraint mapping.
	 *
	 * @param constraintHelper constraint helper required for building constraint descriptors
	 * @param typeResolutionHelper type resolution helper
	 * @param valueExtractorManager the {@link ValueExtractor} manager
	 *
	 * @return a set of {@link JsonConfiguration}s with an element for each type configured through this mapping
	 */
	public Set<JsonConfiguration<?>> getBeanConfigurations(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager) {
		Set<JsonConfiguration<?>> configurations = newHashSet();

		for ( JsonTypeConstraintMappingContextImpl<?> typeContext : typeContexts ) {
			configurations.add( typeContext.build( constraintHelper, typeResolutionHelper, valueExtractorManager ) );
		}

		return configurations;
	}

}
