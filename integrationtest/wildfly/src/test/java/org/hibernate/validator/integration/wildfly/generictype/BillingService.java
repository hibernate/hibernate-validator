/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.wildfly.generictype;

public interface BillingService<T> {

	int getBillingAmount(T order);
}
