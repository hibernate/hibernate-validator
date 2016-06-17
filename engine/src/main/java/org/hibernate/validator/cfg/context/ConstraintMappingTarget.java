/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

/**
 * Facet of a constraint mapping creational context which allows to start a new constraint mapping or definition.
 *
 * @author Yoann Rodiere
 */
public interface ConstraintMappingTarget extends TypeTarget, ConstraintDefinitionTarget {

}
