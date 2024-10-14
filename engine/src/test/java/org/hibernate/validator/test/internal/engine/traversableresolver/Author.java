/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.traversableresolver;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.Valid;

/**
 * @author Hardy Ferentschik
 */
@Entity
@AuthorBusinessRules
public class Author {
	@Id
	public Long id;

	@Valid
	@OneToMany(mappedBy = "author")
	public List<Book> books = new ArrayList<Book>();
}
