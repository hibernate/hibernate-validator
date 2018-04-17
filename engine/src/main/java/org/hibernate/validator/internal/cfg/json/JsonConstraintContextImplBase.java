/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.json;

import org.hibernate.validator.cfg.json.TypeConstraintMappingContext;

/**
 * Base class for implementations of constraint-related context types.
 *
 * @author Gunnar Morling
 * @author Yoann Rodiere
 * @author Marko Bekhta
 */
abstract class JsonConstraintContextImplBase {

	protected final JsonConstraintMappingImpl mapping;

	public JsonConstraintContextImplBase(JsonConstraintMappingImpl mapping) {
		this.mapping = mapping;
	}

	public <C> TypeConstraintMappingContext<C> type(Class<C> type) {
		return mapping.type( type );
	}
}
