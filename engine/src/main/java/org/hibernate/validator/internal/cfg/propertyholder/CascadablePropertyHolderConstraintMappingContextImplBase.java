/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.propertyholder;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.util.Map;

import org.hibernate.validator.cfg.propertyholder.Cascadable;
import org.hibernate.validator.cfg.propertyholder.GroupConversionTargetContext;
import org.hibernate.validator.internal.metadata.aggregated.cascading.PropertyHolderCascadingMetaDataBuilder;

/**
 * Base class for all implementations of cascadable context types.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
abstract class CascadablePropertyHolderConstraintMappingContextImplBase<C extends Cascadable<C>>
		extends PropertyConstraintMappingContextImplBase implements Cascadable<C>, GroupConversionTargetContextHelper<C> {

	protected final Map<Class<?>, Class<?>> groupConversions = newHashMap();
	protected boolean isCascading;
	private String mapping;

	CascadablePropertyHolderConstraintMappingContextImplBase(PropertyHolderConstraintMappingImpl mapping, String property) {
		super( mapping, property );
	}

	protected abstract C getThis();

	@Override
	public void addGroupConversion(Class<?> from, Class<?> to) {
		groupConversions.put( from, to );
	}

	@Override
	public C valid(String mapping) {
		this.mapping = mapping;
		this.isCascading = true;

		return getThis();
	}

	@Override
	public GroupConversionTargetContext<C> convertGroup(Class<?> from) {
		return new GroupConversionTargetContextImpl<>( from, getThis(), this );
	}

	public boolean isCascading() {
		return isCascading;
	}

	protected PropertyHolderCascadingMetaDataBuilder getCascadingMetaDataBuilder() {
		return PropertyHolderCascadingMetaDataBuilder.simplePropertyHolder( mapping, isCascading, groupConversions );
	}
}
