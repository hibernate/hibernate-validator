/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.propertyholder;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.cfg.propertyholder.PropertyHolderConstraintMapping;
import org.hibernate.validator.cfg.propertyholder.TypeConstraintMappingContext;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.propertyholder.PropertyHolderConfiguration;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Implementation of {@link PropertyHolderConstraintMapping}.
 *
 * @author Marko Bekhta
 */
public class PropertyHolderConstraintMappingImpl implements PropertyHolderConstraintMapping {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Set<String> configuredMappingNames;
	private final Set<PropertyHolderTypeConstraintMappingContextImpl> typeContexts;

	public PropertyHolderConstraintMappingImpl() {
		this.configuredMappingNames = newHashSet();
		this.typeContexts = newHashSet();
	}

	@Override
	public final TypeConstraintMappingContext type(String propertyHolderMappingName) {
		Contracts.assertNotNull( propertyHolderMappingName, MESSAGES.mappingNameMustNotBeNull() );

		if ( configuredMappingNames.contains( propertyHolderMappingName ) ) {
			throw LOG.getPropertyHolderMappingHasAlreadyBeenConfiguredViaProgrammaticApiException( propertyHolderMappingName );
		}

		PropertyHolderTypeConstraintMappingContextImpl typeContext = new PropertyHolderTypeConstraintMappingContextImpl( this, propertyHolderMappingName );
		typeContexts.add( typeContext );
		configuredMappingNames.add( propertyHolderMappingName );

		return typeContext;
	}

	public Set<String> getConfiguredMappingNames() {
		return configuredMappingNames;
	}

	/**
	 * Returns all property holder configurations configured through this constraint mapping.
	 *
	 * @param constraintHelper constraint helper required for building constraint descriptors
	 * @param typeResolutionHelper type resolution helper
	 * @param valueExtractorManager the {@link ValueExtractor} manager
	 *
	 * @return a set of {@link PropertyHolderConfiguration}s with an element for each type configured through this mapping
	 */
	public Set<PropertyHolderConfiguration> getPropertyHolderConfigurations(ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager) {
		return typeContexts.stream()
				.map( context -> context.build() )
				.collect( Collectors.toSet() );
	}

}
