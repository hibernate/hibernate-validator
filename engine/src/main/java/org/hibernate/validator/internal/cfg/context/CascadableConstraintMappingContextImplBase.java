/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.cfg.context;

import java.util.Map;

import org.hibernate.validator.cfg.context.Cascadable;
import org.hibernate.validator.cfg.context.GroupConversionTargetContext;
import org.hibernate.validator.cfg.context.Unwrapable;

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
	private boolean unwrapValidatedValue;

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
	public C unwrapValidatedValue() {
		unwrapValidatedValue = true;
		return getThis();
	}

	boolean isUnwrapValidatedValue() {
		return unwrapValidatedValue;
	}
}
