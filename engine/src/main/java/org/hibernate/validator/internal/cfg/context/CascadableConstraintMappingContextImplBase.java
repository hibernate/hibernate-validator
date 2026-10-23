/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.cfg.context;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.cfg.context.Cascadable;
import org.hibernate.validator.cfg.context.ContainerElementConstraintMappingContext;
import org.hibernate.validator.cfg.context.ContainerElementTarget;
import org.hibernate.validator.cfg.context.GroupConversionTargetContext;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Base class for all implementations of cascadable context types.
 *
 * @author Gunnar Morling
 */
abstract class CascadableConstraintMappingContextImplBase<C extends Cascadable<C>>
		extends ConstraintMappingContextImplBase
		implements Cascadable<C> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	// Null for getters: their type depends on the destination factory's getter selection strategy.
	protected final Type configuredType;
	private boolean implicitContainerSelection;
	private boolean duplicateContainerElementPath;
	protected boolean isCascading;
	protected final Map<Class<?>, Class<?>> groupConversions = newHashMap();

	/**
	 * Contexts for configuring nested container elements, if any. Indexed by type parameter.
	 */
	private final Map<Integer, ContainerElementConstraintMappingContextImpl> containerElementContexts = new HashMap<>();
	private final Set<ContainerElementPathKey> configuredPaths = new HashSet<>();

	CascadableConstraintMappingContextImplBase(DefaultConstraintMapping mapping, Type configuredType) {
		super( mapping );
		this.configuredType = configuredType;
	}

	/**
	 * Returns this object, narrowed down to the specific sub-type.
	 *
	 * @return this object, narrowed down to the specific sub-type
	 *
	 * @see <a href="http://www.angelikalanger.com/GenericsFAQ/FAQSections/ProgrammingIdioms.html#FAQ206">"Get this" trick</a>
	 */
	protected abstract C getThis();

	/**
	 * Adds a group conversion for this element.
	 *
	 * @param from the source group of the conversion
	 * @param to the target group of the conversion
	 */
	public void addGroupConversion(Class<?> from, Class<?> to) {
		groupConversions.put( from, to );
	}

	@Override
	public C valid() {
		isCascading = true;
		return getThis();
	}

	@Override
	public GroupConversionTargetContext<C> convertGroup(Class<?> from) {
		return new GroupConversionTargetContextImpl<>( from, getThis(), this );
	}

	public ContainerElementConstraintMappingContext containerElement(ContainerElementTarget parent, TypeConstraintMappingContextImpl<?> typeContext) {
		if ( configuredType != null ) {
			checkImplicitSelection( configuredType );
		}
		ContainerElementConstraintMappingContext context = containerElement( parent, typeContext, 0 );
		implicitContainerSelection = true;
		return context;
	}

	public ContainerElementConstraintMappingContext containerElement(ContainerElementTarget parent, TypeConstraintMappingContextImpl<?> typeContext,
			int index, int... nestedIndexes) {
		Contracts.assertTrue( index >= 0, "Type argument index must not be negative" );
		for ( int nestedIndex : nestedIndexes ) {
			Contracts.assertTrue( nestedIndex >= 0, "Type argument index must not be negative" );
		}
		if ( configuredType != null ) {
			checkContainerType( configuredType );
		}
		ContainerElementPathKey key = new ContainerElementPathKey( index, nestedIndexes );
		if ( !configuredPaths.add( key ) ) {
			if ( configuredType == null ) {
				// Getters: the duplicate is detected here, but the type to report is only known once the destination
				// factory has resolved the getter, so the error is raised while materializing.
				duplicateContainerElementPath = true;
			}
			else {
				throw LOG.getContainerElementTypeHasAlreadyBeenConfiguredViaProgrammaticApiException( configuredType );
			}
		}
		ContainerElementConstraintMappingContextImpl context = containerElementContexts.get( index );
		if ( context == null ) {
			context = new ContainerElementConstraintMappingContextImpl( typeContext, parent, configuredType, index );
			containerElementContexts.put( index, context );
		}
		return nestedIndexes.length == 0 ? context : context.nestedContainerElement( nestedIndexes );
	}

	static void checkContainerType(Type type) {
		// HV-1428 Container element support is disabled for arrays.
		if ( TypeHelper.isArray( type ) ) {
			throw LOG.getContainerElementConstraintsAndCascadedValidationNotSupportedOnArraysException( type );
		}
		if ( !( type instanceof ParameterizedType ) ) {
			throw LOG.getTypeIsNotAParameterizedNorArrayTypeException( type );
		}
	}

	private static void checkImplicitSelection(Type type) {
		checkContainerType( type );
		if ( ( (ParameterizedType) type ).getActualTypeArguments().length > 1 ) {
			throw LOG.getNoTypeArgumentIndexIsGivenForTypeWithMultipleTypeArgumentsException( type );
		}
	}

	ResolvedConstraintMapping.ResolvedCascading materializeCascading(ConstraintLocation location, Type resolvedType) {
		if ( implicitContainerSelection ) {
			checkImplicitSelection( resolvedType );
		}
		if ( duplicateContainerElementPath ) {
			throw LOG.getContainerElementTypeHasAlreadyBeenConfiguredViaProgrammaticApiException( resolvedType );
		}
		Set<ConfiguredConstraint<?>> constraints = new HashSet<>();
		Map<TypeVariable<?>, CascadingMetaDataBuilder> cascading = new HashMap<>();
		for ( ContainerElementConstraintMappingContextImpl child : containerElementContexts.values() ) {
			ResolvedConstraintMapping.ResolvedCascading resolved = child.materialize( location );
			constraints.addAll( resolved.containerConstraints );
			cascading.put( resolved.cascading.getTypeParameter(), resolved.cascading );
		}
		return new ResolvedConstraintMapping.ResolvedCascading( constraints,
				CascadingMetaDataBuilder.annotatedObject( resolvedType, isCascading, cascading, new HashMap<>( groupConversions ) ) );
	}

	private static class ContainerElementPathKey {

		private final int index;
		private final int[] nestedIndexes;

		public ContainerElementPathKey(int index, int[] nestedIndexes) {
			this.index = index;
			this.nestedIndexes = nestedIndexes.clone();
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
