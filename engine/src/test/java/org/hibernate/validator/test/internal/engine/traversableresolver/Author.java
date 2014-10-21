/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.traversableresolver;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.Valid;

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


