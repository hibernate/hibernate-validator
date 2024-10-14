/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.traversableresolver;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * @author Hardy Ferentschik
 */
@BookBusinessRules
@Entity
public class Book {
	@Id
	public Long id;

	@ManyToOne
	public Author author;
}
