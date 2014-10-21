/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import java.util.Map;

import org.hibernate.validator.cfg.context.Cascadable;
import org.hibernate.validator.cfg.context.GroupConversionTargetContext;
import org.hibernate.validator.cfg.context.Unwrapable;
import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

/**
 * Base class for all implementations of cascadable context types.
 *
 * @author Gunnar Morling
 */
abstract class CascadableConstraintMappingContextImplBase<C extends Cascadable<C> & Unwrapable<C>>
		extends ConstraintMappingContextImplBase implements Cascadable<C>, Unwrapable<C> {

	protected boolean isCascading;
	protected Map<Class<?>, Class<?>> groupConversions = newHashMap();
	private UnwrapMode unwrapMode = UnwrapMode.AUTOMATIC;

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
		return new GroupConversionTargetContextImpl<C>( from, getThis(), this );
	}

	public boolean isCascading() {
		return isCascading;
	}

	public Map<Class<?>, Class<?>> getGroupConversions() {
		return groupConversions;
	}

	@Override
	public C unwrapValidatedValue(boolean unwrap) {
		if ( unwrap ) {
			unwrapMode = UnwrapMode.UNWRAP;
		}
		else {
			unwrapMode = UnwrapMode.SKIP_UNWRAP;
		}
		return getThis();
	}

	UnwrapMode unwrapMode() {
		return unwrapMode;
	}
}
