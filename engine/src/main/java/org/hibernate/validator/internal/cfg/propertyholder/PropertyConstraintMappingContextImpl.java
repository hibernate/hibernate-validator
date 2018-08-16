/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.propertyholder;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.propertyholder.CascadableContainerElementConstraintMappingContext;
import org.hibernate.validator.cfg.propertyholder.ContainerElementConstraintMappingContext;
import org.hibernate.validator.cfg.propertyholder.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.propertyholder.PropertyHolderConstraintMappingContext;
import org.hibernate.validator.internal.metadata.aggregated.cascading.NonCascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.MetaConstraintBuilder;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.propertyholder.ConstrainedPropertyHolderElementBuilder;
import org.hibernate.validator.internal.metadata.raw.propertyholder.SimpleConstrainedPropertyHolderElementBuilder;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Constraint mapping creational context which allows to configure the constraints for one proeprty holder simple property.
 * The type of the property should be a simple type or a collection and no cascading is allowed for it.
 *
 * @author Marko Bekhta
 */
final class PropertyConstraintMappingContextImpl extends PropertyConstraintMappingContextImplBase
		implements PropertyConstraintMappingContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final PropertyHolderTypeConstraintMappingContextImpl typeContext;

	private final Map<Integer, ContainerElementConstraintMappingContextImpl> containerElementContexts = new HashMap<>();
	private final Set<ContainerElementPathKey> configuredPaths = new HashSet<>();

	private final Class<?> type;


	PropertyConstraintMappingContextImpl(PropertyHolderTypeConstraintMappingContextImpl typeContext, String property, Class<?> type) {
		super( typeContext.getConstraintMapping(), property );
		this.typeContext = typeContext;
		this.type = type;
	}

	@Override
	public PropertyConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		super.addConstraint(
				new MetaConstraintBuilder(
						definition
				)
		);
		return this;
	}

	@Override
	public PropertyConstraintMappingContext property(String property, Class<?> propertyType) {
		return typeContext.property( property, propertyType );
	}

	@Override
	public PropertyHolderConstraintMappingContext propertyHolder(String property) {
		return typeContext.propertyHolder( property );
	}

	@Override
	public ContainerElementConstraintMappingContext containerElementType(Class<?> containerElementType) {
		if ( type.getTypeParameters().length > 1 ) {
			throw LOG.getNoTypeArgumentIndexIsGivenForTypeWithMultipleTypeArgumentsException( type );
		}
		return containerElementType( containerElementType, 0 );
	}

	@Override
	public ContainerElementConstraintMappingContext containerElementType(Class<?> containerElementType, int index, int... nestedIndexes) {
		Contracts.assertTrue( index >= 0, "Type argument index must not be negative" );

		// HV-1428 Container element support is disabled for arrays
		if ( type.isArray() ) {
			throw LOG.getContainerElementConstraintsAndCascadedValidationNotSupportedOnArraysException( type );
		}

		if ( ( type.getTypeParameters().length == 0 ) ) {
			throw LOG.getTypeIsNotAParameterizedNorArrayTypeException( type );
		}

		ContainerElementPathKey key = new ContainerElementPathKey( index, nestedIndexes );
		boolean configuredBefore = !configuredPaths.add( key );
		if ( configuredBefore ) {
			throw LOG.getContainerElementTypeHasAlreadyBeenConfiguredViaProgrammaticApiException(
					// TODO: add another exception method to log
					null
			);
		}

		// As we already checked that the specific path was not yet configured we should not worry about returning the same context here,
		// as it means that there are some nested indexes which make a difference, And at the end a new context will be returned by call
		// to containerElementContext#nestedContainerElement().
		ContainerElementConstraintMappingContextImpl containerElementContext = containerElementContexts.get( index );
		if ( containerElementContext == null ) {
			containerElementContext = new ContainerElementConstraintMappingContextImpl( typeContext, this, type, index, containerElementType );
			containerElementContexts.put( index, containerElementContext );
		}

		if ( nestedIndexes.length > 0 ) {
			return containerElementContext.nestedContainerElement( nestedIndexes );
		}
		else {
			return containerElementContext;
		}
	}

	@Override
	public CascadableContainerElementConstraintMappingContext containerElementType(String mapping) {
		return containerElementType( mapping, 0 );
	}

	@Override
	public CascadableContainerElementConstraintMappingContext containerElementType(String mapping, int index, int... nestedIndexes) {
		return null;
	}

	@Override
	protected ConstrainedPropertyHolderElementBuilder build() {
		return new SimpleConstrainedPropertyHolderElementBuilder(
				ConfigurationSource.API,
				property,
				type,
				getConstraints(),
				Collections.emptySet(),
				NonCascadingMetaDataBuilder.INSTANCE
		);
	}

	private static class ContainerElementPathKey {

		private final int index;
		private final int[] nestedIndexes;

		public ContainerElementPathKey(int index, int[] nestedIndexes) {
			this.index = index;
			this.nestedIndexes = nestedIndexes;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + index;
			result = prime * result + Arrays.hashCode( nestedIndexes );
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			ContainerElementPathKey other = (ContainerElementPathKey) obj;
			if ( index != other.index ) {
				return false;
			}
			if ( !Arrays.equals( nestedIndexes, other.nestedIndexes ) ) {
				return false;
			}
			return true;
		}
	}
}
