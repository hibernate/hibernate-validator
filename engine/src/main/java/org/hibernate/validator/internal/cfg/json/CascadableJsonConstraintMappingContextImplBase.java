/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.json;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import org.hibernate.validator.cfg.json.Cascadable;
import org.hibernate.validator.cfg.json.GroupConversionTargetContext;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;

/**
 * Base class for all implementations of cascadable context types.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
abstract class CascadableJsonConstraintMappingContextImplBase<C extends Cascadable<C>>
		extends JsonConstraintMappingContextImplBase implements Cascadable<C> {

	private final Type configuredType;
	protected boolean isCascading;
	protected final Map<Class<?>, Class<?>> groupConversions = newHashMap();

	CascadableJsonConstraintMappingContextImplBase(JsonConstraintMappingImpl mapping, Type configuredType) {
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

	public boolean isCascading() {
		return isCascading;
	}

	protected CascadingMetaDataBuilder getCascadingMetaDataBuilder() {
		return CascadingMetaDataBuilder.annotatedObject( configuredType, isCascading, Collections.emptyMap(), groupConversions );
	}
}
