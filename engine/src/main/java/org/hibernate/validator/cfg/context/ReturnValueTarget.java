/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.context;

/**
 * Facet of a constraint mapping creational context which allows to select the current method's or constructor's
 * return value as target for the next operations.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Gunnar Morling
 */
public interface ReturnValueTarget {

	/**
	 * Selects the current method's return value as the target for the next operations. The return value
	 * of one method or constructor may only be configured more once.
	 *
	 * @return A creational context representing the current method's or constructor's return value.
	 */
	ReturnValueConstraintMappingContext returnValue();
}
