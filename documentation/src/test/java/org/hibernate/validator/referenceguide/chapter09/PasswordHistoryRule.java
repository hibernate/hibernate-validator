/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter09;

//tag::include[]
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

public class PasswordHistoryRule implements PasswordPolicyRule {

	private final PasswordHistoryService historyService;

	public PasswordHistoryRule(PasswordHistoryService historyService) {
		this.historyService = historyService;
	}

	@Override
	public String getMessage() {
		return "password was used recently";
	}

	@Override
	public boolean isValid(char[] password, HibernateConstraintValidatorContext context) {
		return !historyService.isPreviouslyUsed( password );
	}
}
//end::include[]
