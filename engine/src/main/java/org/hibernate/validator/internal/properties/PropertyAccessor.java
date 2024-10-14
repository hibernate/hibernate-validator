/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.properties;

/**
 * @author Guillaume Smet
 */
public interface PropertyAccessor {

	Object getValueFrom(Object bean);
}
