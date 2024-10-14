/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.wildfly.generictype;

import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;

@ValidateOnExecution(type = ExecutableType.ALL)
public class StringImpl implements StringInterface {
	@Override
	public void genericArg(String arg) {
	}
}
