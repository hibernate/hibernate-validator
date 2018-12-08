/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
import java.util.stream.Collectors;

import org.hibernate.validator.cfg.context.Cascadable;
import org.hibernate.validator.cfg.context.ContainerElementConstraintMappingContext;
import org.hibernate.validator.cfg.context.ContainerElementTarget;
import org.hibernate.validator.cfg.context.GroupConversionTargetContext;
import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
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
		extends ConstraintMappingContextImplBase implements Cascadable<C> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Type configuredType;
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

	public ContainerElementConstraintMappingContext containerElement(ContainerElementTarget parent, TypeConstraintMappingContextImpl<?> typeContext,
			ConstraintLocation location) {

		// HV-1428 Container element support is disabled for arrays
		if ( TypeHelper.isArray( configuredType ) ) {
			throw LOG.getContainerElementConstraintsAndCascadedValidationNotSupportedOnArraysException( configuredType );
		}

		if ( configuredType instanceof ParameterizedType ) {
			if ( ( (ParameterizedType) configuredType ).getActualTypeArguments().length > 1 ) {
				throw LOG.getNoTypeArgumentIndexIsGivenForTypeWithMultipleTypeArgumentsException( configuredType );
			}
		}
		else if ( !TypeHelper.isArray( configuredType ) ) {
			throw LOG.getTypeIsNotAParameterizedNorArrayTypeException( configuredType );
		}

		return containerElement( parent, typeContext, location, 0 );
	}

	public ContainerElementConstraintMappingContext containerElement(ContainerElementTarget parent, TypeConstraintMappingContextImpl<?> typeContext,
			ConstraintLocation location, int index, int... nestedIndexes) {
		Contracts.assertTrue( index >= 0, "Type argument index must not be negative" );

		// HV-1428 Container element support is disabled for arrays
		if ( TypeHelper.isArray( configuredType ) ) {
			throw LOG.getContainerElementConstraintsAndCascadedValidationNotSupportedOnArraysException( configuredType );
		}

		if ( !( configuredType instanceof ParameterizedType ) && !( TypeHelper.isArray( configuredType ) ) ) {
			throw LOG.getTypeIsNotAParameterizedNorArrayTypeException( configuredType );
		}

		ContainerElementPathKey key = new ContainerElementPathKey( index, nestedIndexes );
		boolean configuredBefore = !configuredPaths.add( key );
		if ( configuredBefore ) {
			throw LOG.getContainerElementTypeHasAlreadyBeenConfiguredViaProgrammaticApiException(
				location.getTypeForValidatorResolution()
			);
		}

		// As we already checked that the specific path was not yet configured we should not worry about returning the same context here,
		// as it means that there are some nested indexes which make a difference, And at the end a new context will be returned by call
		// to containerElementContext#nestedContainerElement().
		ContainerElementConstraintMappingContextImpl containerElementContext = containerElementContexts.get( index );
		if ( containerElementContext == null ) {
			containerElementContext = new ContainerElementConstraintMappingContextImpl( typeContext, parent, location, index );
			containerElementContexts.put( index, containerElementContext );
		}

		if ( nestedIndexes.length > 0 ) {
			return containerElementContext.nestedContainerElement( nestedIndexes );
		}
		else {
			return containerElementContext;
		}
	}

	public boolean isCascading() {
		return isCascading;
	}

	protected Set<MetaConstraint<?>> getTypeArgumentConstraints(ConstraintCreationContext constraintCreationContext) {
		return containerElementContexts.values()
			.stream()
			.map( t -> t.build( constraintCreationContext ) )
			.flatMap( Set::stream )
			.collect( Collectors.toSet() );
	}

	protected CascadingMetaDataBuilder getCascadingMetaDataBuilder() {
		Map<TypeVariable<?>, CascadingMetaDataBuilder> typeParametersCascadingMetaData = containerElementContexts.values().stream()
				.filter( c -> c.getContainerElementCascadingMetaDataBuilder() != null )
				.collect( Collectors.toMap( c -> c.getContainerElementCascadingMetaDataBuilder().getTypeParameter(),
						c -> c.getContainerElementCascadingMetaDataBuilder() ) );

		for ( ContainerElementConstraintMappingContextImpl typeArgumentContext : containerElementContexts.values() ) {
			CascadingMetaDataBuilder cascadingMetaDataBuilder = typeArgumentContext.getContainerElementCascadingMetaDataBuilder();
			if ( cascadingMetaDataBuilder != null ) {
				typeParametersCascadingMetaData.put( cascadingMetaDataBuilder.getTypeParameter(), cascadingMetaDataBuilder );
			}
		}

		return CascadingMetaDataBuilder.annotatedObject( configuredType, isCascading, typeParametersCascadingMetaData, groupConversions );
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
