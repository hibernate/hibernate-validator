/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.wildfly.generictype;

import jakarta.validation.constraints.NotNull;

public interface GenericInterface<T> {
	void genericArg(@NotNull T arg);
}
