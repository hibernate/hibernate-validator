/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

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
import org.hibernate.validator.internal.engine.cascading.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Base class for all implementations of cascadable context types.
 *
 * @author Gunnar Morling
 */
abstract class CascadableConstraintMappingContextImplBase<C extends Cascadable<C>>
		extends ConstraintMappingContextImplBase implements Cascadable<C> {

	private static final Log LOG = LoggerFactory.make();

	protected boolean isCascading;
	protected final Map<Class<?>, Class<?>> groupConversions = newHashMap();

	/**
	 * Contexts for configuring nested container elements, if any. Indexed by type parameter.
	 */
	protected final Map<Integer, ContainerElementConstraintMappingContextImpl> containerElementContexts = new HashMap<>();
	private final Set<ContainerElementPathKey> configuredPaths = new HashSet<>();

	CascadableConstraintMappingContextImplBase(DefaultConstraintMapping mapping) {
		super( mapping );
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
			ConstraintLocation location, int index, int... nestedIndexes) {
		Contracts.assertTrue( index >= 0, "Type argument index must not be negative" );

		ContainerElementPathKey key = new ContainerElementPathKey( index, nestedIndexes );
		boolean configuredBefore = !configuredPaths.add( key );
		if ( configuredBefore ) {
			throw LOG.getContainerElementHasAlreadyBeConfiguredViaProgrammaticApiException(
				location.getTypeForValidatorResolution()
			);
		}

		ContainerElementConstraintMappingContextImpl containerElementContext = new ContainerElementConstraintMappingContextImpl(
			typeContext, parent, location, index
		);

		containerElementContexts.put( index, containerElementContext );

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

	public Map<Class<?>, Class<?>> getGroupConversions() {
		return groupConversions;
	}

	protected Set<MetaConstraint<?>> getTypeArgumentConstraints(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager) {
		return containerElementContexts.values()
			.stream()
			.map( t -> t.build( constraintHelper, typeResolutionHelper, valueExtractorManager ) )
			.flatMap( Set::stream )
			.collect( Collectors.toSet() );
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
