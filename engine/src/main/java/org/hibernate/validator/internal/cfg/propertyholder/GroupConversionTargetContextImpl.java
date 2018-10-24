/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.propertyholder;

import org.hibernate.validator.cfg.propertyholder.GroupConversionTargetContext;

/**
 * Context allowing to set the target of a group conversion.
 *
 * @author Gunnar Morling
 */
class GroupConversionTargetContextImpl<C> implements GroupConversionTargetContext<C> {

	private final C cascadableContext;
	private final Class<?> from;
	private final GroupConversionTargetContextHelper<?> target;

	GroupConversionTargetContextImpl(Class<?> from, C cascadableContext, GroupConversionTargetContextHelper<?> target) {
		this.from = from;
		this.cascadableContext = cascadableContext;
		this.target = target;
	}

	@Override
	public C to(Class<?> to) {
		target.addGroupConversion( from, to );
		return cascadableContext;
	}
}
