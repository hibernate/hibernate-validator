/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.returnvaluevalidation;

import jakarta.validation.Valid;

/**
 * @author Hardy Ferentschik
 */
public interface ContactService {
	void validateValidBeanParamConstraint(@Valid ContactBean bean);
}
