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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.cfg.propertyholder.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.propertyholder.PropertyHolderConstraintMappingContext;
import org.hibernate.validator.cfg.propertyholder.TypeConstraintMappingContext;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.propertyholder.ConstrainedPropertyHolderElementBuilder;
import org.hibernate.validator.internal.metadata.raw.propertyholder.PropertyHolderConfiguration;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Constraint mapping creational context which allows to configure the class-level constraints for one bean.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Marko Bekhta
 */
public final class PropertyHolderTypeConstraintMappingContextImpl extends ConstraintContextImplBase
		implements TypeConstraintMappingContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final String propertyHolderMappingName;

	private final Set<PropertyConstraintMappingContextImplBase> propertyContexts = newHashSet();
	private final Set<String> configuredMembers = newHashSet();

	private List<Class<?>> defaultGroupSequence;

	PropertyHolderTypeConstraintMappingContextImpl(PropertyHolderConstraintMappingImpl mapping, String propertyHolderMappingName) {
		super( mapping );
		this.propertyHolderMappingName = propertyHolderMappingName;
	}

	@Override
	public TypeConstraintMappingContext defaultGroupSequence(Class<?>... defaultGroupSequence) {
		this.defaultGroupSequence = Arrays.asList( defaultGroupSequence );
		return this;
	}

	@Override
	public PropertyConstraintMappingContext property(String property, Class<?> propertyType) {
		Contracts.assertNotNull( property, "The property name must not be null." );
		Contracts.assertNotEmpty( property, MESSAGES.propertyNameMustNotBeEmpty() );

		if ( configuredMembers.contains( property ) ) {
			throw LOG.getPropertyHolderMappingPropertyHasAlreadyBeenConfiguredViaProgrammaticApiException( propertyHolderMappingName, property );
		}

		PropertyConstraintMappingContextImpl context = new PropertyConstraintMappingContextImpl(
				this,
				property,
				propertyType
		);

		configuredMembers.add( property );
		propertyContexts.add( context );
		return context;
	}

	@Override
	public PropertyHolderConstraintMappingContext propertyHolder(String property) {
		Contracts.assertNotNull( property, "The property name must not be null." );
		Contracts.assertNotEmpty( property, MESSAGES.propertyNameMustNotBeEmpty() );

		if ( configuredMembers.contains( property ) ) {
			throw LOG.getPropertyHolderMappingPropertyHasAlreadyBeenConfiguredViaProgrammaticApiException( propertyHolderMappingName, property );
		}

		PropertyHolderConstraintMappingContextImpl context = new PropertyHolderConstraintMappingContextImpl(
				this,
				property
		);

		configuredMembers.add( property );
		propertyContexts.add( context );
		return context;
	}

	PropertyHolderConfiguration build() {
		return new PropertyHolderConfiguration(
				ConfigurationSource.API,
				propertyHolderMappingName,
				buildConstraintElements(),
				defaultGroupSequence
		);
	}

	private Set<ConstrainedPropertyHolderElementBuilder> buildConstraintElements() {
		Set<ConstrainedPropertyHolderElementBuilder> elements = newHashSet();

		//properties
		for ( PropertyConstraintMappingContextImplBase propertyContext : propertyContexts ) {
			elements.add( propertyContext.build() );
		}

		return elements;
	}
}
