/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.properties;

/**
 * @author Marko Bekhta
 */
public interface Property extends Constrainable {

	String getPropertyName();

	String getResolvedPropertyName();

	PropertyAccessor createAccessor();
}
