/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.context;

/**
 * Facet of a constraint mapping creational context which allows to start a new constraint mapping or definition.
 *
 * @author Yoann Rodiere
 */
public interface ConstraintMappingTarget extends TypeTarget, ConstraintDefinitionTarget {

}
