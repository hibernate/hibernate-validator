/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter09;

//tag::include[]
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

public class DictionaryRule implements PasswordPolicyRule {

	private final DictionaryService dictionaryService;

	public DictionaryRule(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	@Override
	public String getMessage() {
		return "password must not contain a common dictionary word";
	}

	@Override
	public boolean isValid(char[] password, HibernateConstraintValidatorContext context) {
		return !dictionaryService.containsDictionaryWord( new String( password ) );
	}
}
//end::include[]
